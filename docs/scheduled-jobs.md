# 정기 실행 작업 (스케줄러 · 크론 · 타이머) 현황

최종 갱신: 2026-09-04

이 시스템에서 주기적으로 도는 것들이 **네 군데에 흩어져 있다.** 앱 안(`@Scheduled`),
서버 crontab, systemd 타이머, 그리고 Claude 세션 크론이다. 소유자도 배포 경로도
다르고, 서로를 모른다. 장애를 볼 때 "이 시각에 뭐가 돌았나" 를 한 번에 확인할 데가
없어서 정리한다.

**바뀌면 이 문서도 같이 고칠 것.** 특히 새로 추가할 때는 아래 "추가할 때 확인할 것" 을
먼저 읽는다.

---

## 한눈에

| 무엇 | 주기 | 어디에 산다 | 배포 |
|---|---|---|---|
| 유튜브 뉴스 크롤링 ×5 | 72분마다 | 앱 `@Scheduled` | jar 배포 |
| 탈퇴 회원 익명화 | 매일 00:00 KST | 앱 `@Scheduled` | jar 배포 |
| DB 백업 | 매일 04:00 KST | ubuntu crontab | **수동** |
| ERROR 로그 감시 | 10분마다 | ubuntu crontab | **수동** |
| 인증서 갱신 | 하루 2회 | systemd 타이머 | OS 패키지 |
| 로그 로테이션 | 매일 | systemd 타이머 | OS 패키지 |
| 스플래시 멈춤 집계 | 2026-09-08 1회 | Claude 세션 크론 | 휘발성 |

시각 기준이 뒤섞여 있으니 주의한다. **앱은 KST**(JVM 타임존이 `+09:00`),
**crontab 과 systemd 타이머는 UTC**(서버 로케일), **nginx 로그도 UTC** 다.

---

## 1. 앱 내 `@Scheduled`

`DebateSeasonBackendV1Application` 에 `@EnableScheduling` 이 걸려 있다.
**jar 에 같이 배포되므로 코드 수정 → PR → 머지가 곧 반영이다.**

기본 스레드 풀이 1개라는 점을 기억할 것 — 아래 작업들이 **직렬로** 돈다. 하나가
느려지면 나머지가 밀린다.

### 유튜브 뉴스 크롤링 (5개)

`src/main/java/.../youtubeLive/scheduler/news/` — `KbsNews`, `MbcNews`, `SbsNews`,
`YtnNews`, `YonhapNews`. 전부 `@Scheduled(fixedRate = 4320000)` = **72분마다.**

`fixedRate` 라 앱 기동 시각 기준으로 돈다. 고정 시각이 아니므로 재배포하면 주기가
어긋난다. 5개가 같은 주기라 **동시에 몰린다.**

> ⚠️ **현재 유튜브 API 할당량이 상시 소진 상태다.** `유튜브 API 할당량 모두 소진`
> 이 반복 로그되고 일부 썸네일이 비어 보인다. 백엔드 버그가 아니라 할당량 문제이며,
> `error-monitor.sh` 의 무해 패턴에 등록돼 있어 알림은 가지 않는다.

### 탈퇴 회원 익명화

`src/main/java/.../user/scheduler/UserScheduler.java`
`@Scheduled(cron = "0 0 0 * * *")` = **매일 자정(KST).**

`WITHDRAWAL_PENDING` 상태 회원을 찾아 `canAnonymizeBySchedule()` 을 통과하면
user·profile 을 익명화한다. 개인정보 파기 요건에 걸린 작업이라 조용히 멈추면 안 된다.
`@Transactional` 이 메서드 전체에 걸려 있어 **한 건이 터지면 그 회차 전체가 롤백된다.**

### 비활성 (주석 처리됨)

`youtubeLive/scheduler/entertainment/` 의 `LckLive`, `KboLive` 는 `@Scheduled` 가
주석 처리돼 있다. 되살릴 거면 위 스레드 풀 직렬 실행을 같이 고려할 것.

---

## 2. 서버 crontab (`ubuntu` 사용자)

```bash
ssh debateseason-prod "crontab -l"
```

```cron
# DB 일일 백업 (UTC 19:00 = KST 04:00)
0 19 * * * /home/ubuntu/scripts/db-backup.sh
# 백엔드 ERROR 로그 감시 (10분마다)
*/10 * * * * /home/ubuntu/scripts/error-monitor.sh
```

