#!/usr/bin/env bash
#
# MariaDB 일일 백업. 서버의 cron이 호출한다.
#
# 배치 위치: /home/ubuntu/scripts/db-backup.sh
# cron:      0 19 * * *   (UTC 19:00 = KST 04:00. 서버 OS가 UTC임에 주의)
#
# DB가 앱과 같은 인스턴스에 있어 인스턴스 소실 시 함께 사라진다.
# 이 스크립트는 로컬 덤프까지만 담당하며, 원격 보관(S3 등)은 별도 과제.
#
set -euo pipefail

APP_DIR=/home/ubuntu/app
BACKUP_DIR=/home/ubuntu/backups
RETENTION_DAYS=30
LOG=/home/ubuntu/backups/backup.log

mkdir -p "$BACKUP_DIR"

log() { echo "[$(date -u '+%Y-%m-%dT%H:%M:%SZ')] $*" >> "$LOG"; }

# .env에서 접속정보를 읽는다. 값은 절대 로그로 출력하지 않는다.
set -a
# shellcheck disable=SC1091
. "$APP_DIR/.env"
set +a

DB_NAME="${DB_NAME:-debate_season}"
STAMP=$(date -u '+%Y%m%d-%H%M%S')
OUT="$BACKUP_DIR/${DB_NAME}-${STAMP}.sql.gz"

# 커맨드라인에 비밀번호가 노출되지 않도록 MYSQL_PWD를 쓴다(ps 방어).
export MYSQL_PWD="$DB_PASSWORD"

if mysqldump \
      -h 127.0.0.1 \
      -u "$DB_USERNAME" \
      --single-transaction \
      --quick \
      --routines \
      --triggers \
      --default-character-set=utf8mb4 \
      `# --events는 넣지 않는다: 이 서버는 event_scheduler가 꺼져 있어 덤프가 실패한다` \
      "$DB_NAME" 2>>"$LOG" | gzip -9 > "$OUT"; then
  SIZE=$(stat -c%s "$OUT")
  # 덤프가 비정상적으로 작으면 실패로 간주 (빈 덤프 방어)
  if [ "$SIZE" -lt 10240 ]; then
    log "FAIL: 덤프 크기가 비정상적으로 작음 (${SIZE} bytes) -> $OUT"
    rm -f "$OUT"
    exit 1
  fi
  log "OK: $OUT (${SIZE} bytes)"
else
  log "FAIL: mysqldump 실패"
  rm -f "$OUT"
  exit 1
fi

unset MYSQL_PWD

# 보존기간 경과분 정리
DELETED=$(find "$BACKUP_DIR" -name "${DB_NAME}-*.sql.gz" -mtime "+${RETENTION_DAYS}" -print -delete | wc -l)
[ "$DELETED" -gt 0 ] && log "정리: ${DELETED}개 삭제 (보존 ${RETENTION_DAYS}일)"

exit 0
