# Backend 편집 이력

## 2026-02-27: Optional Authentication 아키텍처 구현

### 목적
토론철 서비스를 웹(Next.js)에 확장하기 위해, 기존 모든 API가 JWT 필수인 구조를 **3-Tier 인증 레벨**(Public / Optional Auth / Required Auth)로 전환.
비로그인 사용자가 이슈/토론을 열람할 수 있게 하면서, 채팅/투표 등 참여 시에만 로그인을 요구한다.
기존 모바일 앱은 항상 토큰을 보내므로 영향 없음.

### 인증 흐름 변경

```
[변경 전]
Public URL? → Yes → skip (인증 없이 통과)
             → No  → 토큰 검증 (없으면 401)

[변경 후]
Public URL?     → Yes → skip (인증 없이 통과)
                → No  → Optional Auth URL? → Yes → 토큰 있으면 파싱, 없으면 anonymous 통과
                                            → No  → 토큰 검증 (없으면 401)
```

### 변경 파일 목록 (8개)

| # | 파일 | 변경 유형 |
|---|------|----------|
| 1 | `WebSecurityConfig.java` | Security 설정 |
| 2 | `SecurityPathMatcher.java` | URL 매칭 |
| 3 | `JwtAuthenticationFilter.java` | 필터 분기 |
| 4 | `IssueControllerV1.java` | 컨트롤러 null-safe |
| 5 | `IssueServiceV1.java` | 서비스 null-safe |
| 6 | `ChatRoomControllerV1.java` | 컨트롤러 null-safe |
| 7 | `ChatRoomServiceV1.java` | 서비스 null-safe |
| 8 | `UserControllerV1.java` | dead code 제거 |

---

### 1. WebSecurityConfig.java

**경로:** `src/main/java/com/debateseason_backend_v1/config/WebSecurityConfig.java`

**변경 내용:**
- `OPTIONAL_AUTH_URLS` 상수 배열 추가 (6개 URL 패턴)
- `filterChain()` 내 `authorizeHttpRequests`에 `.requestMatchers(OPTIONAL_AUTH_URLS).permitAll()` 추가

**추가된 코드:**
```java
public static final String[] OPTIONAL_AUTH_URLS = {
    "/api/v1/issue",
    "/api/v1/issue-map",
    "/api/v1/home/recommend",
    "/api/v1/home/media",
    "/api/v1/room",
    "/api/v1/users/home"
};
```

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(PUBLIC_URLS).permitAll()
    .requestMatchers(OPTIONAL_AUTH_URLS).permitAll()   // 신규
    .anyRequest().authenticated()
)
```

**설계 의도:**
- Spring Security 레벨에서 Optional Auth URL들을 `permitAll()`로 설정하여 SecurityFilterChain이 401을 내지 않도록 함
- 실제 인증 로직은 `JwtAuthenticationFilter`에서 처리

---

### 2. SecurityPathMatcher.java

**경로:** `src/main/java/com/debateseason_backend_v1/security/component/SecurityPathMatcher.java`

**변경 내용:**
- `isOptionalAuthUrl(String requestURI, String method)` 메서드 추가
- 기존 `isPublicUrl()` 내 context path 제거 로직을 `removeContextPath()` private 메서드로 추출하여 재사용
- import문을 `WebSecurityConfig.*` 와일드카드에서 `OPTIONAL_AUTH_URLS`, `PUBLIC_URLS` 명시적 import로 변경

**추가된 메서드:**
```java
public boolean isOptionalAuthUrl(String requestURI, String method) {
    String path = removeContextPath(requestURI);

    // /api/v1/room은 GET만 Optional, POST는 Required Auth
    if (pathMatcher.match("/api/v1/room", path)) {
        return "GET".equalsIgnoreCase(method);
    }

    return Arrays.stream(OPTIONAL_AUTH_URLS)
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
}

