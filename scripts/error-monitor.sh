#!/usr/bin/env bash
#
# 백엔드 ERROR 로그 감시. cron이 주기적으로 호출한다.
#
# 배치 위치: /home/ubuntu/scripts/error-monitor.sh
# cron:      */10 * * * *   (10분마다)
#
# 동작:
#   1. 지난 실행 이후 새로 쌓인 로그만 읽는다 (journald 커서 사용 → 누락/중복 없음)
#   2. 알려진 소음을 걸러낸다 (BENIGN_PATTERNS)
#   3. 남은 ERROR를 유형별로 묶고, 각 유형의 근본 원인(Caused by)과
#      애플리케이션 스택 프레임을 함께 뽑아 웹훅으로 보낸다
#   4. 웹훅 URL이 없으면 로컬 파일에만 기록하고 조용히 종료한다
#
# 웹훅 설정: /home/ubuntu/app/.env 에 아래 한 줄을 추가한다.
#   ALERT_WEBHOOK_URL=https://discord.com/api/webhooks/...   (Discord)
#   ALERT_WEBHOOK_URL=https://hooks.slack.com/services/...   (Slack)
#
# URL을 보고 형식을 자동으로 맞춘다. Discord는 {"content":...} + **굵게**,
# Slack은 {"text":...} + *굵게* 를 쓴다. Discord 채널 웹훅은
# 디스코드 앱의 [채널 편집 > 연동 > 웹후크]에서 만든다.
# (개발자 포털의 "Webhook Events"는 반대 방향 기능이라 여기에 쓸 수 없다)
#
set -uo pipefail

APP_DIR=/home/ubuntu/app
STATE_DIR=/home/ubuntu/.error-monitor
CURSOR_FILE="$STATE_DIR/cursor"
LOG=/home/ubuntu/logs/error-monitor.log
UNIT=toronchul.service

# 한 번에 알림에 담을 최대 유형 수 / 각 부분의 최대 길이
MAX_GROUPS=8
MAX_SAMPLE_LEN=220
MAX_CAUSE_LEN=260
# 유형 목록이 차지할 수 있는 최대 길이. Discord 본문 상한(2000자)에서
# 머리말·꼬리말과 이스케이프 여유를 뺀 값이다.
BODY_BUDGET=1500

mkdir -p "$STATE_DIR" "$(dirname "$LOG")"

log() { echo "[$(date -u '+%Y-%m-%dT%H:%M:%SZ')] $*" >> "$LOG"; }

# ── 알려진 소음 ──────────────────────────────────────────────
# 여기 걸리는 로그는 알림을 보내지 않는다.
# 클라이언트가 원인이거나 서버 결함이 아닌 것들만 넣을 것.
# 새 소음이 생기면 이 배열에 추가하고 레포에도 반영한다.
#
# 2026-08-09: 인증 실패 로그는 대부분 앱에서 직접 레벨을 내렸다(아래 "레벨로 해결한 것" 참고).
# 그쪽이 근본 해결이라 새 인증 소음은 여기에 추가하기 전에 로그 레벨부터 검토할 것.
BENIGN_PATTERNS=(
	# 만료된 토큰으로 온 요청. 정상적인 인증 흐름이다. (주 400건 수준)
	# 앱에서 DEBUG 로 내렸지만, 배포 이전 버전이 남긴 로그를 위해 유지한다.
	'JWT 토큰 검증 실패: JWT expired'
	'WebSocket CONNECT - 토큰 검증 실패: JWT expired'
	'Token has expired'
	'Authentication token is missing'
	# 형식이 깨진 토큰. 클라이언트가 보낸 값의 문제다.
	# (CLAUDE.md에도 "알려진 소음"으로 기록되어 있음)
	'Malformed protected header JSON'
	'Invalid compact JWT string'
	# WebSocket 엔드포인트로 온 일반 HTTP 요청(헬스체크·스캐너 등)
	'invalid Upgrade header'
	# YouTube API 일일 할당량. 키/쿼터 이슈이지 서버 결함이 아니다.
	'유튜브 API 할당량 모두 소진'
)
# 레벨로 해결한 것 (여기서 걸러낼 필요가 없다):
#   'signature does not match' — 시크릿 교체 이전에 발급된 토큰. 휴면 사용자가 오랜만에
#   앱을 켜면 401 -> /auth/reissue -> 재시도로 1초 안에 스스로 복구한다. 서버 결함이 아니라
#   JwtAuthenticationErrorHandler#handleStaleSignatureToken 에서 INFO 로 남기므로
#   이 스크립트의 ERROR 필터에 아예 걸리지 않는다.
#   단, 서명 불일치는 위조 시도와 구분되지 않는다. 로그가 사라진 게 아니라 레벨만 내려간 것이니
#   급증이 의심되면 직접 확인할 것:
#     journalctl -u toronchul.service -S '1 day ago' | grep -c '휴면 사용자 복귀'

