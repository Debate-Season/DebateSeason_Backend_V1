# 토론철 웹 프론트엔드 - 소셜 로그인 API 가이드

## 개요

웹 프론트엔드(Next.js)에서 백엔드 API를 호출하여 카카오/애플 소셜 로그인을 처리하는 방법을 정리한 문서입니다.
모바일 앱과 동일한 유저 DB를 공유하며, OIDC(OpenID Connect) 기반의 V2 API를 사용합니다.

---

## 로그인 엔드포인트

### 1. OIDC 로그인 (V2) - 권장

웹 프론트엔드에서는 카카오/애플 OAuth를 통해 **ID Token**을 발급받은 뒤, 이를 백엔드로 전달합니다.

| 항목         | 값                            |
| ------------ | ----------------------------- |
| **Method**   | `POST`                        |
| **URL**      | `/api/v2/users/login`         |
| **인증**     | 불필요 (Public endpoint)       |

#### Request Body

```json
{
  "socialType": "kakao",
  "idToken": "<OIDC ID Token>"
}
```

| 필드         | 타입     | 필수 | 설명                                         |
| ------------ | -------- | ---- | -------------------------------------------- |
| `socialType` | `string` | O    | 소셜 로그인 타입. `"kakao"` 또는 `"apple"` 중 하나 |
| `idToken`    | `string` | O    | 소셜 프로바이더에서 발급받은 OIDC ID Token        |

#### Response Body (성공 시)

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "로그인을 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "socialType": "kakao",
    "profileStatus": false,
    "termsStatus": false
  }
}
```

| 필드                  | 타입      | 설명                                           |
| --------------------- | --------- | ---------------------------------------------- |
| `data.accessToken`    | `string`  | API 호출 시 `Authorization: Bearer <token>` 헤더에 사용 |
| `data.refreshToken`   | `string`  | Access Token 만료 시 재발급용                    |
| `data.socialType`     | `string`  | 로그인한 소셜 타입                                |
| `data.profileStatus`  | `boolean` | 프로필 생성 여부 (`false`면 프로필 생성 화면으로 이동) |
| `data.termsStatus`    | `boolean` | 이용약관 동의 여부 (`false`면 약관 동의 화면으로 이동) |

---

### 2. 소셜 로그인 (V1) - 참고용

V1은 소셜 프로바이더의 고유 식별자(identifier)를 직접 전달하는 방식입니다.
웹에서는 V2(OIDC) 방식을 사용하는 것을 권장합니다.

| 항목         | 값                            |
| ------------ | ----------------------------- |
| **Method**   | `POST`                        |
| **URL**      | `/api/v1/users/login`         |
| **인증**     | 불필요 (Public endpoint)       |

#### Request Body

```json
{
  "identifier": "1323412",
  "socialType": "kakao"
}
```

| 필드         | 타입     | 필수 | 설명                                         |
| ------------ | -------- | ---- | -------------------------------------------- |
| `identifier` | `string` | O    | 소셜 프로바이더에서 제공하는 사용자 고유 식별자     |
| `socialType` | `string` | O    | 소셜 로그인 타입. `"kakao"` 또는 `"apple"` 중 하나 |

---

## 카카오 로그인 (웹) 전체 흐름

### Step 1: 카카오 SDK로 ID Token 발급

카카오 JavaScript SDK 또는 REST API를 통해 OIDC ID Token을 발급받습니다.
카카오 앱 설정에서 **OpenID Connect 활성화**가 필요합니다.

```typescript
// Next.js 예시 - 카카오 SDK 사용
const kakao = window.Kakao;

kakao.Auth.login({
  scope: "openid",
  success: (authObj) => {
    const idToken = authObj.id_token;
    // Step 2로 진행
  },
});
```

### Step 2: 백엔드 API 호출

```typescript
const response = await fetch("https://<BACKEND_HOST>/api/v2/users/login", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    socialType: "kakao",
    idToken: "<카카오에서 발급받은 ID Token>",
  }),
});

const result = await response.json();
// result.data.accessToken, result.data.refreshToken 저장
```

### Step 3: cURL 예시

```bash
curl -X POST https://<BACKEND_HOST>/api/v2/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "socialType": "kakao",
    "idToken": "eyJraWQiOiJkTWxFUkJhRmRLIiwiYWxnIjoiUlMyNTYifQ.eyJpc3M..."
  }'
```

---

## 애플 로그인 (웹) 전체 흐름

### Step 1: Apple JS SDK로 ID Token 발급

Apple Sign In JS SDK를 사용하여 ID Token을 발급받습니다.

```typescript
// Next.js 예시 - Apple JS SDK 사용
const AppleID = window.AppleID;

AppleID.auth.init({
  clientId: "<APPLE_SERVICE_ID>",
  scope: "name email",
  redirectURI: "<REDIRECT_URI>",
  usePopup: true,
});

const appleResponse = await AppleID.auth.signIn();
const idToken = appleResponse.authorization.id_token;
// Step 2로 진행
```

### Step 2: 백엔드 API 호출

```typescript
const response = await fetch("https://<BACKEND_HOST>/api/v2/users/login", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    socialType: "apple",
    idToken: "<애플에서 발급받은 ID Token>",
  }),
});

const result = await response.json();
// result.data.accessToken, result.data.refreshToken 저장
```

### Step 3: cURL 예시

```bash
curl -X POST https://<BACKEND_HOST>/api/v2/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "socialType": "apple",
    "idToken": "eyJraWQiOiJXNldjT0tCIiwiYWxnIjoiUlMyNTYifQ.eyJpc3M..."
  }'
```

---

## 토큰 재발급

Access Token이 만료되면 Refresh Token으로 재발급합니다.

| 항목         | 값                            |
| ------------ | ----------------------------- |
| **Method**   | `POST`                        |
| **URL**      | `/api/v1/auth/reissue`        |
| **인증**     | 불필요 (Public endpoint)       |

#### Request Body

```json
{
  "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
}
```

#### Response Body (성공 시)

```json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "토큰 재발급에 성공했습니다.",
  "data": {
    "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

## 인증이 필요한 API 호출

로그인 후 발급받은 Access Token을 `Authorization` 헤더에 담아 요청합니다.

```typescript
const response = await fetch("https://<BACKEND_HOST>/api/v1/some-endpoint", {
  method: "GET",
  headers: {
    Authorization: `Bearer ${accessToken}`,
    "Content-Type": "application/json",
  },
});
```

---

## 에러 응답 형식

```json
{
  "status": 401,
  "code": "EXPIRED_ACCESS_TOKEN",
  "message": "Access Token이 만료되었습니다."
}
```

주요 에러 코드:

| 코드                     | 설명                          |
| ------------------------ | ----------------------------- |
| `EXPIRED_ACCESS_TOKEN`   | Access Token 만료              |
| `INVALID_ACCESS_TOKEN`   | 유효하지 않은 Access Token      |
| `MISSING_ACCESS_TOKEN`   | Authorization 헤더 누락         |
| `EXPIRED_REFRESH_TOKEN`  | Refresh Token 만료              |
| `INVALID_REFRESH_TOKEN`  | 유효하지 않은 Refresh Token      |
| `MISSING_REFRESH_TOKEN`  | Refresh Token 누락              |

---

## 로그인 후 분기 처리

```
로그인 API 호출
  -> profileStatus == false  -> 프로필 생성 화면으로 이동
  -> termsStatus == false    -> 이용약관 동의 화면으로 이동
  -> 둘 다 true              -> 메인 화면으로 이동
```