private String removeContextPath(String requestURI) {
    if (!contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
        return requestURI.substring(contextPath.length());
    }
    return requestURI;
}
```

**리팩터링된 기존 메서드:**
```java
// Before: context path 제거 로직이 isPublicUrl() 안에 인라인
// After: removeContextPath() 호출로 단순화
public boolean isPublicUrl(String requestURI) {
    String path = removeContextPath(requestURI);
    return Arrays.stream(PUBLIC_URLS)
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
}
```

**설계 의도:**
- `/api/v1/room`은 GET(채팅방 조회)만 Optional Auth, POST(채팅방 생성)는 Required Auth이므로 HTTP Method도 검사
- 나머지 Optional Auth URL은 method 무관하게 Optional Auth 적용

---

### 3. JwtAuthenticationFilter.java

**경로:** `src/main/java/com/debateseason_backend_v1/security/jwt/JwtAuthenticationFilter.java`

**변경 내용:**
- `doFilterInternal()` 메서드에 Optional Auth URL 분기 추가 (Public URL 체크 직후, Required Auth 체크 직전)
- `tryOptionalAuthentication(HttpServletRequest request)` private 메서드 추가

**doFilterInternal() 내 추가된 분기 (L48-53):**
```java
// Optional Auth: 토큰 있으면 파싱, 없으면 anonymous로 통과
if (securityPathMatcher.isOptionalAuthUrl(requestURI, request.getMethod())) {
    tryOptionalAuthentication(request);
    filterChain.doFilter(request, response);
    return;
}
```

**추가된 메서드 (L130-144):**
```java
private void tryOptionalAuthentication(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
    if (!containsValidHeader(authorizationHeader)) {
        return;  // 토큰 없으면 anonymous로 통과
    }
    String token = removeBearerPrefix(authorizationHeader);
    if (token == null) {
        return;  // 형식 잘못되어도 anonymous로 통과
    }
    try {
        authenticateWithAccessToken(token, request.getRequestURI());
    } catch (Exception e) {
        // 만료/잘못된 토큰이어도 에러를 내지 않고 anonymous로 통과
        log.debug("Optional auth failed, continuing as anonymous: {}", e.getMessage());
    }
}
```

**설계 의도:**
- 토큰이 있으면 기존 `authenticateWithAccessToken()`으로 SecurityContext에 인증 정보 설정
- 토큰이 없거나 유효하지 않으면 에러 없이 anonymous로 통과 → 컨트롤러에서 `@AuthenticationPrincipal`이 null이 됨
- 기존 Required Auth 흐름은 전혀 변경하지 않음

---

### 4. IssueControllerV1.java

**경로:** `src/main/java/com/debateseason_backend_v1/domain/issue/presentation/controller/IssueControllerV1.java`

**변경 내용:**
- `getIssue()` (GET /api/v1/issue): principal null-safe 처리
- `getRecommend()` (GET /api/v1/home/recommend): principal null-safe 처리

**변경 전 (getIssue, L51):**
```java
Long userId = principal.getUserId();  // principal이 null이면 NPE
```

**변경 후 (getIssue, L51):**
```java
Long userId = principal != null ? principal.getUserId() : null;
```

**변경 전 (getRecommend, L91):**
```java
Long userId = principal.getUserId();  // principal이 null이면 NPE
```

**변경 후 (getRecommend, L91):**
```java
Long userId = principal != null ? principal.getUserId() : null;
```

**변경하지 않은 메서드:**
- `indexPage()` (GET /home/refresh): Required Auth 유지 (반드시 userId 필요)
- `bookMarkIssue()` (POST /bookmark): Required Auth 유지 (사용자 행위)

---

### 5. IssueServiceV1.java

**경로:** `src/main/java/com/debateseason_backend_v1/domain/issue/application/service/IssueServiceV1.java`

**변경 내용:**
`fetchV2(Long issueId, Long userId, Long ChatRoomId)` 메서드 내 userId가 null인 경우 분기 처리

**변경된 로직 (3개 블록):**

**(1) 북마크 상태 조회 (L92-100):**
```java
// 변경 전: 무조건 userIssueRepository.findByIssueIdAndUserId(issueId, userId) 호출
// 변경 후: userId가 null이면 조회 스킵, 기본값 "no" 유지
String bookMarkState = "no";
if (userId != null) {
    List<Object[]> object = userIssueRepository.findByIssueIdAndUserId(issueId, userId);
    if (!object.isEmpty()) {
        Object[] object2 = object.get(0);
        bookMarkState = (String)object2[0];
    }
}
```

**(2) 프로필 조회 & 커뮤니티 기록 (L110-126):**
```java
// 변경 전: profileRepository.findByUserId(userId) → userId가 null이면 NPE 핵심 원인
// 변경 후: userId가 null이면 프로필 조회/커뮤니티 기록 전체 스킵
if (userId != null) {
    ProfileEntity profile = profileRepository.findByUserId(userId).orElseThrow(...);
    CommunityType communityType = profile.getCommunityType();
    if (communityType == null) {
        throw new CustomException(ErrorCode.NOT_FOUND_COMMUNITY);
    }
    UserDTO userDTO = new UserDTO();
    userDTO.setCommunity(communityType.getName());
    userDTO.setId(userId);
    communityMananger.record(userDTO, issueId);
}
// getSortedCommunity()는 항상 호출 (기존 데이터 반환)
LinkedHashMap<String, Integer> sortedMap = communityMananger.getSortedCommunity(issueId);
```

**(3) 사용자 투표 상태 조회 (L140-144):**
```java
// 변경 전: 무조건 userChatRoomRepository.findUserChatRoomOpinions(userId, chatRoomIds) 호출
// 변경 후: 로그인 사용자만 개인 투표 상태 조회
if (userId != null) {
    List<Object[]> opinions = userChatRoomRepository.findUserChatRoomOpinions(userId, chatRoomIds);
    markUserOpinion(opinions, chatRooms);
}
```

**비로그인 사용자 응답 결과:**
- `bookMarkState`: "no" (기본값)
- `map` (커뮤니티 맵): 기존 데이터만 (방문 기록 추가 없음)
- `chatRoomMap`의 각 채팅방 `opinion`: NEUTRAL (기본값, markUserOpinion 미호출)

---

### 6. ChatRoomControllerV1.java

**경로:** `src/main/java/com/debateseason_backend_v1/domain/chatroom/controller/ChatRoomControllerV1.java`

**변경 내용:**
- `fetch()` (GET /api/v1/room): principal null-safe 처리

**변경 전 (L64):**
```java
Long userId = principal.getUserId();  // principal이 null이면 NPE
```

**변경 후 (L64):**
```java
Long userId = principal != null ? principal.getUserId() : null;
```

**변경하지 않은 메서드:**
- `save()` (POST /room): `@AuthenticationPrincipal` 미사용 — 변경 불필요
- `vote()` (POST /room/vote): Required Auth 유지 (사용자 행위)

---

### 7. ChatRoomServiceV1.java

**경로:** `src/main/java/com/debateseason_backend_v1/domain/chatroom/service/ChatRoomServiceV1.java`

**변경 내용 (2개 메서드):**

**(1) fetch() — 채팅방 단건 조회 (L140-146):**
```java
// 변경 전: userId가 null이면 userChatRoomRepository.findByUserIdAndChatRoomId()에서 문제 발생 가능
String opinion = Optional.ofNullable(userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId))
    .map(UserChatRoom::getOpinion)
    .orElse(Opinion.NEUTRAL.name());

