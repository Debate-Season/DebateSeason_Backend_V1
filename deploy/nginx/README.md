# nginx / sshd 설정 원본 (source of truth)

`deploy/toronchul.service` 와 같은 이유로 버전 관리한다 — **서버에만 두면 인스턴스
재생성 시 유실된다.** 실제로 2026-06-12 에 손으로 넣은 WebSocket 업그레이드 설정이
어느 레포에도 없어서, 서버를 다시 만들면 채팅이 통째로 깨지는 상태였다.

> ⚠️ **systemd 유닛과 달리 이 파일들은 CI 가 자동 동기화하지 않는다.**
> `PROD_CICD.yml` 은 `deploy/toronchul.service` 만 서버로 밀어 넣는다.
> 여기 있는 파일을 고쳐도 배포로 반영되지 않으므로, 아래 "적용" 절차를 손으로 밟아야 한다.
> 반대로 **서버에서 직접 고쳤다면 이 파일에도 반영해서 커밋해야 한다.**

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

## 적용

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

**인증서 라인은 Certbot 이 관리한다.** `ssl_certificate*` 줄은 갱신 시 Certbot 이
건드릴 수 있으므로, 서버 쪽이 바뀌었으면 이 파일로 덮어쓰기 전에 diff 를 먼저 볼 것.

**`toronchul.app` / `www.toronchul.app` 은 Vercel 을 가리킨다.** `server_name` 에
남아 있지만 이 서버로 실제 오는 건 `api.toronchul.app` 뿐이다.
