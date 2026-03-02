# CI/CD 마이그레이션: ECS → Lightsail VM

## 개요

서버를 AWS ECS에서 Lightsail VM으로 이전. DEV 서버는 폐기하고 PROD만 운영한다.

### 변경된 CI/CD 구조

| 파일 | 변경 내용 |
|------|----------|
| `PROD_CICD.yml` | deploy job을 SSH 기반 Lightsail 배포로 교체 |
| `DEV_CICD.yml` | deploy job 제거 (build + test만 유지) |

### 배포 흐름

```
GitHub Actions (build jar)
  → SCP로 jar 전송 (app.jar.new)
  → SSH: 기존 jar 백업 (app.jar → app.jar.backup)
  → SSH: atomic swap (app.jar.new → app.jar)
  → SSH: systemd restart
  → health check (60초간 12회 재시도)
  → 실패 시 app.jar.backup으로 자동 rollback
```

---

## 서버 1회 설정

Lightsail VM에 SSH 접속 후 아래 순서대로 진행한다.

### 1. 앱 디렉토리 생성

```bash
mkdir -p /home/ubuntu/app
```

### 2. 환경변수 파일 생성

```bash
nano /home/ubuntu/app/.env
```

내용 예시:

```
# Database
DB_DRIVER_CLASS_NAME=org.mariadb.jdbc.Driver
DB_URL=jdbc:mariadb://your-db-host:3306/debateseason
DB_USERNAME=your_user
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret
ACCESS_EXPIRE_TIME=3600000
REFRESH_EXPIRE_TIME=604800000

# External API
YOUTUBE_API_KEY=your_youtube_api_key
```

권한 설정:

```bash
chmod 600 /home/ubuntu/app/.env
```

### 3. Java 포트 80 바인딩 허용

```bash
sudo setcap 'cap_net_bind_service=+ep' $(readlink -f $(which java))
```

### 4. systemd 서비스 파일 생성

```bash
sudo nano /etc/systemd/system/debateseason.service
```

```ini
[Unit]
Description=DebateSeason Backend
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/app
EnvironmentFile=/home/ubuntu/app/.env
ExecStart=/usr/bin/java -Dspring.profiles.active=prod -Xms256m -Xmx512m -jar app.jar
Restart=on-failure
RestartSec=10
TimeoutStopSec=30

[Install]
WantedBy=multi-user.target
```

등록:

```bash
sudo systemctl daemon-reload
sudo systemctl enable debateseason
```

### 5. 서비스 시작

```bash
# 기존 java 프로세스 종료
sudo pkill -f 'java.*jar' || true

# 현재 jar를 app.jar로 복사 (기존 파일명에 맞게 수정)
cp /home/ubuntu/app/기존파일명.jar /home/ubuntu/app/app.jar

sudo systemctl start debateseason
```

### 6. 동작 확인

```bash
sudo systemctl status debateseason
curl http://localhost:80/prod/actuator/health
# 기대 결과: {"status":"UP"}
```

---

## 배포용 SSH 키 설정

### Lightsail 서버에서 키 생성

```bash
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/deploy_key -N ""
cat ~/.ssh/deploy_key.pub >> ~/.ssh/authorized_keys
```

### 프라이빗 키 확인

```bash
cat ~/.ssh/deploy_key
```

`-----BEGIN OPENSSH PRIVATE KEY-----`부터 `-----END OPENSSH PRIVATE KEY-----`까지 전체를 복사한다.

> **보안 참고:** GitHub Secrets에 등록한 후, 서버에 남은 프라이빗 키 파일은 삭제하는 것을 권장한다.
> ```bash
> rm ~/.ssh/deploy_key
> ```

---

## GitHub Secrets 등록

GitHub 리포지토리 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

### 신규 등록 (2개)

| Name | Value |
|------|-------|
| `LIGHTSAIL_HOST` | Lightsail VM 퍼블릭 IP (예: `3.35.xxx.xxx`) |
| `LIGHTSAIL_SSH_PRIVATE_KEY` | 위에서 복사한 프라이빗 키 전체 |

### 제거 가능 (기존 ECS 관련)

- `AWS_ROLE_TO_ASSUME`, `AWS_ACCOUNT_ID`
- `PROD_ECR_REPOSITORY_NAME`, `PROD_TASK_DEFINITION_NAME`, `PROD_ECS_SERVICE`, `PROD_ECS_CLUSTER`
- `DEV_ECR_REPOSITORY_NAME`, `DEV_TASK_DEFINITION_NAME`, `DEV_ECS_SERVICE`, `DEV_ECS_CLUSTER`

---

## 배포 시 자동 백업 및 롤백

`PROD_CICD.yml`의 deploy job이 아래 로직을 SSH heredoc으로 실행한다.

### 배포 흐름 상세 (PROD_CICD.yml 기준)

```
1. SCP: jar를 app.jar.new로 전송
2. 기존 app.jar → app.jar.backup으로 백업
3. app.jar.new → app.jar로 atomic swap (mv)
4. systemctl restart debateseason
5. health check: 12회 × 5초 간격 = 최대 60초 대기
6. 성공 → 배포 완료
7. 실패 → app.jar.backup을 app.jar로 복원 후 재시작
```

### 수동 긴급 롤백

CI/CD 자동 롤백이 실패하거나 수동 개입이 필요한 경우:

```bash
# 서버 접속
ssh -i [key] ubuntu@[LIGHTSAIL_HOST]

# 백업 jar로 복구
mv /home/ubuntu/app/app.jar.backup /home/ubuntu/app/app.jar

# 서비스 재시작
sudo systemctl restart debateseason

# 복구 확인
curl http://localhost:80/prod/actuator/health
```

---

## 검증

1. `sudo systemctl status debateseason` → active (running)
2. `curl http://localhost:80/prod/actuator/health` → `{"status":"UP"}`
3. main 브랜치에 커밋 push → GitHub Actions deploy job 성공
4. 헬스 체크 실패 테스트 → 자동 롤백 후 서비스 복구 확인
5. 모바일 앱에서 API 정상 호출 확인
