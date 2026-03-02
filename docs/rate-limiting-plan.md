# Rate Limiting 구현 계획

## 1. 배경 및 목적

Optional Authentication 아키텍처 도입으로 비인증 엔드포인트(Level 0, Level 1)가 늘어남에 따라, 봇/크롤러/악의적 트래픽 증가에 대비한 IP 및 사용자 기반 Rate Limiting이 필요하다.

### 현재 인프라 환경

- **서버**: AWS Lightsail 단일 인스턴스
- **Spring Boot 3.3.5** / **Java 17** / **Gradle**
- Rate Limiting 라이브러리 **미도입**
- `ErrorCode`에 이미 `TOO_MANY_REQUESTS(429)` HTTP 상태 패턴 존재 (`JWKS_RATE_LIMIT_REACHED`)
- 필터 체인: `JwtAuthenticationFilter` → `UsernamePasswordAuthenticationFilter`
- Redis 등 외부 캐시 **미사용** (순수 Spring Boot + MariaDB 구조)

### 인프라 특성에 따른 설계 방향

- Lightsail 단일 인스턴스이므로 **in-memory Rate Limiting**이 가장 적합
- ALB/WAF가 없으므로 **앱 레벨에서 직접 방어**해야 함
- 로드 밸런서 없이 단일 JVM이므로 인스턴스 간 동기화 불필요

---

## 2. 라이브러리 선택: Bucket4j

| 선택지 | 장점 | 단점 |
|--------|------|------|
| **Bucket4j** | Token Bucket 알고리즘, Spring 호환 우수, 버스트 허용 | 의존성 1개 추가 |
| 직접 구현 (AtomicInteger) | 의존성 없음 | edge case 처리 직접 해야 함 |
| Resilience4j | Circuit Breaker 중심, Rate Limit은 부가 기능 | 설정 복잡 |
| Nginx 레벨 | 앱 코드 변경 없음 | 인증 상태별 분기 불가, Lightsail에서 별도 Nginx 구성 필요 |

**Bucket4j 선택 이유**: Token Bucket 알고리즘이 검증되어 있고, 인증 상태별 분기가 쉬움. 의존성은 `bucket4j-core` 하나만 추가.

> Caffeine Cache는 추가하지 않는다. 메모리 정리는 `ScheduledExecutorService`로 충분하며, 단일 인스턴스 환경에서 의존성을 최소화한다.

---

## 3. Rate Limit 정책

비로그인은 엔드포인트 인증 레벨(Public/Optional Auth)과 무관하게 **IP당 단일 버킷**으로 통합 운영한다.
동일 IP에서 `/api/v1/users/login`과 `/api/v1/issue`를 번갈아 호출해도 같은 버킷에서 차감된다.

| 구분 | 식별 키 | 제한 | 근거 |
|------|---------|------|------|
| **비로그인** | IP | 100 req/min | 봇/크롤러/브루트포스 방어 |
| **로그인** | userId | 300 req/min | 정상 사용자 여유 확보 |

- 로그인 사용자는 `userId` 기반 → IP 공유 환경(회사/학교)에서 불이익 없음
- 비로그인은 `IP` 기반 → 엔드포인트별 분리 시 오히려 총 허용량이 늘어나므로 통합

---

## 4. 아키텍처 설계

### 필터 위치

`RateLimitFilter`는 `JwtAuthenticationFilter` **뒤**에 위치시킨다.

```
요청 → JwtAuthenticationFilter → RateLimitFilter → Controller
                                      ↓
                               SecurityContext에 userId 있으면 → userId 버킷
                               없으면 → IP 버킷
                               초과 시 → 429 반환
```

**트레이드오프**:
- JWT 파싱 **뒤**에 두면 `SecurityContext`에서 인증 정보를 바로 가져올 수 있어 구현이 단순
- 대신 Rate Limit 초과 요청도 JWT 파싱을 거침
- JWT 파싱은 마이크로초 단위의 가벼운 연산이므로, 현재 Lightsail 단일 인스턴스 규모에서 실질적 성능 영향 없음

### 버킷 저장소

```
ConcurrentHashMap<String, Bucket>
  key: "ip:192.168.1.1" 또는 "user:12345"
  value: Bucket (토큰 버킷)
```

### 메모리 누수 방지

`ConcurrentHashMap`에 IP/userId 키가 무한히 쌓이는 문제를 `ScheduledExecutorService`로 해결한다.

- **주기**: 5분마다 실행
- **정리 대상**: 마지막 요청 후 10분 경과한 버킷 제거
- 별도 라이브러리(Caffeine) 없이 `ConcurrentHashMap` + 타임스탬프 기록으로 구현