# ── 새 로그 수집 ─────────────────────────────────────────────
if [ -f "$CURSOR_FILE" ] && [ -s "$CURSOR_FILE" ]; then
	RAW=$(journalctl -u "$UNIT" --after-cursor="$(cat "$CURSOR_FILE")" \
		--no-pager -o short-iso --show-cursor 2>/dev/null)
else
	# 최초 실행: 과거 로그 전체를 알리지 않도록 최근 10분만 본다
	RAW=$(journalctl -u "$UNIT" --since '10 min ago' \
		--no-pager -o short-iso --show-cursor 2>/dev/null)
	log "커서 없음 - 최근 10분부터 시작"
fi

if [ -z "$RAW" ]; then
	exit 0
fi

# --show-cursor가 마지막에 붙이는 "-- cursor: s=..." 줄에서 커서를 뽑아 저장
NEW_CURSOR=$(printf '%s\n' "$RAW" | sed -n 's/^-- cursor: //p' | tail -1)
if [ -n "$NEW_CURSOR" ]; then
	printf '%s' "$NEW_CURSOR" > "$CURSOR_FILE"
fi

# ── 웹훅 대상 판별 ───────────────────────────────────────────
# 굵게 표시 문법이 플랫폼마다 다르다. Discord는 **, Slack은 *.
set -a
# shellcheck disable=SC1091
[ -f "$APP_DIR/.env" ] && . "$APP_DIR/.env"
set +a

case "${ALERT_WEBHOOK_URL:-}" in
	*discord.com*|*discordapp.com*) PLATFORM=discord; B='**' ;;
	*)                              PLATFORM=slack;   B='*'  ;;
esac

# ── 필터링 · 유형별 묶기 · 원인 추출 ─────────────────────────
# 예전에는 `grep ' ERROR '` 로 헤더 줄만 남겼는데, 스택트레이스와 "Caused by:" 줄에는
# 레벨 문자열이 없어서 통째로 버려졌다. 그래서 알림에
#   "TaskUtils$LoggingErrorHandler | Unexpected error occurred in scheduled task"
# 처럼 원인이 빠진 껍데기만 실려 서버에 들어가 봐야만 뭔지 알 수 있었다.
# 이제 ERROR 줄에 딸린 연속줄(스택트레이스)까지 같이 들고 근본 원인을 뽑는다.
#
# 파서는 변수에 담아 `python3 -c` 로 넘긴다. `... | python3 - <<PY` 형태로 쓰면
# 히어독이 stdin을 차지해 파이프로 들어온 로그가 통째로 버려진다(조용히 0건이 된다).
read -r -d '' GROUPER <<'PY' || true
import os
import re
import sys
from collections import Counter, OrderedDict

MAX_GROUPS = int(os.environ["MAX_GROUPS"])
MAX_SIG_LEN = int(os.environ["MAX_SAMPLE_LEN"])
MAX_CAUSE_LEN = int(os.environ["MAX_CAUSE_LEN"])
BODY_BUDGET = int(os.environ["BODY_BUDGET"])
BENIGN = [p for p in os.environ.get("BENIGN_PATTERNS", "").split("\n") if p]

