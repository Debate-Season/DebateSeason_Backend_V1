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
#   3. 남은 ERROR를 유형별로 묶어 웹훅으로 보낸다
#   4. 웹훅 URL이 없으면 로컬 파일에만 기록하고 조용히 종료한다
#
# 웹훅 설정: /home/ubuntu/app/.env 에 아래 한 줄을 추가한다.
#   ALERT_WEBHOOK_URL=https://hooks.slack.com/services/...
#   (Discord를 쓰면 웹훅 URL 뒤에 /slack 을 붙이면 같은 형식으로 동작한다)
#
set -uo pipefail

APP_DIR=/home/ubuntu/app
STATE_DIR=/home/ubuntu/.error-monitor
CURSOR_FILE="$STATE_DIR/cursor"
LOG=/home/ubuntu/logs/error-monitor.log
UNIT=toronchul.service

# 한 번에 알림에 담을 최대 유형 수 / 알림 본문 최대 길이
MAX_GROUPS=8
MAX_SAMPLE_LEN=300

mkdir -p "$STATE_DIR" "$(dirname "$LOG")"

log() { echo "[$(date -u '+%Y-%m-%dT%H:%M:%SZ')] $*" >> "$LOG"; }

# ── 알려진 소음 ──────────────────────────────────────────────
# 여기 걸리는 로그는 알림을 보내지 않는다.
# 클라이언트가 원인이거나 서버 결함이 아닌 것들만 넣을 것.
# 새 소음이 생기면 이 배열에 추가하고 레포에도 반영한다.
BENIGN_PATTERNS=(
	# 만료된 토큰으로 온 요청. 정상적인 인증 흐름이다. (주 400건 수준)
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
# 일부러 걸러내지 않는 것:
#   'signature does not match' — 서명이 맞지 않는 토큰. 만료/형식오류와 달리
#   위조 시도이거나 JWT 시크릿이 바뀐 뒤 남은 토큰일 수 있어 눈에 띄는 게 낫다.
#   주 2건 수준이라 소음 부담도 없다. 거슬리면 위 배열에 추가하면 된다.

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

# ── 필터링 ───────────────────────────────────────────────────
ERRORS=$(printf '%s\n' "$RAW" | grep -E ' ERROR | SEVERE ' || true)

if [ -n "$ERRORS" ]; then
	for pattern in "${BENIGN_PATTERNS[@]}"; do
		ERRORS=$(printf '%s\n' "$ERRORS" | grep -vF "$pattern" || true)
	done
fi

ERRORS=$(printf '%s\n' "$ERRORS" | sed '/^$/d')

if [ -z "$ERRORS" ]; then
	exit 0
fi

COUNT=$(printf '%s\n' "$ERRORS" | wc -l | tr -d ' ')

# ── 유형별 묶기 ──────────────────────────────────────────────
# 로거 이름 + 메시지에서 가변값(숫자/날짜/UUID)을 지운 것을 시그니처로 삼는다.
GROUPED=$(printf '%s\n' "$ERRORS" \
	| sed -E 's/.* (ERROR|SEVERE) +[0-9]+ +--- +\[[^]]*\] +([^ ]+) +: +(.*)/\2 | \3/' \
	| sed -E 's/[0-9a-fA-F]{8}-[0-9a-fA-F-]{27}/<uuid>/g' \
	| sed -E 's/[0-9]{4}-[0-9]{2}-[0-9]{2}[T ][0-9:.]+Z?/<time>/g' \
	| sed -E 's/[0-9]+/<n>/g' \
	| sort | uniq -c | sort -rn)

# ── 알림 본문 구성 ───────────────────────────────────────────
BODY="🚨 *toronchul 에러 감지* — ${COUNT}건"$'\n'
i=0
while IFS= read -r line; do
	i=$((i + 1))
	[ "$i" -gt "$MAX_GROUPS" ] && break
	n=$(printf '%s' "$line" | awk '{print $1}')
	sig=$(printf '%s' "$line" | sed -E 's/^ *[0-9]+ +//' | cut -c1-"$MAX_SAMPLE_LEN")
	BODY+=$'\n'"▸ ${n}건 · ${sig}"
done <<< "$GROUPED"

GROUP_TOTAL=$(printf '%s\n' "$GROUPED" | wc -l | tr -d ' ')
if [ "$GROUP_TOTAL" -gt "$MAX_GROUPS" ]; then
	BODY+=$'\n'"…외 $((GROUP_TOTAL - MAX_GROUPS))개 유형 (전체는 서버 로그 확인)"
fi
BODY+=$'\n\n'"확인: journalctl -u ${UNIT} -S '15 min ago'"

log "감지 ${COUNT}건 / ${GROUP_TOTAL}개 유형"
printf '%s\n\n' "$BODY" >> "$LOG"

# ── 발송 ─────────────────────────────────────────────────────
set -a
# shellcheck disable=SC1091
[ -f "$APP_DIR/.env" ] && . "$APP_DIR/.env"
set +a

if [ -z "${ALERT_WEBHOOK_URL:-}" ]; then
	log "ALERT_WEBHOOK_URL 미설정 - 로컬 기록만 함"
	exit 0
fi

# JSON 문자열 이스케이프 (따옴표/역슬래시/개행)
ESCAPED=$(printf '%s' "$BODY" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()))')

HTTP=$(curl -s -o /dev/null -w '%{http_code}' -X POST \
	-H 'Content-Type: application/json' \
	-d "{\"text\": $ESCAPED}" \
	--max-time 10 \
	"$ALERT_WEBHOOK_URL" 2>/dev/null)

if [ "$HTTP" = "200" ] || [ "$HTTP" = "204" ]; then
	log "웹훅 발송 성공 (HTTP $HTTP)"
else
	log "웹훅 발송 실패 (HTTP $HTTP)"
fi

exit 0