// 변경 후: userId가 null이면 조회 스킵, NEUTRAL 기본값 유지
String opinion = Opinion.NEUTRAL.name();
if (userId != null) {
    opinion = Optional.ofNullable(userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId))
        .map(UserChatRoom::getOpinion)
        .orElse(Opinion.NEUTRAL.name());
}
```

**(2) findVotedChatRoom() — 투표한 채팅방 목록 (L341-359):**
```java
// 추가된 early return 블록: userId가 null이면 공개 데이터만 반환
if (userId == null) {
    List<Top5BestChatRoom> top5BestChatRooms = chatRoomProcessor.getTop5ActiveRooms();
    List<IssueBriefResponse> top5BestIssueRooms = issueManager.findTop5BestIssueRooms();

    UserVotedChatRoom userVotedChatRoom = UserVotedChatRoom.builder()
        .breakingNews(breakingNews)
        .chatRoomResponse(List.of())       // 빈 리스트 (투표 기록 없음)
        .top5BestChatRooms(top5BestChatRooms)
        .top5BestIssueRooms(top5BestIssueRooms)
        .build();

    return ApiResult.<UserVotedChatRoom>builder()
        .status(200)
        .code(ErrorCode.SUCCESS)
        .message("채팅방을 불러왔습니다.")
        .data(userVotedChatRoom)
        .build();
}
```

**비로그인 사용자 응답 결과:**
- `fetch()`: opinion = "NEUTRAL", 나머지 채팅방 데이터(찬성/반대 수, 팀 스코어 등)는 정상 반환
- `findVotedChatRoom()`: breakingNews + top5BestChatRooms + top5BestIssueRooms는 반환, chatRoomResponse는 빈 리스트

---

### 8. UserControllerV1.java

**경로:** `src/main/java/com/debateseason_backend_v1/domain/user/presentation/controller/UserControllerV1.java`

**변경 내용:**
- `indexPage()` (GET /api/v1/users/home): 사용하지 않는 `userId` 추출 코드 제거

**변경 전 (L70-80):**
```java
@GetMapping("/home")
public ApiResult<List<IssueBriefResponse>> indexPage(
    //@RequestParam(name = "page", required = false) Long page,
    @AuthenticationPrincipal CustomUserDetails principal
) {
    Long userId = principal.getUserId();  // 추출하지만 사용 안 함 → dead code
    //return chatRoomServiceV1.findVotedChatRoom(userId,page);
    return issueServiceV1.fetchV1();
}
```

**변경 후 (L69-74):**
```java
@GetMapping("/home")
public ApiResult<List<IssueBriefResponse>> indexPage(
    @AuthenticationPrincipal CustomUserDetails principal
) {
    return issueServiceV1.fetchV1();  // userId 미사용이므로 추출 불필요
}
```

**설계 의도:**
- `principal` 파라미터는 유지 (향후 개인화 용도 + interface 호환성)
- `userId` 추출 코드만 제거하여 principal이 null이어도 안전

---

### 변경하지 않은 엔드포인트

| 엔드포인트 | 이유 |
|-----------|------|
| `GET /api/v1/issue-map` | `@AuthenticationPrincipal` 미사용 — Security 설정만 열면 됨 |
| `GET /api/v1/home/media` | `@AuthenticationPrincipal` 미사용 — Security 설정만 열면 됨 |
| `GET /api/v1/home/refresh` | "내가 투표한 채팅방" → 반드시 userId 필요 → Required Auth 유지 |
| `POST /api/v1/bookmark` | 사용자 행위 → Required Auth 유지 |
| `POST /api/v1/room/vote` | 사용자 행위 → Required Auth 유지 |
| `POST /api/v1/room` | `@AuthenticationPrincipal` 미사용 — 변경 불필요 |

---

### 빌드 결과

- **컴파일**: `./gradlew build -x test` → BUILD SUCCESSFUL
- **테스트**: `./gradlew test` → 60개 중 58개 통과, 2개 실패
  - 실패한 테스트: `ChatApiTest.채팅메시지_조회_성공()`, `ChatWebSocketTest.채팅메시지_전송_및_수신_성공()`
  - 이번 변경과 무관한 기존 실패 (chat 도메인 통합 테스트)

### 엔드포인트별 인증 레벨 최종 정리

| 인증 레벨 | 엔드포인트 | 비로그인 접근 |
|-----------|-----------|-------------|
| **Public** | `/api/v1/users/login`, `/api/v1/auth/reissue`, `/api/v1/app/**` 등 | O |
| **Optional Auth** | `GET /api/v1/issue` | O (개인화 데이터 제외) |
| **Optional Auth** | `GET /api/v1/issue-map` | O |
| **Optional Auth** | `GET /api/v1/home/recommend` | O (투표 채팅방 빈 리스트) |
| **Optional Auth** | `GET /api/v1/home/media` | O |
| **Optional Auth** | `GET /api/v1/room` | O (opinion=NEUTRAL) |
| **Optional Auth** | `GET /api/v1/users/home` | O |
| **Required Auth** | `GET /api/v1/home/refresh` | X (401) |
| **Required Auth** | `POST /api/v1/bookmark` | X (401) |
| **Required Auth** | `POST /api/v1/room/vote` | X (401) |
| **Required Auth** | `POST /api/v1/room` | X (401) |

---

## 2026-03-01: Rate Limiting 구현

### 목적
AWS Lightsail 단일 인스턴스 환경에서 API 남용 방지를 위한 Rate Limiting 적용.
Bucket4j 기반 in-memory Token Bucket 알고리즘으로 비로그인 IP당 100 req/min, 로그인 userId당 300 req/min 제한.

### Rate Limiting 흐름

```
[필터 체인 순서]
JwtAuthenticationFilter → RateLimitFilter → AuthorizationFilter

[RateLimitFilter 내부 흐름]
제외 URL? (/swagger-ui/**, /actuator/**, /ws-stomp/**)
  → Yes → skip (Rate Limit 미적용)
  → No  → SecurityContext에 인증 정보 있음?
            → Yes → "user:{userId}" 키로 300 req/min 버킷 적용
            → No  → "ip:{clientIp}" 키로 100 req/min 버킷 적용
                     → 토큰 소비 성공? → Yes → 다음 필터로 진행
                                        → No  → 429 + Retry-After 헤더 + JSON 에러 응답
```

### 의사결정

**1. JwtAuthenticationFilter 뒤에 배치하는 이유**
- `SecurityContext`에 인증 정보가 세팅된 후여야 로그인/비로그인을 구분 가능
- JwtAuthenticationFilter가 먼저 실행되어 Optional Auth URL에서도 토큰이 있으면 인증 정보가 세팅됨
- 인증된 사용자는 userId 기반 버킷, 비인증 사용자는 IP 기반 버킷으로 분리

**2. Bucket4j in-memory 선택 이유**
- AWS Lightsail 단일 인스턴스 환경 → Redis 등 외부 저장소 불필요
- `ConcurrentHashMap<String, BucketEntry>` + `ScheduledExecutorService`로 단순하게 구현
- 다중 인스턴스 확장 시 Redis 기반으로 전환 가능 (Bucket4j-redis 모듈 존재)

**3. Caffeine 미사용 이유**
- 의존성 최소화: Caffeine 추가 없이 `ScheduledExecutorService`로 5분마다 만료 엔트리(10분 미접근) 정리
- `BucketEntry` 내부 클래스로 `lastAccessTime`을 `volatile`로 관리하여 thread-safe 보장

**4. 설정값 외부화 (`@Value` + YAML)**
- `RateLimitFilter`는 `new`로 직접 인스턴스화 (Spring 관리 Bean 아님) → filter 내부에서 `@Value` 사용 불가
- `WebSecurityConfig`에서 `@Value`로 주입받아 생성자 파라미터로 전달
- `application.yml`과 `application-prod.yml`에 분리하여 운영 모니터링 후 독립 조정 가능

**5. IP 추출 시 `X-Forwarded-For` 우선 사용**
- Lightsail 로드밸런서/리버스 프록시 뒤에서 실제 클라이언트 IP를 얻기 위함
- `X-Forwarded-For` 헤더의 첫 번째 값(원본 클라이언트 IP)을 사용

**6. 에러 응답 패턴 통일**
- `JwtAuthenticationErrorHandler.writeErrorResponse()`와 동일한 패턴으로 429 응답 작성
- `ObjectMapper` → `ErrorResponse` JSON 직렬화 → `response.setStatus()`, `setContentType()`, `setCharacterEncoding()`, `getWriter().write()`

### 변경 파일 목록 (5개 수정 + 1개 신규)

| # | 파일 | 변경 유형 |
|---|------|----------|
| 1 | `build.gradle` | 의존성 추가 |
| 2 | `ErrorCode.java` | 에러 코드 추가 |
| 3 | `RateLimitFilter.java` | **신규 생성** |
| 4 | `WebSecurityConfig.java` | 필터 등록 + 설정 주입 |
| 5 | `application.yml` | 설정값 추가 |
| 6 | `application-prod.yml` | 설정값 추가 |

---

### 1. build.gradle

**경로:** `build.gradle`

**변경 내용:**
- `com.bucket4j:bucket4j-core:8.10.1` 의존성 추가

**추가된 코드:**
```gradle
// Rate Limiting
implementation 'com.bucket4j:bucket4j-core:8.10.1'
```

---

### 2. ErrorCode.java

**경로:** `src/main/java/com/debateseason_backend_v1/common/exception/ErrorCode.java`

**변경 내용:**
- 8000번대 Rate Limiting 에러 코드 추가 (기존: 7000번대 동시성)

**추가된 코드:**
```java
// 8000번대: Rate Limiting 에러
RATE_LIMIT_EXCEEDED(8000, HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),
```

---

### 3. RateLimitFilter.java (신규)

**경로:** `src/main/java/com/debateseason_backend_v1/security/filter/RateLimitFilter.java`

**설계 패턴:**
- `OncePerRequestFilter` 확장 (JwtAuthenticationFilter와 동일 패턴)
- Spring Bean이 아닌 직접 인스턴스화 → 생성자로 의존성 주입

**핵심 구조:**

**(1) 버킷 키 결정 (L88-99):**
```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
if (isAuthenticated(authentication)) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    bucketKey = "user:" + userDetails.getUserId();
    rateLimit = authenticatedRateLimit;
} else {
    bucketKey = "ip:" + resolveClientIp(request);
    rateLimit = anonymousRateLimit;
}
```
- `isAuthenticated()`: `authentication != null && isAuthenticated() && principal instanceof CustomUserDetails`로 3중 체크
- 로그인 사용자 → `"user:{userId}"`, 비로그인 → `"ip:{clientIp}"`

**(2) Token Bucket 소비 (L101-113):**
```java
BucketEntry entry = buckets.computeIfAbsent(bucketKey, k -> new BucketEntry(createBucket(rateLimit)));
entry.updateLastAccess();

ConsumptionProbe probe = entry.getBucket().tryConsumeAndReturnRemaining(1);

if (!probe.isConsumed()) {
    long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;
    log.warn("Rate limit exceeded for key: {}, retry after: {}s", bucketKey, retryAfterSeconds);
    response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
    writeErrorResponse(response);
    return;
}
```
- `computeIfAbsent`로 키별 버킷 lazy 생성
- `tryConsumeAndReturnRemaining(1)`로 토큰 1개 소비 시도
- 초과 시 `Retry-After` 헤더에 대기 시간(초) 포함, WARN 로깅

**(3) Bucket4j 8.10+ API 사용 (L142-146):**
```java
private Bucket createBucket(long capacity) {
    return Bucket.builder()
        .addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, Duration.ofMinutes(1)))
        .build();
}
```
- deprecated된 `Bandwidth.simple()` 대신 lambda builder API 사용
- `refillGreedy`: 1분마다 capacity만큼 한번에 리필 (greedy 전략)

**(4) 만료 엔트리 정리 (L159-175):**
```java
private void cleanupExpiredEntries() {
    long now = System.currentTimeMillis();
    var iterator = buckets.entrySet().iterator();
    while (iterator.hasNext()) {
        var entry = iterator.next();
        if (now - entry.getValue().getLastAccessTime() > ENTRY_EXPIRATION_MILLIS) {
            iterator.remove();
        }
    }
}
```
- `ScheduledExecutorService` daemon 스레드가 5분 간격으로 실행
- 10분 미접근 엔트리 제거 → 메모리 누수 방지

**(5) BucketEntry 내부 클래스 (L177-197):**
```java
private static class BucketEntry {
    private final Bucket bucket;
    private volatile long lastAccessTime;  // volatile: 멀티스레드 가시성 보장
    // ...
}
```

**(6) 제외 URL (L34-38, L124-132):**
```java
private static final String[] EXCLUDED_PATHS = {
    "/swagger-ui/**", "/actuator/**", "/ws-stomp/**"
};
```
- `AntPathMatcher`로 패턴 매칭 (SecurityPathMatcher와 동일 방식)
- Swagger, Actuator, WebSocket은 Rate Limiting 미적용

---

### 4. WebSecurityConfig.java

**경로:** `src/main/java/com/debateseason_backend_v1/config/WebSecurityConfig.java`

**변경 내용:**
- `ObjectMapper` 의존성 추가 (기존 `@RequiredArgsConstructor`에 final 필드로 추가)
- `@Value`로 rate-limit 설정값 주입
- `RateLimitFilter` 인스턴스 생성 및 `JwtAuthenticationFilter` 뒤에 등록

**추가된 필드:**
```java
private final ObjectMapper objectMapper;

@Value("${rate-limit.anonymous-requests-per-minute:100}")
private long anonymousRateLimit;

@Value("${rate-limit.authenticated-requests-per-minute:300}")
private long authenticatedRateLimit;
```

**변경된 filterChain() 메서드:**
```java
// 변경 전: 인라인 생성
.addFilterBefore(
    new JwtAuthenticationFilter(jwtUtil, errorHandler, securityPathMatcher),
    UsernamePasswordAuthenticationFilter.class
)

// 변경 후: 변수 추출 + RateLimitFilter 추가
JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil, errorHandler, securityPathMatcher);
RateLimitFilter rateLimitFilter = new RateLimitFilter(
    securityPathMatcher, objectMapper, anonymousRateLimit, authenticatedRateLimit
);

// ...
.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
.addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class)
```

**설계 의도:**
- `addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class)`: JWT 인증이 먼저 실행된 후 Rate Limiting 적용
- 최종 필터 순서: `JwtAuthenticationFilter` → `RateLimitFilter` → `UsernamePasswordAuthenticationFilter` (비활성)

---

### 5. application.yml

**경로:** `src/main/resources/application.yml`

**추가된 설정:**
```yaml
rate-limit:
  anonymous-requests-per-minute: 100
  authenticated-requests-per-minute: 300
```

---

### 6. application-prod.yml

**경로:** `src/main/resources/application-prod.yml`

**추가된 설정:**
```yaml
rate-limit:
  anonymous-requests-per-minute: 100
  authenticated-requests-per-minute: 300
```

**설계 의도:**
- 초기에는 동일값이지만, 운영 모니터링 후 독립 조정 가능하도록 분리

---

### 빌드 결과

- **컴파일**: `./gradlew build -x test` → BUILD SUCCESSFUL (deprecation 경고 없음)
- **테스트**: `./gradlew test` → 60개 중 58개 통과, 2개 실패
  - 실패한 테스트: `ChatApiTest.채팅메시지_조회_성공()`, `ChatWebSocketTest.채팅메시지_전송_및_수신_성공()`
  - 이번 변경과 무관한 기존 실패 (chat 도메인 통합 테스트)

### Rate Limiting 적용 범위 정리

| 대상 | Rate Limit | 버킷 키 |
|------|-----------|---------|
| 비로그인 사용자 | 100 req/min | `ip:{clientIp}` |
| 로그인 사용자 | 300 req/min | `user:{userId}` |
| Swagger / Actuator / WebSocket | 미적용 (제외) | — |

### 429 응답 예시

```json
{
  "status": 429,
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
}
```
- `Retry-After` 헤더: 버킷 리필까지 남은 초(+1) 포함

---

## 2026-03-02: Chat 통합 테스트 FK 제약조건 위반 수정

### 목적
`ChatApiTest.채팅메시지_조회_성공()`과 `ChatWebSocketTest.채팅메시지_전송_및_수신_성공()` 2개 테스트가 H2 테스트 DB에서 FK 제약조건 위반으로 실패하던 문제 해결.
기존에 `new ChatRoom(); setId(1L)`로 메모리상 객체만 만들고 실제 DB에 INSERT하지 않아 발생.

### 근본 원인

```
ChatEntity → FK(chat_room_id) → ChatRoom → FK(issue_id) → IssueEntity
```

테스트 코드에서 `ChatRoom(ID=1)`을 DB에 저장하지 않은 채 `ChatEntity`를 저장하려 하여:
- `ChatApiTest`: `chatRepository.save(chat)` 시점에 `DataIntegrityViolationException` (FK 위반)
- `ChatWebSocketTest`: `chatRoomService.findChatRoomById(1L)` 조회 실패 → 메시지 브로드캐스트 안 됨 → `messageQueue.poll()` 타임아웃 → `assertNotNull` 실패

### 변경 파일 목록 (2개)

| # | 파일 | 변경 유형 |
|---|------|----------|
| 1 | `ChatApiTest.java` | 테스트 셋업 수정 |
| 2 | `ChatWebSocketTest.java` | 테스트 셋업 수정 |

---

### 1. ChatApiTest.java

**경로:** `src/test/java/com/debateseason_backend_v1/integration/chat/ChatApiTest.java`

**변경 내용:**
- `IssueJpaRepository`, `ChatRoomRepository` 의존성 주입 추가
- `@BeforeEach`에서 `IssueEntity` → `ChatRoom` 순서로 DB 저장
- 하드코딩된 `roomId = 1L` 제거, `savedChatRoom.getId()` 사용
- `prepareTestChatMessages()` 파라미터를 `Long roomId` → `ChatRoom chatRoom`으로 변경
- 미사용 `ApplicationContext`, `createTestJwt()` 제거

**추가된 필드 및 @BeforeEach:**
```java
@Autowired
private IssueJpaRepository issueRepository;

@Autowired
private ChatRoomRepository chatRoomRepository;

private ChatRoom savedChatRoom;

@BeforeEach
void setup(){
    this.baseUrl = "http://localhost:" + port + "/api/v1/chat";

    // IssueEntity 저장 (ChatRoom의 FK 필수)
    IssueEntity issue = IssueEntity.builder()
            .title("테스트 이슈")
            .majorCategory("정치")
            .build();
    IssueEntity savedIssue = issueRepository.save(issue);

    // ChatRoom 저장 (ChatEntity의 FK 필수)
    savedChatRoom = ChatRoom.builder()
            .issueEntity(savedIssue)
            .title("테스트 채팅방")
            .content("테스트 채팅방 내용")
            .build();
    savedChatRoom = chatRoomRepository.save(savedChatRoom);
}
```

**변경된 테스트 메서드:**
```java
// 변경 전
Long roomId = 1L;
List<ChatEntity> chats = prepareTestChatMessages(roomId, messageCount);

// 변경 후
Long roomId = savedChatRoom.getId();
List<ChatEntity> chats = prepareTestChatMessages(savedChatRoom, messageCount);
```

**변경된 prepareTestChatMessages():**
```java
// 변경 전: 메모리상 ChatRoom 객체만 생성
private List<ChatEntity> prepareTestChatMessages(Long roomId, int count) {
    ChatRoom chatRoom = new ChatRoom();
    chatRoom.setId(roomId);
    // ...
}

// 변경 후: DB에 저장된 ChatRoom 객체를 직접 받음
private List<ChatEntity> prepareTestChatMessages(ChatRoom chatRoom, int count) {
    // ...
}
```

---

### 2. ChatWebSocketTest.java

**경로:** `src/test/java/com/debateseason_backend_v1/integration/chat/ChatWebSocketTest.java`

**변경 내용:**
- `IssueJpaRepository`, `ChatRoomRepository` 의존성 주입 추가
- `@BeforeEach`에서 `IssueEntity` → `ChatRoom` 순서로 DB 저장
- 하드코딩된 `roomId = 1L` 제거, `savedChatRoom.getId()` 사용

**추가된 필드 및 @BeforeEach 셋업:**
```java
@Autowired
private IssueJpaRepository issueRepository;

@Autowired
private ChatRoomRepository chatRoomRepository;

private ChatRoom savedChatRoom;

@BeforeEach
void setup(){
    // ... 기존 stompClient, WS_URL 설정 ...

    // IssueEntity 저장 (ChatRoom의 FK 필수)
    IssueEntity issue = IssueEntity.builder()
            .title("테스트 이슈")
            .majorCategory("정치")
            .build();
    IssueEntity savedIssue = issueRepository.save(issue);

    // ChatRoom 저장 (WebSocket 메시지 처리 시 DB 조회 필수)
    savedChatRoom = ChatRoom.builder()
            .issueEntity(savedIssue)
            .title("테스트 채팅방")
            .content("테스트 채팅방 내용")
            .build();
    savedChatRoom = chatRoomRepository.save(savedChatRoom);
}
```

**변경된 테스트 메서드:**
```java
// 변경 전
Long roomId = 1L;

// 변경 후
Long roomId = savedChatRoom.getId();
```

---

### 빌드 결과

- **컴파일**: `./gradlew build -x test` → BUILD SUCCESSFUL
- **테스트**: `./gradlew test` → **60개 전체 통과, 0개 실패**

---

## 2026-03-03: CI/CD 마이그레이션 ECS → Lightsail VM + 보안 강화

### 목적
AWS ECS 기반 Docker 배포에서 Lightsail VM 기반 JAR 직접 배포로 전환.
DEV 서버 폐기, PROD만 운영. 배포 안정성(자동 백업/롤백/헬스체크)과 보안(API 키 환경변수화) 강화.

### 배포 아키텍처 변경

```
[변경 전 - ECS]
GitHub Actions → Docker Build → ECR Push → ECS Task Definition 업데이트 → ECS 롤링 배포

[변경 후 - Lightsail VM]
GitHub Actions → Gradle Build → SCP jar 전송 → SSH: 백업 → swap → systemd restart → 헬스체크 → 실패 시 자동 롤백
```

### 서버 인프라 구조

```
클라이언트 → :80 Nginx (HTTP→HTTPS 301) → :443 Nginx (SSL termination) → :8080 Spring Boot (systemd)
```

### 변경 파일 목록 (6개 수정 + 2개 신규)

| # | 파일 | 변경 유형 |
|---|------|----------|
| 1 | `PROD_CICD.yml` | deploy job 전면 교체 (ECS → SSH/SCP) |
| 2 | `DEV_CICD.yml` | deploy job 제거 (build + test만 유지) |
| 3 | `application-prod.yml` | server.port 80→8080, YouTube API 키 환경변수화 |
| 4 | `.gitignore` | *.pem, *.key, .env 추가 |
| 5 | `lightsail-deployment-guide.md` | **신규** — 서버 초기 설정 가이드 |
| 6 | `lightsail-deployment-review.md` | **신규** — 운영 배포 실행 계획 |

---

### 1. PROD_CICD.yml

**경로:** `.github/workflows/PROD_CICD.yml`

**변경 내용:**
- ECS 배포 관련 스텝 전체 제거 (Docker Build, ECR Push, Task Definition, ECS Deploy)
- SSH/SCP 기반 Lightsail 배포로 교체
- 자동 백업, 헬스체크, 롤백 로직 포함

**제거된 스텝:**
- Configure AWS credentials (OIDC role assume)
- Login to Amazon ECR
- Set up QEMU / Docker Buildx
- Build and push (Docker multi-platform)
- Download Task Definition Template
- Fill in the new image ID
- Deploy Amazon ECS task definition

**추가된 스텝 (4개):**

**(1) Setup SSH key:**
```yaml
- name: Setup SSH key
  run: |
    mkdir -p ~/.ssh
    echo "${{ secrets.LIGHTSAIL_SSH_PRIVATE_KEY }}" > ~/.ssh/lightsail_key
    chmod 600 ~/.ssh/lightsail_key
    ssh-keyscan -H ${{ secrets.LIGHTSAIL_HOST }} >> ~/.ssh/known_hosts
```

**(2) Transfer jar to Lightsail:**
```yaml
- name: Transfer jar to Lightsail
  run: |
    JAR_FILE=$(ls build/libs/*.jar | grep -v plain | head -1)
    scp -i ~/.ssh/lightsail_key "$JAR_FILE" ubuntu@${{ secrets.LIGHTSAIL_HOST }}:/home/ubuntu/app/app.jar.new
```
- `grep -v plain`: Spring Boot의 plain jar 제외, 실행 가능한 fat jar만 전송

**(3) Deploy and health check:**
```yaml
- name: Deploy and health check
  run: |
    ssh -T -i ~/.ssh/lightsail_key ubuntu@${{ secrets.LIGHTSAIL_HOST }} 'bash -s' << 'DEPLOY_SCRIPT'
    APP_DIR=/home/ubuntu/app
    JAR=$APP_DIR/app.jar
    JAR_NEW=$APP_DIR/app.jar.new
    JAR_BACKUP=$APP_DIR/app.jar.backup

    # Backup current jar
    if [ -f "$JAR" ]; then
      cp "$JAR" "$JAR_BACKUP"
    fi

    # Atomic swap
    mv "$JAR_NEW" "$JAR"

    # Restart service
    sudo systemctl restart toronchul

    # Health check: 12 attempts, 5 seconds apart (60s total)
    echo "Waiting for application to start..."
    DEPLOY_SUCCESS=false
    for i in $(seq 1 12); do
      sleep 5
      if curl -sf http://localhost:8080/prod/actuator/health > /dev/null 2>&1; then
        echo "Health check passed (attempt $i)"
        DEPLOY_SUCCESS=true
        break
      fi
      echo "Health check attempt $i/12 failed, retrying..."
    done

    if [ "$DEPLOY_SUCCESS" = true ]; then
      echo "Deploy completed successfully"
      exit 0
    fi

    # Health check failed — rollback
    echo "Health check failed after 60 seconds. Rolling back..."
    if [ -f "$JAR_BACKUP" ]; then
      mv "$JAR_BACKUP" "$JAR"
      sudo systemctl restart toronchul
      echo "Rollback complete."
    fi
    exit 1
    DEPLOY_SCRIPT
```

**설계 결정:**
- `ssh -T`: pseudo-terminal 할당 방지 (heredoc 사용 시 필수)
- `bash -s`: 명시적 셸 실행으로 exit code 전파 보장
- `break` + 플래그 변수: for 루프 내 `exit 0` 대신 사용 — SSH heredoc에서 exit code가 정상 전파되지 않는 문제 해결
- `mv` (atomic swap): `cp` 대비 파일 교체 중 불완전 상태 방지
- 12회 × 5초 = 60초: Spring Boot 기동 시간 고려한 헬스체크 타임아웃

**(4) Cleanup SSH key:**
```yaml
- name: Cleanup SSH key
  if: always()
  run: rm -f ~/.ssh/lightsail_key
```
- `if: always()`: deploy 실패해도 키 파일 반드시 삭제

---

### 2. DEV_CICD.yml

**경로:** `.github/workflows/DEV_CICD.yml`

**변경 내용:**
- ECS deploy job 전체 제거 (77줄)
- build + test job만 유지

**제거된 job:**
```yaml
deploy:
  permissions:
    id-token: write
    contents: read
  needs: [ build, test ]
  if: github.event_name == 'push' && github.ref == 'refs/heads/develop'
  # ... ECR login, Docker build, ECS deploy 등 전체 삭제
```

**설계 의도:**
- DEV 서버 폐기에 따라 develop 브랜치 push 시 배포하지 않음
- CI(빌드/테스트) 기능은 유지하여 PR 검증용으로 활용

---

### 3. application-prod.yml

**경로:** `src/main/resources/application-prod.yml`

**변경 내용 (2개):**

**(1) server.port 변경:**
```yaml
# 변경 전
server:
  port: 80

# 변경 후
server:
  port: 8080
```

**변경 이유:**
- Lightsail 서버에 Nginx가 :80/:443을 점유하고 `proxy_pass http://localhost:8080`으로 프록시
- Spring Boot가 :80에서 직접 리슨하면 Nginx와 포트 충돌

**(2) YouTube API 키 환경변수화:**
```yaml
# 변경 전
youtube_live-api-key: "AIzaSyCdEG_MS81NpdlsAJOQwmzS21u7L_K-r0M"

# 변경 후
youtube_live-api-key: ${YOUTUBE_API_KEY}
```

**변경 이유:**
- API 키가 소스코드에 평문 노출 — git history에 남아있으므로 키 rotate 권장
- `.env` 파일에 `YOUTUBE_API_KEY=...`로 관리

---

### 4. .gitignore

**경로:** `.gitignore`

**추가된 규칙:**
```gitignore
### Secrets & Keys ###
*.pem
*.key
.env
```

**변경 이유:**
- SSH 키 파일(`.pem`)이 프로젝트 루트에 존재했음 → `~/.ssh/`로 이동 후 gitignore 추가
- `.env` 파일이 실수로 커밋되는 것 방지

---

### 5. lightsail-deployment-guide.md (신규)

**경로:** `docs/lightsail-deployment-guide.md`

서버 1회 초기 설정 가이드:
- 앱 디렉토리 생성 (`/home/ubuntu/app`)
- `.env` 환경변수 파일 생성 (DB, JWT, YouTube API 키 등 전체 목록)
- Java 포트 바인딩 (`setcap`)
- systemd 서비스 파일 생성 및 등록
- 배포용 SSH 키 생성 및 GitHub Secrets 등록
- 자동 백업/롤백 흐름 설명
- 수동 긴급 롤백 절차

---

### 6. lightsail-deployment-review.md (신규)

**경로:** `docs/lightsail-deployment-review.md`

운영 배포 실행 계획:
- Phase 1~4 단계별 체크리스트 (로컬 검증 → 환경변수 → CI/CD → 배포 검증)
- 예외 상황 대응 테이블 (빌드 실패, 기동 실패, 헬스체크 실패, 긴급 롤백)
- 수동 긴급 롤백 절차 (코드 블록)
- 향후 개선 권고 (무중단 배포, logrotate, 환경변수 관리)

---

### Lightsail 서버 설정 내역

배포 과정에서 서버에 직접 수행한 설정:

| # | 작업 | 명령어/파일 |
|---|------|-----------|
| 1 | 앱 디렉토리 생성 | `mkdir -p /home/ubuntu/app` |
| 2 | `.env` 파일 생성 | `nano /home/ubuntu/app/.env` + `chmod 600` |
| 3 | Java 포트 바인딩 | `sudo setcap 'cap_net_bind_service=+ep' ...` |
| 4 | systemd 서비스 등록 | `/etc/systemd/system/toronchul.service` |
| 5 | 서비스 활성화 | `sudo systemctl daemon-reload && enable toronchul` |
| 6 | 배포용 SSH 키 생성 | `ssh-keygen -t ed25519` → GitHub Secrets 등록 |

### GitHub Secrets 등록 내역

| Name | 용도 |
|------|------|
| `LIGHTSAIL_HOST` | Lightsail VM 퍼블릭 IP |
| `LIGHTSAIL_SSH_PRIVATE_KEY` | 배포용 ED25519 프라이빗 키 |

### 제거 가능한 기존 Secrets (ECS 관련)

- `AWS_ROLE_TO_ASSUME`, `AWS_ACCOUNT_ID`
- `PROD_ECR_REPOSITORY_NAME`, `PROD_TASK_DEFINITION_NAME`, `PROD_ECS_SERVICE`, `PROD_ECS_CLUSTER`
- `DEV_ECR_REPOSITORY_NAME`, `DEV_TASK_DEFINITION_NAME`, `DEV_ECS_SERVICE`, `DEV_ECS_CLUSTER`

---

### 배포 중 발생한 이슈 및 해결

| # | 이슈 | 원인 | 해결 |
|---|------|------|------|
| 1 | ECR repository not found | CI/CD 워크플로우 파일이 커밋에서 누락 | `.github/workflows/` 파일 별도 커밋 |
| 2 | PAT에 workflow 권한 없음 | GitHub가 워크플로우 파일 push 거부 | PAT에 `workflow` 스코프 추가 |
| 3 | `debateseason.service not found` | 서버에 systemd 서비스 미생성 | 서버에서 `toronchul.service` 생성 및 등록 |
| 4 | 서비스명 불일치 | PROD_CICD.yml이 `debateseason` 참조 | `toronchul`로 변경 |
| 5 | Health check 301 리다이렉트 | Nginx가 :80 점유, Spring Boot도 :80 | Spring Boot 포트 80→8080 변경 |
| 6 | SSH heredoc exit code 1 | for 루프 내 `exit 0`이 SSH heredoc에서 전파 안 됨 | `ssh -T` + `bash -s` + break/플래그 방식으로 변경 |

---

### 빌드 결과

- **GitHub Actions PROD CI/CD**: build → test → deploy 전체 통과
- **GitHub Actions DEV CI/CD**: build → test 통과 (deploy 없음)
- **서버 상태**: `sudo systemctl status toronchul` → active (running)
- **헬스 체크**: `curl http://localhost:8080/prod/actuator/health` → 정상 응답

---

## 2026-03-06: SecurityPathMatcher context-path 버그 수정 + CI/CD 고스트 프로세스 방어

### 목적
웹(Next.js)에서 비로그인 상태로 `GET /api/v1/home/recommend`, `GET /api/v1/issue-map` 호출 시 401 반환되던 문제 수정.

### 근본 원인

**2가지 문제가 중첩**되어 있었다:

| # | 문제 | 영향 |
|---|------|------|
| 1 | CI/CD 마이그레이션(3/3) 이전에 수동 실행된 **구 Java 프로세스가 포트 8080을 점유** | `systemctl restart toronchul`로 새 서비스를 시작해도 포트를 잡지 못함. 헬스체크는 구 프로세스가 응답하여 통과 |
| 2 | `SecurityPathMatcher`가 `@Value("${server.servlet.context-path:}")`로 context-path를 주입받았으나 **런타임에 빈 문자열로 resolve** | `/prod/api/v1/home/recommend`에서 `/prod`가 제거되지 않아 Optional Auth URL 패턴 매칭 실패 → 401 |

### 진단 과정

1. 백엔드 코드 검토 → Optional Auth 구현은 올바름 (WebSecurityConfig, SecurityPathMatcher, JwtAuthenticationFilter, Controller 모두 정상)
2. 서버에서 직접 curl 테스트 → 401 확인
3. main 브랜치에 코드 push → CI/CD 배포 성공 → 여전히 401
4. `SecurityPathMatcher`를 `request.getContextPath()` 사용으로 변경 → 배포 성공 → 여전히 401
5. 디버깅 로그(`log.info`) 추가 → 배포 후 로그 출력 없음 → **새 코드가 실행되고 있지 않음** 의심
6. `sudo lsof -i :8080` → **구 프로세스(PID 259905)**가 포트 점유 확인, `toronchul` 서비스(PID 464396)는 별도 PID
7. 구 프로세스 kill 후 서비스 재시작 → 200 정상 응답

### 변경 파일 목록 (4개)

| # | 파일 | 변경 유형 |
|---|------|----------|
| 1 | `SecurityPathMatcher.java` | `@Value` 제거, `request.getServletPath()` 사용 |
| 2 | `JwtAuthenticationFilter.java` | `SecurityPathMatcher` 호출 시 `HttpServletRequest` 전달 |
| 3 | `RateLimitFilter.java` | `isExcluded()` context-path 처리 |
| 4 | `PROD_CICD.yml` | 배포 시 포트 8080 고스트 프로세스 kill 로직 추가 |

---

### 1. SecurityPathMatcher.java

**경로:** `src/main/java/com/debateseason_backend_v1/security/component/SecurityPathMatcher.java`

**변경 내용:**
- `@Value("${server.servlet.context-path:}")` 생성자 주입 **제거**
- 메서드 시그니처를 `String` → `HttpServletRequest`로 변경
- `resolvePath()`: `request.getServletPath()` 우선 사용 (context-path가 이미 제거된 경로), fallback으로 `request.getRequestURI()` + `request.getContextPath()` 수동 제거

**변경 전:**
```java
public SecurityPathMatcher(@Value("${server.servlet.context-path:}") String contextPath) {
    this.pathMatcher = new AntPathMatcher();
    this.contextPath = contextPath;
}

public boolean isPublicUrl(String requestURI) {
    String path = removeContextPath(requestURI);
    // ...
}

public boolean isOptionalAuthUrl(String requestURI, String method) {
    String path = removeContextPath(requestURI);
    // ...
}

private String removeContextPath(String requestURI) {
    if (!contextPath.isEmpty() && requestURI.startsWith(contextPath)) {
        return requestURI.substring(contextPath.length());
    }
    return requestURI;
}
```

**변경 후:**
```java
public SecurityPathMatcher() {
    this.pathMatcher = new AntPathMatcher();
}

public boolean isPublicUrl(HttpServletRequest request) {
    String path = resolvePath(request);
    // ...
}

public boolean isOptionalAuthUrl(HttpServletRequest request) {
    String path = resolvePath(request);
    // ...
}

public String resolvePath(HttpServletRequest request) {
    String servletPath = request.getServletPath();
    if (servletPath != null && !servletPath.isEmpty()) {
        return servletPath;
    }
    String uri = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
        return uri.substring(contextPath.length());
    }
    return uri;
}
```

**왜 `@Value` 대신 `request.getServletPath()`인가:**
- `@Value("${server.servlet.context-path:}")` 가 프로덕션 런타임에 빈 문자열로 resolve되는 현상 발생
- `request.getServletPath()`는 서블릿 컨테이너(Tomcat)가 context-path를 이미 제거한 경로를 반환하므로 가장 신뢰할 수 있음
- `request.getContextPath()`를 이용한 수동 제거는 fallback으로 유지

---

### 2. JwtAuthenticationFilter.java

**경로:** `src/main/java/com/debateseason_backend_v1/security/jwt/JwtAuthenticationFilter.java`

**변경 내용:**
- `securityPathMatcher.isPublicUrl(requestURI)` → `securityPathMatcher.isPublicUrl(request)`
- `securityPathMatcher.isOptionalAuthUrl(requestURI, request.getMethod())` → `securityPathMatcher.isOptionalAuthUrl(request)`

---

### 3. RateLimitFilter.java

**경로:** `src/main/java/com/debateseason_backend_v1/security/filter/RateLimitFilter.java`

**변경 내용:**
- `isExcluded(String requestURI)` → `isExcluded(HttpServletRequest request)`
- 내부에서 `request.getRequestURI()` + `request.getContextPath()`로 context-path 제거 후 패턴 매칭

---

### 4. PROD_CICD.yml

**경로:** `.github/workflows/PROD_CICD.yml`

**변경 내용:**
- `systemctl restart` 직전에 포트 8080을 점유하는 고스트 프로세스를 kill하는 로직 추가

**추가된 코드:**
```bash
# Kill any orphan java process occupying port 8080
ORPHAN_PID=$(sudo lsof -ti :8080 || true)
if [ -n "$ORPHAN_PID" ]; then
  echo "Killing orphan process on port 8080: PID=$ORPHAN_PID"
  sudo kill "$ORPHAN_PID" || true
  sleep 2
fi
```

**설계 의도:**
- CI/CD 마이그레이션 이전에 수동 실행된 Java 프로세스가 남아있는 경우 방어
- `|| true`로 프로세스가 없어도 스크립트 실패하지 않음
- `sleep 2`로 포트 해제 대기 후 `systemctl restart` 실행

---

### 빌드 결과

- **컴파일**: `./gradlew build -x test` → BUILD SUCCESSFUL
- **테스트**: GitHub Actions 60개 전체 통과
- **서버 검증**:
  - `curl http://localhost:8080/prod/api/v1/home/recommend` → 200 (비로그인 정상 응답)
  - `curl http://localhost:8080/prod/api/v1/issue-map` → 200 (비로그인 정상 응답)

---

## 2026-03-07: authorizeHttpRequests 간소화 + CommunitySorterV4 NPE 수정

### 목적
비로그인 상태로 `GET /api/v1/issue` 호출 시 403 반환, 이후 보안 통과 후 500(NPE) 발생하던 문제 수정.

### 근본 원인 (2가지)

| # | 문제 | 영향 |
|---|------|------|
| 1 | Spring Security 6.x `authorizeHttpRequests`의 `requestMatchers(String...)`가 내부적으로 `MvcRequestMatcher`를 사용. `/api/v1/issue`가 두 컨트롤러(`IssueControllerV1` GET, `AdminIssueControllerV1` POST)에 분산 매핑되어 `HandlerMappingIntrospector` 해석 실패 → 403 | `AntPathRequestMatcher` 명시 사용으로도 해결 안 됨 → `anyRequest().permitAll()`로 전환 |
| 2 | `CommunitySorterV4.getSortedCommunity()`에서 비로그인 시 `record()`가 호출되지 않아 `usersByIssue.get(issueId)` → null → for문 NPE → 500 | null 체크 추가로 빈 맵 반환 |

### 진단 과정

1. 서버에서 curl 테스트 → `/api/v1/issue` 403, `/api/v1/room` 404(정상), `/api/v1/issue-map` 200(정상)
2. 403 + Content-Length: 0 = Spring Security `Http403ForbiddenEntryPoint` (formLogin/httpBasic 비활성 시 기본)
3. 같은 `OPTIONAL_AUTH_URLS` 배열인데 `/api/v1/issue`만 실패 → 핸들러 매핑 조사
4. `/api/v1/issue`가 두 컨트롤러에 분산 매핑 발견 → `AntPathRequestMatcher` 시도 → 여전히 403
5. `authorizeHttpRequests`를 `anyRequest().permitAll()`로 변경 → 403 해결, 500 발생
6. 500 로그: `CommunitySorterV4.getSortedCommunity()` NPE → null 방어 추가 → 200 정상

### 설계 결정: `anyRequest().permitAll()`

**`JwtAuthenticationFilter`가 이미 모든 인증 로직을 처리:**
- Public URL → skip (인증 없이 통과)
- Optional Auth URL → 토큰 있으면 파싱, 없으면 anonymous 통과
- 그 외 → 토큰 필수 (없으면 401 응답, `filterChain.doFilter()` 호출 안 함)

따라서 `authorizeHttpRequests`의 URL별 `permitAll()` / `authenticated()` 설정은 **중복**이었으며, Spring Security의 `MvcRequestMatcher` 버그에 취약했다. `anyRequest().permitAll()`로 변경하여 URL 매칭 의존성을 제거하고, 접근 제어를 `JwtAuthenticationFilter`에 일원화.

### 변경 파일 (2개)

| # | 파일 | 변경 유형 |
|---|------|----------|
| 1 | `WebSecurityConfig.java` | `authorizeHttpRequests` 간소화 |
| 2 | `CommunitySorterV4.java` | NPE 방어 |

---

### 1. WebSecurityConfig.java

**경로:** `src/main/java/com/debateseason_backend_v1/config/WebSecurityConfig.java`

**변경 내용:**
- `authorizeHttpRequests` 내 URL별 `requestMatchers(...).permitAll()` 제거
- `anyRequest().permitAll()`로 단순화
- `AntPathRequestMatcher`, `RequestMatcher`, `Arrays` 등 미사용 import 정리

**변경 전:**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(PUBLIC_URLS).permitAll()
    .requestMatchers(OPTIONAL_AUTH_URLS).permitAll()
    .anyRequest().authenticated()
)
```

**변경 후:**
```java
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()
)
```

**`PUBLIC_URLS`, `OPTIONAL_AUTH_URLS` 상수 배열은 유지** — `JwtAuthenticationFilter`와 `SecurityPathMatcher`가 참조하므로 삭제 불가.

---

### 2. CommunitySorterV4.java

**경로:** `src/main/java/com/debateseason_backend_v1/domain/issue/community/CommunitySorterV4.java`

**변경 내용:**
- `getSortedCommunity()` 메서드에 `userList` null 체크 추가

**변경 전 (L101-109):**
```java
public LinkedHashMap<String, Integer> getSortedCommunity(Long issueId) {
    List<UserDTO> userList = usersByIssue.get(issueId);
    HashMap<String, Integer> map = new HashMap<>();
    for (UserDTO u : userList) {  // userList가 null이면 NPE
```

**변경 후 (L101-113):**
```java
public LinkedHashMap<String, Integer> getSortedCommunity(Long issueId) {
    List<UserDTO> userList = usersByIssue.get(issueId);

    if (userList == null) {
        return new LinkedHashMap<>();
    }

    HashMap<String, Integer> map = new HashMap<>();
    for (UserDTO u : userList) {
```

**발생 조건:**
- 비로그인 사용자가 이슈를 조회할 때, `record()`가 호출되지 않아 `usersByIssue`에 해당 issueId 엔트리가 없음
- `ConcurrentHashMap.get()` → null 반환 → for문 NPE

---

### 서버 검증

- `curl http://localhost:8080/prod/api/v1/issue?issue-id=1` → 200 (비로그인 정상 응답, `map: {}`, `bookMarkState: "no"`)
- `curl http://localhost:8080/prod/api/v1/room?chatroom-id=1` → 404 (해당 chatroom 미존재, 보안 통과)
- `curl http://localhost:8080/prod/api/v1/issue-map` → 200 (기존 정상)
- `curl http://localhost:8080/prod/api/v1/home/recommend` → 200 (기존 정상)