# journald 접두사: "<iso> <host> <unit>[<pid>]: "
PREFIX = re.compile(r"^\S+ \S+ [^:]*: ?")
# 애플리케이션 로그 한 줄의 시작. 이게 아니면 앞 줄에 딸린 연속줄(스택트레이스)이다.
APP_LINE = re.compile(r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}")
# "<시각> ERROR <pid> --- [<스레드>] <로거> : <메시지>"
HEADER = re.compile(
    r"^\S+ +(ERROR|SEVERE|WARN|INFO|DEBUG|TRACE) +\d+ +--- +\[[^\]]*\] +(\S+) +: +(.*)$"
)
CAUSED_BY = re.compile(r"^Caused by:\s*(.+)$")
THROWABLE = re.compile(r"^(?:[\w$]+\.)+[\w$]*(?:Exception|Error|Throwable)\b")
APP_FRAME = re.compile(r"^at (com\.debateseason_backend_v1\.[\w$.]+)\(([^)]*)\)")

events = []
current = None

for line in sys.stdin.read().splitlines():
    if line.startswith("-- "):  # --show-cursor 가 붙이는 줄
        continue
    m = PREFIX.match(line)
    msg = line[m.end():] if m else line

    if not APP_LINE.match(msg):
        if current is not None:
            current["trace"].append(msg.strip())
        continue

    # 새 로그 줄을 만났으니 직전 이벤트의 트레이스는 여기서 끝난다.
    current = None
    header = HEADER.match(msg)
    if not header or header.group(1) not in ("ERROR", "SEVERE"):
        continue
    if any(b in msg for b in BENIGN):
        continue
    current = {"logger": header.group(2), "text": header.group(3), "trace": []}
    events.append(current)

if not events:
    print("0\t0\t0")
    sys.exit(0)


def root_cause(trace):
    """가장 마지막 'Caused by:' 가 근본 원인이다. 없으면 최상위 예외 줄을 쓴다."""
    cause = None
    for t in trace:
        c = CAUSED_BY.match(t)
        if c:
            cause = c.group(1).strip()
    if cause is None:
        for t in trace:
            if THROWABLE.match(t):
                cause = t
                break
    return cause


def app_frame(trace):
    """우리 코드가 등장하는 첫 프레임. 프레임워크 프레임은 건너뛴다."""
    for t in trace:
        f = APP_FRAME.match(t)
        if f:
            owner = ".".join(f.group(1).rsplit(".", 2)[-2:])  # SbsNews.activate
            return f"{owner}({f.group(2)})"
    return None


def normalize(s):
    """가변값(UUID/시각/숫자)을 지운 것을 묶음 기준으로 삼는다."""
    s = re.sub(r"[0-9a-fA-F]{8}-[0-9a-fA-F-]{27}", "<uuid>", s)
    s = re.sub(r"\d{4}-\d{2}-\d{2}[T ][0-9:.]+Z?", "<time>", s)
    return re.sub(r"\d+", "<n>", s)


groups = OrderedDict()
for ev in events:
    cause = root_cause(ev["trace"])
    frame = app_frame(ev["trace"])
    sig = "{} | {}".format(ev["logger"], ev["text"])
    # 원인까지 묶음 기준에 넣는다. 같은 메시지라도 원인이 다르면 다른 사고다.
    key = (normalize(sig), normalize(cause or ""))
    g = groups.setdefault(key, {"n": 0, "sig": sig, "causes": Counter(), "frames": Counter()})
    g["n"] += 1
    if cause:
        g["causes"][cause] += 1
    if frame:
        g["frames"][frame] += 1

