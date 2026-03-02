# 백엔드 수정 및 운영 배포 실행 계획

## 1. 개요

- **목적:** 로컬에서 수정된 백엔드 기능을 테스트 후, Lightsail 운영 서버에 자동 배포 및 검증.
- **대상:** main 브랜치 (PROD 서버 전용).
- **주요 방식:** GitHub Actions 기반의 SCP 전송 및 SSH 서비스 재시작.

---

## 2. 단계별 상세 절차

### Phase 1: 로컬 개발 및 사전 검증

1. **코드 수정:** 비즈니스 로직 수정 및 신규 API 개발.
2. **프로파일 확인:** `src/main/resources/application-prod.yml` 설정이 실제 운영 환경(DB 접속 정보 등)과 충돌이 없는지 확인.
3. **로컬 테스트:** `./gradlew test`를 수행하여 빌드 오류 및 테스트 실패 여부 확인.

### Phase 2: 운영 서버 환경 변수(Environment) 업데이트

수정된 코드에서 새로운 환경 변수(예: 외부 API 키, 새로운 DB 옵션)가 추가되었다면 배포 전 서버 설정을 먼저 수정해야 합니다.

1. **서버 접속:** `ssh -i [key] ubuntu@[LIGHTSAIL_HOST]`
2. **환경 변수 수정:** `nano /home/ubuntu/app/.env` 명령으로 변수 추가.
3. **권한 재확인:** 보안을 위해 파일 권한이 600으로 유지되고 있는지 확인합니다.

### Phase 3: CI/CD 파이프라인 가동 (배포 실행)

1. **코드 푸시:** 수정된 코드를 main 브랜치로 `git push` 합니다.
2. **워크플로우 트리거:** `PROD_CICD.yml`이 실행되며 아래 과정을 자동으로 수행합니다.
   - **Build:** JAR 파일 빌드.
   - **Transfer:** SCP를 통해 `/home/ubuntu/app/app.jar.new`로 전송.
   - **Backup:** 기존 `app.jar`을 `app.jar.backup`으로 백업.
   - **Swap:** `app.jar.new` → `app.jar`로 atomic swap (`mv`).
   - **Restart:** SSH 명령으로 `sudo systemctl restart debateseason` 실행.
   - **Health Check:** 자동 헬스 체크 (60초간 12회 x 5초 간격) 실행.
   - **Auto Rollback:** 헬스 체크 실패 시 `app.jar.backup`으로 자동 롤백.

### Phase 4: 운영 환경 배포 검증

배포 직후 아래 명령어를 통해 서비스가 정상적으로 올라왔는지 확인합니다.

1. **서비스 상태 확인:** `sudo systemctl status debateseason` 명령 결과가 `active (running)`이어야 합니다.
2. **헬스 체크:** `curl http://localhost:80/prod/actuator/health` 호출 시 `{"status":"UP"}` 응답을 확인합니다.
3. **로그 모니터링:** 오류 발생 시 `sudo journalctl -u debateseason -f`로 실시간 로그를 추적합니다.

---

## 3. 예외 상황 대응 (Rollback)

배포 후 헬스 체크 실패 시, 서비스 중단을 최소화하기 위한 대응 방안입니다.

| 상황 | 대응 조치 |
|------|----------|
| 빌드 실패 | GitHub Actions 로그 확인 후 로컬에서 수정하여 재푸시. |
| 서비스 기동 실패 | `.env` 파일 설정 누락 여부 확인 및 `journalctl` 로그 분석. |
| 헬스 체크 실패 (자동 롤백 성공) | CI/CD가 `app.jar.backup`으로 자동 복구. 원인 분석 후 재배포. |
| 자동 롤백도 실패 | SSH 접속 후 수동 롤백: `cp app.jar.backup app.jar && sudo systemctl restart debateseason` |
| 긴급 롤백 필요 | 이전 배포 성공 시점의 커밋으로 `git revert` 후 다시 push하여 자동 배포 유도. |

### 수동 긴급 롤백 절차

```bash
# 1. 서버 접속
ssh -i [key] ubuntu@[LIGHTSAIL_HOST]

# 2. 백업 jar로 복구
cp /home/ubuntu/app/app.jar.backup /home/ubuntu/app/app.jar

# 3. 서비스 재시작
sudo systemctl restart debateseason

# 4. 복구 확인
curl http://localhost:80/prod/actuator/health
```

---

## 4. 향후 개선 권고 사항

- **무중단 배포:** 현재는 restart 시 수 초간 다운타임이 발생하므로, 서비스 규모 확장 시 Nginx를 활용한 Blue-Green 배포 도입을 검토하세요.
- **로그 관리:** 서버 내 로그 파일이 쌓여 디스크가 부족해지지 않도록 `logrotate` 설정을 추가하는 것을 권장합니다.
- **환경 변수 관리:** `.env` 수동 편집 대신 GitHub Secrets + CI/CD 자동 동기화 또는 AWS SSM Parameter Store 도입을 검토하세요.