> ⚠️ **스크립트 원본은 레포 `scripts/` 에 있지만 자동 동기화되지 않는다.**
> `deploy/nginx/` 와 달리 CI 가 건드리지 않으므로, 고쳤으면 **직접 서버에 올려야 한다.**
> ```bash
> scp scripts/db-backup.sh scripts/error-monitor.sh debateseason-prod:/home/ubuntu/scripts/
> ssh debateseason-prod "chmod +x /home/ubuntu/scripts/*.sh"
> ```
> crontab 자체도 버전 관리되지 않는다. 서버를 재생성하면 **두 작업 모두 사라진다.**

### `db-backup.sh`

`mysqldump --single-transaction` → gzip → `/home/ubuntu/backups/`, **30일 보존.**
로그는 `/home/ubuntu/backups/backup.log`.

`--events` 는 일부러 뺐다 — 이 서버는 `event_scheduler` 가 꺼져 있어 넣으면 덤프가 실패한다.

```bash
ssh debateseason-prod "ls -lh /home/ubuntu/backups/*.sql.gz | tail -3; tail -2 /home/ubuntu/backups/backup.log"
```

> 백업이 **같은 인스턴스 안에만 있다.** 인스턴스가 죽으면 백업도 같이 죽는다.
> 외부 보관은 아직 없다.

### `error-monitor.sh`

`journalctl` 커서 기반으로 지난 실행 이후의 새 로그만 읽어 ERROR 를 그룹핑하고,
`ALERT_WEBHOOK_URL`(Discord)로 보낸다. 로그는 `/home/ubuntu/logs/error-monitor.log`.

무해 패턴은 제외한다 — 만료 JWT, 잘못된 Upgrade 헤더, 유튜브 할당량 등. 클라이언트가
흔히 만드는 것들이라 넣어두면 알림이 무의미해진다. 패턴 목록은 스크립트 상단
`BENIGN_PATTERNS` 에 있다.

**알림이 안 온다고 정상이라는 뜻은 아니다.** 웹훅 발송 성공 기록이 마지막으로 남은 건
2026-08-08 이다. 그 뒤로 무해 패턴 밖의 ERROR 가 없었다는 뜻이지만, 스크립트가 죽어
있어도 똑같이 조용하다. 가끔 로그를 직접 확인할 것.

---

## 3. systemd 타이머 (OS)

```bash
ssh debateseason-prod "systemctl list-timers --all --no-pager"
```

대부분 우분투 기본이고, 우리가 신경 쓸 건 둘이다.

### `certbot.timer` — 인증서 갱신

만료 30일 이내일 때만 실제로 갱신한다. `/etc/cron.d/certbot` 에도 같은 항목이 있지만
**systemd 환경에서는 타이머가 우선하고 cron 쪽은 실행되지 않는다**(파일 안 주석 참조).

> ⚠️ **2026-09-03 에 갱신이 계속 실패하고 있던 걸 만료 5일 전에 발견했다.**
> 인증서 한 장에 `toronchul.app` + `www` + `api` 세 도메인이 묶여 있었는데 apex 와
> www 가 Vercel 로 옮겨간 뒤라, Lightsail 의 certbot 이 그 둘의 http-01 챌린지에
> 응답할 수 없었다(Vercel 이 404 반환). **한 도메인만 실패해도 인증서 전체가 갱신되지
> 않는다.** `api.toronchul.app` 한 장으로 줄여 해결했다.
>
> **이게 조용했다는 게 진짜 문제다.** 타이머는 정상 동작했고 `certbot certificates` 는
> 계속 "VALID" 로 표시했다. 로그를 직접 열기 전까지 징후가 없었다.
> 자세한 내용은 `deploy/nginx/README.md` 의 인증서 항목.

```bash
ssh debateseason-prod "sudo certbot certificates | grep -E 'Domains|Expiry'"
ssh debateseason-prod "sudo grep -iE 'error|fail' /var/log/letsencrypt/letsencrypt.log | tail"
```

### `logrotate.timer` — 로그 로테이션

nginx 는 `daily` + `rotate 14` + `compress` 다. 즉 **액세스 로그 보관은 14일.**
그보다 오래된 트래픽은 조사할 수 없다. 사후 분석 창이 2주라는 뜻이라, 장기 추세가
필요하면 별도로 집계를 떠야 한다.

---

## 4. Claude 세션 크론

`CronCreate` 로 만든 것은 **해당 Claude 세션의 메모리에만 있다.** 디스크에 안 쓰이고,
세션이 끝나면 사라진다. 반복 작업은 7일 뒤 자동 만료된다.

**운영 자동화에 쓰면 안 된다.** 조사 중 "며칠 뒤 다시 보자" 같은 일회성 후속 확인에만
쓴다. 진짜 자동화가 필요하면 서버 crontab 이나 앱 `@Scheduled` 로 내린다.