blocks = []
used = 0
for g in sorted(groups.values(), key=lambda g: -g["n"]):
    if len(blocks) >= MAX_GROUPS:
        break
    lines = ["▸ {}건 · {}".format(g["n"], g["sig"][:MAX_SIG_LEN])]
    if g["causes"]:
        lines.append("　↳ 원인: " + g["causes"].most_common(1)[0][0][:MAX_CAUSE_LEN])
    if g["frames"]:
        frame = g["frames"].most_common(1)[0][0]
        others = len(g["frames"]) - 1
        lines.append("　↳ 위치: {}{}".format(frame, " 외 {}곳".format(others) if others else ""))
    block = "\n".join(lines)
    # 첫 유형은 길이와 무관하게 넣는다. 아무것도 못 싣고 보내는 알림이 제일 쓸모없다.
    if blocks and used + len(block) > BODY_BUDGET:
        break
    blocks.append(block)
    used += len(block) + 1

print("{}\t{}\t{}".format(len(events), len(groups), len(blocks)))
print("\n".join(blocks))
PY

SUMMARY=$(printf '%s\n' "$RAW" | \
	MAX_GROUPS="$MAX_GROUPS" \
	MAX_SAMPLE_LEN="$MAX_SAMPLE_LEN" \
	MAX_CAUSE_LEN="$MAX_CAUSE_LEN" \
	BODY_BUDGET="$BODY_BUDGET" \
	BENIGN_PATTERNS="$(printf '%s\n' "${BENIGN_PATTERNS[@]}")" \
	python3 -c "$GROUPER")

HEAD_LINE=$(printf '%s\n' "$SUMMARY" | head -1)
COUNT=$(printf '%s' "$HEAD_LINE" | cut -f1)
GROUP_TOTAL=$(printf '%s' "$HEAD_LINE" | cut -f2)
SHOWN=$(printf '%s' "$HEAD_LINE" | cut -f3)

if [ -z "$COUNT" ] || [ "$COUNT" = "0" ]; then
	exit 0
fi

# ── 알림 본문 구성 ───────────────────────────────────────────
BODY="🚨 ${B}toronchul 에러 감지${B} — ${COUNT}건"$'\n'
BODY+=$'\n'"$(printf '%s\n' "$SUMMARY" | tail -n +2)"

if [ "$GROUP_TOTAL" -gt "$SHOWN" ]; then
	BODY+=$'\n'"…외 $((GROUP_TOTAL - SHOWN))개 유형 (전체는 서버 로그 확인)"
fi
BODY+=$'\n\n'"확인: journalctl -u ${UNIT} -S '15 min ago'"

log "감지 ${COUNT}건 / ${GROUP_TOTAL}개 유형 (알림에 ${SHOWN}개 포함)"
printf '%s\n\n' "$BODY" >> "$LOG"

# ── 발송 ─────────────────────────────────────────────────────
if [ -z "${ALERT_WEBHOOK_URL:-}" ]; then
	log "ALERT_WEBHOOK_URL 미설정 - 로컬 기록만 함"
	exit 0
fi

# Discord는 본문 2000자 제한이 있어 넘기면 400을 돌려준다.
# 위에서 BODY_BUDGET으로 이미 맞췄지만, 예상 밖으로 긴 원인 문자열에 대비한 안전망이다.
if [ "$PLATFORM" = discord ] && [ "${#BODY}" -gt 1900 ]; then
	BODY="${BODY:0:1900}"$'\n'"…(길이 제한으로 잘림)"
fi

# JSON 문자열 이스케이프 (따옴표/역슬래시/개행)
ESCAPED=$(printf '%s' "$BODY" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()))')

if [ "$PLATFORM" = discord ]; then
	PAYLOAD="{\"content\": $ESCAPED}"
else
	PAYLOAD="{\"text\": $ESCAPED}"
fi

HTTP=$(curl -s -o /dev/null -w '%{http_code}' -X POST \
	-H 'Content-Type: application/json' \
	-d "$PAYLOAD" \
	--max-time 10 \
	"$ALERT_WEBHOOK_URL" 2>/dev/null)

if [ "$HTTP" = "200" ] || [ "$HTTP" = "204" ]; then
	log "웹훅 발송 성공 ($PLATFORM, HTTP $HTTP)"
else
	log "웹훅 발송 실패 ($PLATFORM, HTTP $HTTP)"
fi

exit 0
