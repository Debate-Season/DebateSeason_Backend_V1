# nginx / sshd 설정 원본 (source of truth)

`deploy/toronchul.service` 와 같은 이유로 버전 관리한다 — **서버에만 두면 인스턴스
재생성 시 유실된다.** 실제로 2026-06-12 에 손으로 넣은 WebSocket 업그레이드 설정이
어느 레포에도 없어서, 서버를 다시 만들면 채팅이 통째로 깨지는 상태였다.

**`main` 에 push 하면 CI 가 자동으로 서버에 반영한다** (`PROD_CICD.yml` 의
`Sync nginx config` 스텝). 이 디렉토리가 곧 운영 설정이다 —
**서버에서 직접 고치면 다음 배포 때 이 파일 내용으로 덮어써진다.**

동기화 동작:

1. 4개 파일을 서버 스테이징으로 보내고 현재 설정과 `cmp` 한다. 같으면 아무것도 안 한다(no-op).
2. 다르면 백업(`/home/ubuntu/app/nginx-backups/<타임스탬프>/`, 최근 5세대 보관) 후 적용.
3. `nginx -t` 실패 → **reload 하지 않고** 백업으로 되돌린 뒤 배포 실패.
4. 통과하면 reload. 워커 PID 가 실제로 교체될 때까지 기다린 뒤 검증한다.
5. 검증 실패 → 백업으로 되돌리고 배포 실패.

jar 배포보다 **먼저** 돈다. 프록시가 깨진 채로 새 jar 가 올라가면 원인 구분이
어려워지므로, 문제가 있으면 jar 를 건드리기 전에 멈춘다.

> ⚠️ **`deploy/ssh/99-hardening.conf` 는 일부러 자동 동기화 대상에서 뺐다.**
> CI 가 서버에 닿는 유일한 경로가 SSH 라, sshd 설정을 잘못 밀어 넣으면 스스로를
> 잠가 버리고 복구에 Lightsail 웹 콘솔이 필요해진다. 내용도 한 줄짜리라 바뀔 일이
> 거의 없다. 아래 수동 절차로 적용한다.

## 파일 대응

| 레포 | 서버 |
|---|---|
| `nginx.conf` | `/etc/nginx/nginx.conf` |
| `conf.d/websocket_upgrade.conf` | `/etc/nginx/conf.d/websocket_upgrade.conf` |
| `sites-available/default` | `/etc/nginx/sites-enabled/default` |
| `www/_error.html` | `/var/www/html/_error.html` |
| `../ssh/99-hardening.conf` | `/etc/ssh/sshd_config.d/99-hardening.conf` |

## 패키지 의존성

`nginx.conf` 의 `more_clear_headers` 는 동적 모듈이 있어야 한다. 없으면 **nginx 가 아예 뜨지 않는다.**

```bash
sudo apt-get install -y libnginx-mod-http-headers-more-filter
```

`nginx-extras` 를 쓰면 안 된다 — `nginx-core` 를 교체해 버린다. 위 패키지는 모듈만
추가하며 `nginx-common` 을 정확한 버전으로 의존하므로 nginx 업그레이드 시 함께 올라간다.

`load_module` 은 **reload 로 반영되지 않는다.** 이 패키지를 새로 설치한 직후에는
`systemctl reload` 가 아니라 `systemctl restart nginx` 를 해야 한다.

CI 동기화 스텝은 이 패키지가 없으면 **자동으로 설치하고 reload 대신 restart** 한다.
서버를 재생성한 직후 첫 배포가 그 경우다.

## 수동 적용 (CI 우회 · 서버 재생성 직후 · sshd)

