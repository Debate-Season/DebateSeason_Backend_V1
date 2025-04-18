# 토론철 백엔드 (DebateSeason Backend)

## ✅ 목차

[1. 프로젝트 소개](#-프로젝트-소개)  
[2. 기술 스택](#-기술-스택)  
[3. 아키텍처](#-아키텍처)  
[4. 프로젝트 구조](#-프로젝트-구조)  
[5. 주요 기능](#-주요-기능)   
[6. 시작하기](#-시작하기)  
[7. 환경 설정](#-환경-설정)  
[8. API 문서](#-api-문서)  
[9. CI/CD](#-CI/CD)  
[10. 팀원 소개](#-팀원-소개)

## 📋 프로젝트 소개

토론철(DebateSeason)은 다양한 커뮤니티의 사용자들이 모여 여러 주제에 대해 실시간 채팅을 통해 토론을 할 수 있는 플랫폼입니다. 토론철은 “결국 대화를 통해 서로를 이해하고, 갈등을 해결할 수 있다”는
신념으로 서비스를 만들고 있습니다.

## 🛠 기술 스택

<img src="https://img.shields.io/badge/Language-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/java-%23ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"><img src="https://img.shields.io/badge/17-515151?style=for-the-badge">
</br>
<img src="https://img.shields.io/badge/Framework-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"><img src="https://img.shields.io/badge/3.3.5-515151?style=for-the-badge">
</br>
<img src="https://img.shields.io/badge/Database-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white"><img src="https://img.shields.io/badge/11.4.4-515151?style=for-the-badge">
</br>
<img src="https://img.shields.io/badge/ORM-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"><img src="https://img.shields.io/badge/3.1.0-515151?style=for-the-badge">
</br>
<img src="https://img.shields.io/badge/Security-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white"><img src="https://img.shields.io/badge/6.1.0-515151?style=for-the-badge"><img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"><img src="https://img.shields.io/badge/0.12.3-515151?style=for-the-badge">
</br>
<img src="https://img.shields.io/badge/Communication-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socket.io&logoColor=white"><img src="https://img.shields.io/badge/STOMP-000000?style=for-the-badge"><img src="https://img.shields.io/badge/2.3.3-515151?style=for-the-badge">
</br>
<img src="https://img.shields.io/badge/Documentation-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black"><img src="https://img.shields.io/badge/2.2.0-515151?style=for-the-badge">
</br>
<img src="https://img.shields.io/badge/Auth-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/OIDC-2671E5?style=for-the-badge&logo=openid&logoColor=white"><img src="https://img.shields.io/badge/Kakao_&_Apple-515151?style=for-the-badge">
</br>
<img src="https://img.shields.io/badge/Build-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"><img src="https://img.shields.io/badge/8.4-515151?style=for-the-badge">
</br>
<img src="https://img.shields.io/badge/Container-%23121011?style=for-the-badge"><img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"><img src="https://img.shields.io/badge/24.0.5-515151?style=for-the-badge">

## 🏗 아키텍처

<img width="1196" alt="Image" src="https://github.com/user-attachments/assets/7d779d6c-6128-4232-bd5c-ed6a6cebb033" />

## 📑 프로젝트 구조

```plaintext
src/main/java/com/debateseason_backend_v1/
├── common/             # 공통 모듈 (예외 처리, 응답 객체 등)
├── config/             # 애플리케이션 설정
├── domain/             # 도메인별 로직
│   ├── chat/           # 채팅 관련
│   ├── chatroom/       # 토론방 관련
│   ├── issue/          # 이슈 관련
│   ├── profile/        # 사용자 프로필 관련
│   └── user/           # 사용자 인증 관련
├── security/           # 보안 관련 (JWT 등)
└── DebateSeasonBackendV1Application.java
```

## 🚀 주요 기능

### 인증 및 사용자 관리

- OIDC 인증 로그인(Kakao, Apple)
- JWT 기반 인증
- 토큰 재발급
- 사용자 프로필 관리

### 이슈방 (토론 주제)

- 이슈 목록 조회
- 이슈 상세 조회
- 이슈 즐겨찾기

### 채팅방 (토론 안건)

- 채팅방 생성
- 채팅방 상세 조회
- 찬성/반대 투표 기능

### 실시간 채팅

- WebSocket/STOMP 기반 실시간 메시지 전송
- 채팅 메시지 페이지네이션 조회

## 📌 시작하기

### 필수 조건

- JDK 17 이상
- Gradle
- MariaDB

### 설치 및 실행

1. 저장소 클론
   ```bash
   git clone https://github.com/your-repo/DebateSeason_Backend_V1.git
   ```

2. 환경 설정 파일 생성
   ```bash
   cd DebateSeason_Backend_V1/src/main/resources
   cp application-local.yml application-secret.yml
   ```

3. `application-secret.yml` 파일을 열어 데이터베이스 정보를 입력하세요. (아래 환경 설정 참고)


4. 프로젝트 디렉토리로 이동
   ```bash
   cd DebateSeason_Backend_V1
   ```

5. 애플리케이션 빌드
   ```bash
   ./gradlew build
   ```

4. 애플리케이션 실행
   ```bash
   ./gradlew bootRun
   ```

### 환경 설정

- `application-local.yml`: 로컬 개발 환경
- `application-dev.yml`: 개발 서버 환경
- `application-prod.yml`: 프로덕션 환경
- `application-test.yml`: 테스트 환경

로컬 환경에서 실행하려면 `src/main/resources` 디렉토리에 `application-secret.yml` 파일을 생성하고 다음 설정을 추가해야 합니다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create

  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/YOUR_DATABASE?characterEncoding=UTF-8&serverTimezone=Asia/Seoul
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
```

`YOUR_DATABASE`, `YOUR_USERNAME`, `YOUR_PASSWORD` 부분을 실제 MariaDB 정보로 변경하세요.

## 📃 API 문서

애플리케이션 실행 후 다음 URL에서 Swagger UI를 통해 API 문서를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html#/
```

## ♾️ CI/CD

GitHub Actions과 AWS를 통해 브랜치에 따라 자동 배포가 진행됩니다.

- 개발 환경: develop 브랜치에 코드가 병합되면, 빌드&테스트 후 개발 서버에 자동 배포
- 운영 환경: main 브랜치에 코드가 병합되면, 빌드&테스트 후 운영 서버에 자동 배포

## 👥 팀원 소개

팀원 정보는 추후 업데이트 예정입니다.