| ID | 예정 | 내용 |
|---|---|---|
| `28e17dd4` | 2026-09-08 10:13 KST (1회) | 모바일 스플래시 멈춤 건수 집계 |

집계 대상은 "UA `Dart/` 가 `version/check` 200 을 받은 뒤 같은 IP 에서 180초 내
후속 요청이 없는 세션" 이다. 2026-09-03 에 넣은 nginx `rt`/`urt` 필드로 전송 실패를
걸러내 실제 건수를 좁히는 게 목적이다.

> 세션이 끊겨 크론이 사라져도 **데이터는 안 날아간다.** nginx 로그 14일 보관 안이면
> 언제든 다시 셀 수 있다.

---

## 앞으로 만들 예정

우선순위 순. 전부 아직 승인 전이다.

### 1. 만료 refresh token 정리 — 승인 대기

**지금 `refresh_tokens` 149행 중 127행(85%)이 만료된 죽은 행이다.**
`AuthServiceV1.reissueToken()` 이 만료 토큰을 DB 조회 전에 막으므로 절대 쓰일 수 없는
데이터인데, 지우는 코드가 없어 계속 쌓인다.

앱 `@Scheduled` 로 넣는 게 맞다(익명화 스케줄러와 같은 결). 기존 127행 삭제는 운영
데이터 쓰기라 **별도 승인이 필요하다.**

> ⚠️ **`user_id` 로 묶어서 지우면 안 된다.** 행 1개 = 로그인 1회 = 기기별 세션이라,
> 한 유저가 여러 행을 갖는 게 정상이다. 반드시 **토큰 만료 여부로만** 판단할 것.
> `AuthServiceV1ReissueTest` 가 이 불변식을 고정하고 있다.

### 2. 인증서 만료 임박 알림 — 승인 대기

위 3번의 재발 방지다. 만료 N일 전(예: 21일)이면 `ALERT_WEBHOOK_URL` 로 보낸다.
`error-monitor.sh` 가 이미 그 웹훅을 쓰고 있으니 발송 부분을 그대로 가져다 쓰면 된다.

갱신 **실패** 자체도 같이 봐야 한다. 이번 건이 만료일은 멀쩡히 "VALID" 인데 갱신만
계속 실패하던 경우였다. `/var/log/letsencrypt/letsencrypt.log` 의 실패 라인을 함께
확인할 것.

### 3. 스플래시 멈춤 정기 집계 — 조건부

위 세션 크론(`28e17dd4`)의 1회 집계 결과에 따라 결정한다. 건수가 유의미하면 서버
crontab 으로 내려 주간 집계로 만들고, 앱 수정이 스토어에 나간 뒤 0으로 떨어지는지
확인한다. 미미하면 만들지 않는다.

---

## 추가할 때 확인할 것

**어느 층에 놓을지 먼저 정한다.** DB·도메인 로직이면 앱 `@Scheduled`(PR 로 배포되고
테스트가 붙는다). OS·파일·프로세스를 다루면 서버 crontab. 조사용 일회성이면 Claude
세션 크론.

**앱 `@Scheduled` 는 스레드 풀이 1개다.** 무거운 작업을 넣으면 뉴스 크롤링과 익명화가
밀린다. 오래 걸릴 것 같으면 풀 크기부터 손볼 것.

**서버 crontab 은 버전 관리되지 않는다.** 넣었으면 이 문서에 적고, 스크립트는
레포 `scripts/` 에 커밋한 뒤 수동으로 올린다. 안 그러면 인스턴스 재생성 때 사라진다.
(2026-06-12 에 손으로 넣은 nginx 설정이 어느 레포에도 없어서 서버를 다시 만들면
채팅이 통째로 깨지는 상태였던 전례가 있다.)

**시각대를 명시한다.** 앱은 KST, crontab·systemd·nginx 로그는 UTC 다. 주석에 둘 다
적어두면 나중에 로그를 맞춰 볼 때 헤매지 않는다.

**"조용한 실패" 를 설계에 넣는다.** 이번 인증서 건도, 지금 유튜브 할당량도, 실패해도
아무도 모르는 형태였다. 새 작업을 만들 때 **실패했을 때 누가 어떻게 아는지**를 같이
정한다. 성공 로그만 남기는 작업은 죽어도 티가 안 난다.

---

## 관련 문서

- `deploy/nginx/README.md` — nginx 설정 원본, 로그 포맷, 인증서 도메인 구성
- `docs/lightsail-deployment-guide.md` — 서버 구성 전반
- `CLAUDE.md` — 운영 접속 정보, 안전 규칙