```bash
# 1) 백업
ssh debateseason-prod "sudo cp -a /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak.$(date +%Y%m%d) && \
  sudo cp -a /etc/nginx/sites-enabled/default /etc/nginx/sites-available/default.bak.$(date +%Y%m%d)"

# 2) 전송
scp deploy/nginx/nginx.conf                     debateseason-prod:/tmp/
scp deploy/nginx/conf.d/websocket_upgrade.conf  debateseason-prod:/tmp/
scp deploy/nginx/sites-available/default        debateseason-prod:/tmp/nginx-default
scp deploy/nginx/www/_error.html                debateseason-prod:/tmp/
scp deploy/ssh/99-hardening.conf                debateseason-prod:/tmp/

# 3) 배치 + 검증 후 반영 (nginx -t 가 실패하면 reload 하지 않는다)
ssh debateseason-prod "set -e
  sudo cp /tmp/nginx.conf /etc/nginx/nginx.conf
  sudo cp /tmp/websocket_upgrade.conf /etc/nginx/conf.d/
  sudo cp /tmp/nginx-default /etc/nginx/sites-enabled/default
  sudo cp /tmp/_error.html /var/www/html/_error.html
  sudo cp /tmp/99-hardening.conf /etc/ssh/sshd_config.d/
  sudo chmod 644 /var/www/html/_error.html /etc/ssh/sshd_config.d/99-hardening.conf
  sudo nginx -t && sudo systemctl reload nginx
  sudo sshd -t && sudo systemctl reload ssh"
```

## 검증

```bash
curl -sSI 'https://api.toronchul.app/prod/api/v1/issue?issue-id=1'  # 200, Server 헤더 없음
curl -sSi  'https://api.toronchul.app/prod/api/v1/nope'             # 401 + Spring JSON 본문 그대로
curl -sSi  'https://api.toronchul.app/nope'                         # 404, nginx 서명 없는 _error.html
curl -sSi -H 'Connection: Upgrade' -H 'Upgrade: websocket' \
  -H 'Sec-WebSocket-Version: 13' -H 'Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==' \
  'https://api.toronchul.app/prod/ws-stomp'                         # 101
ssh -v debateseason-prod exit 2>&1 | grep 'remote software'         # OpenSSH_8.9p1 (Ubuntu 접미사 없음)
```

## 건드릴 때 주의할 것

**`location ~ ^/prod(/|$)` 의 정규식을 prefix 로 바꾸지 말 것.**
`location /prod` 로 쓰면 `/prodfoo` 같은 유사 경로까지 매칭돼 백엔드로 넘어가고,
Tomcat 기본 404 HTML 이 응답으로 나가면서 WAS 종류가 노출된다.

**`proxy_intercept_errors` 를 켜지 말 것.**
켜면 `error_page` 가 `/prod` 하위 Spring 의 401/400 JSON 에러 응답까지 삼켜서
클라이언트가 에러 본문(`code`, `message`)을 읽지 못한다. 지금은 꺼져 있어서
nginx 자신이 만든 에러에만 `_error.html` 이 적용된다.

**`/prod` 밖 catch-all 을 프록시로 되돌리지 말 것.**
API 는 전부 `/prod` 컨텍스트(`application-prod.yml` 의 `server.servlet.context-path`)
아래에 있다. 2026-08-22 access log 감사 기준 `/prod` 밖 경로는 distinct 16,289개가
전부 스캐너(`/.git/config`, `/nacos/v1/auth/users`, `/vendor/phpunit/...`)였고
정상 트래픽은 0건이었다.

**CI 검증에 '특정 경로가 몇 번을 돌려주는지' 를 못박지 말 것.**
그건 오늘 설정의 성질이지 불변식이 아니다. 예전에 "`/prod` 밖은 404" 를 검증 조건으로
넣었더니, catch-all 을 정당하게 바꾸는 변경까지 CI 가 롤백할 구조가 됐다.
지금은 (1) nginx 가 TLS 를 물고 응답하는가 (2) 앱이 살아 있었다면 프록시가 되는가,
두 불변식만 본다.

**reload 직후에 바로 검증하지 말 것.**
`systemctl reload` 는 시그널만 보내고 즉시 반환한다. 곧바로 요청을 던지면 아직 살아 있는
옛 워커가 **이전 설정으로** 응답해서, 잘못된 설정이 검증을 그냥 통과한다.
실제로 이 경쟁 때문에 검증이 무력화되는 걸 확인했고, 지금은 워커 PID 가 교체된 걸
확인한 뒤에 검증한다.

**인증서 라인은 Certbot 이 관리한다.** `ssl_certificate*` 줄은 갱신 시 Certbot 이
건드릴 수 있으므로, 서버 쪽이 바뀌었으면 이 파일로 덮어쓰기 전에 diff 를 먼저 볼 것.

**`toronchul.app` / `www.toronchul.app` 은 Vercel 을 가리킨다.** `server_name` 에
남아 있지만 이 서버로 실제 오는 건 `api.toronchul.app` 뿐이다.