```java
// 버킷과 마지막 접근 시간을 함께 관리
ConcurrentHashMap<String, BucketEntry> buckets;

record BucketEntry(Bucket bucket, long lastAccessedAt) {}

// 5분마다 만료된 엔트리 정리
scheduler.scheduleAtFixedRate(() -> {
    long now = System.currentTimeMillis();
    buckets.entrySet().removeIf(e -> now - e.getValue().lastAccessedAt() > EXPIRE_MILLIS);
}, 5, 5, TimeUnit.MINUTES);
```

### IP 추출

Lightsail은 직접 외부에 노출되거나 Lightsail 로드 밸런서 뒤에 위치할 수 있다.

```java
String ip = request.getHeader("X-Forwarded-For");
if (ip == null || ip.isBlank()) {
    ip = request.getRemoteAddr();
} else {
    ip = ip.split(",")[0].trim(); // 첫 번째 IP가 클라이언트
}
```

> **주의**: Lightsail 로드 밸런서를 사용하지 않고 직접 노출된 경우, `X-Forwarded-For` 헤더를 클라이언트가 위조할 수 있다. 운영 환경에서 프록시 구성을 확인한 뒤 신뢰할 헤더를 결정해야 한다.

---

## 5. 429 응답 형식

기존 `ErrorResponse` 패턴에 맞춤:

```json
{
  "status": 429,
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
}
```

- `Retry-After` 헤더를 포함하여 클라이언트가 재시도 시점을 알 수 있게 한다
- 응답은 `JwtAuthenticationErrorHandler`와 동일한 `writeErrorResponse` 패턴을 따름

---

## 6. 모니터링 및 로깅

Rate Limit 발동 시 로깅이 없으면 공격 탐지와 임계값 튜닝이 불가능하다.

- Rate Limit **초과 시**: `WARN` 레벨 로깅 (IP/userId, 요청 URI)
- 로그 예시: `Rate limit exceeded for ip:203.0.113.5, uri: /api/v1/issue`
- 운영 중 임계값 조정이 필요하면 `application-prod.yml` 수정 후 재배포

---

## 7. 제외 대상

| 대상 | 이유 |
|------|------|
| `/swagger-ui/**`, `/actuator/**` | 내부 개발/운영 도구 — prod에서 접근 제한 별도 처리 |
| `/ws-stomp/**` | WebSocket은 HTTP 필터 경로와 별개, 필요 시 STOMP `ChannelInterceptor`에서 별도 처리 |
| 특정 크롤러 (Googlebot 등) | `User-Agent` 기반 화이트리스트는 위조 가능 → IP 기반으로 통일 |

---

## 8. 구현 파일 목록

| # | 파일 | 변경 유형 | 내용 |
|---|------|----------|------|
| 1 | `build.gradle` | 수정 | `bucket4j-core` 의존성 추가 |
| 2 | `RateLimitFilter.java` | **신규** | `OncePerRequestFilter` 확장, 버킷 관리 및 429 응답 |
| 3 | `WebSecurityConfig.java` | 수정 | 필터 체인에 `RateLimitFilter` 등록 |
| 4 | `ErrorCode.java` | 수정 | `RATE_LIMIT_EXCEEDED` 에러 코드 추가 |
| 5 | `application.yml` | 수정 | Rate Limit 기본 설정값 추가 |
| 6 | `application-prod.yml` | 수정 | 운영 환경 Rate Limit 값 |

> 기존 계획의 `RateLimitConfig.java`는 별도 파일 없이 `RateLimitFilter` 내에서 `@Value`로 설정값을 주입받아 단순화한다.

---

## 9. 구현 순서

1. **의존성 추가** — `bucket4j-core` (build.gradle)
2. **에러 코드 추가** — `ErrorCode.RATE_LIMIT_EXCEEDED`
3. **필터 구현** — `RateLimitFilter` (핵심 로직 + 버킷 정리 스케줄러)
4. **필터 등록** — `WebSecurityConfig`에 필터 추가
5. **환경별 설정** — `application.yml`(기본값), `application-prod.yml`(운영값)
6. **테스트** — 비로그인/로그인 각각 임계값 초과 시 429 확인

---

## 10. 향후 확장 시 고려사항

현재 Lightsail 단일 인스턴스에서 다중 인스턴스로 확장할 경우:

- **in-memory → Redis 전환** 필요 (`bucket4j-redis` 또는 `bucket4j-redisson`)
- 인스턴스별 독립 버킷이면 Rate Limit이 인스턴스 수만큼 배로 허용됨
- Redis 도입 시 `ConcurrentHashMap`을 Redis ProxyManager로 교체하면 `RateLimitFilter` 나머지 로직은 동일
