# chat_reaction 잘못된 UNIQUE 인덱스 (미해결)

- **발견일:** 2026-07-20
- **상태:** 미수정. 채팅 리액션 기능이 아직 미배포이므로 사용자 영향 없음
- **조치 시점:** **리액션 기능을 출시하기 전 반드시 선행**
- **발견 경위:** 운영 ERROR 로그 감시 도구(`scripts/error-monitor.sh`) 도입 검증 중 확인

---

## 1. 요약

운영 DB `chat_reaction` 테이블에 **엔티티가 선언하지 않은 단일 컬럼 UNIQUE 인덱스 3개**가
존재한다. 이 상태로 리액션 기능을 열면 **서비스 전체에서 리액션 3건째부터 무조건 저장에
실패하고 사용자에게 500이 반환된다.**

애플리케이션 코드에는 결함이 없다. 순수한 스키마 문제다.

---

## 2. 현재 운영 DB 상태

`SHOW INDEX FROM chat_reaction` 기준. `NON_UNIQUE = 0` 은 UNIQUE를 뜻한다.

| 인덱스 | 컬럼 | UNIQUE | 판정 |
|---|---|---|---|
| `PRIMARY` | `reaction_id` | 0 | 정상 |
| `UK9ifhvpvcdjrhj77k3p4s2ijdf` | `chat_id, user_id, reaction_type` | 0 | **의도한 제약** |
| `user_id` | `user_id` | 0 | **잘못됨** |
| `chat_id` | `chat_id` | 0 | **잘못됨** |
| `reaction_type` | `reaction_type` | 0 | **잘못됨** |

엔티티가 선언한 제약은 복합 제약 하나뿐이다.

```java
// ChatReaction.java:15-16
@Table(name = "chat_reaction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id", "reaction_type"}))
```

이 선언은 `UK9ifh...` 로 정상 생성되어 있다. 나머지 3개는 **코드에 근거가 없고 DB에만 있다.**

---

## 3. 각 인덱스가 강제하는 규칙

| 잘못된 인덱스 | 실제로 강제되는 규칙 |
|---|---|
| `UNIQUE(user_id)` | 한 사용자는 평생 리액션 **1개**만 가능 |
| `UNIQUE(chat_id)` | 한 채팅 메시지에 리액션 **1개**만 가능 |
| `UNIQUE(reaction_type)` | 타입이 `LOGIC`/`ATTITUDE` 2종뿐 → **테이블 전체 최대 2행** |

마지막 항목이 치명적이다. 사용자 수와 무관하게 **전체 서비스에서 리액션 2건이 한계**다.

현재 `chat_reaction` 행 수는 **0건**이다.

---

## 4. 코드는 정상이다

`ChatReactionServiceV1.addReaction()` 은 중복을 올바르게 선검사한다.

```java
// ChatReactionServiceV1.java:71-85
Optional<ChatReaction> existingReaction = chatReactionRepository
        .findByChatIdAndUserIdAndReactionType(chat.getId(), userId, reactionType);

if (existingReaction.isEmpty()) {
    ...
    chatReactionRepository.save(reaction);   // 여기서 DataIntegrityViolationException
}
```

"같은 사용자 + 같은 채팅 + 같은 타입"이 없으면 저장하는데, DB가 **전혀 다른 이유**로
(예: 그 사용자가 이전에 다른 채팅에 리액션했음) 거부한다. 코드 레벨에서 방어할 수 없다.

예외가 서비스 계층에서 처리되지 않고 그대로 전파되어 500이 나간다.

과거 운영 로그에서 실제로 관측된 형태:

```
o.h.engine.jdbc.spi.SqlExceptionHelper : (conn=NNN) Duplicate entry 'NNN' for key 'user_id'
o.a.c.c.C.[dispatcherServlet]          : ... DataIntegrityViolationException ...
    insert into chat_reaction (chat_id,created_at,reaction_type,user_id) values (?,?,?,?)
```

`for key 'user_id'` — 복합 제약(`UK9ifh...`)이 아니라 단일 컬럼 인덱스에서 터졌음을 보여준다.

---

## 5. 원인 (추정)

레포에 Flyway/Liquibase가 없고 `application-prod.yml` 이 `ddl-auto: none` 이라
스키마가 수동 관리된다. 조회 성능 목적으로 `chat_id`, `user_id`, `reaction_type` 에
인덱스를 추가하려다 `INDEX` 대신 `UNIQUE` 로 만든 것으로 보인다.

`profile.age_range` 에 `UNDEFINED` 가 누락되어 익명화가 실패했던 건
(`scripts/migrations/2026-07-20__profile_age_range_add_undefined.sql`)과 **동일한 뿌리**다.
코드가 선언한 스키마와 DB 실제 상태를 대조하는 장치가 없다.

---

## 6. 해결 방법

리액션 기능 출시 **전에** 적용한다.

```sql
ALTER TABLE chat_reaction DROP INDEX user_id;
ALTER TABLE chat_reaction DROP INDEX chat_id;
ALTER TABLE chat_reaction DROP INDEX reaction_type;

-- 조회용 일반 인덱스.
-- chat_id 단독 조회는 복합 UK(chat_id 선두)가 이미 커버하므로 추가하지 않는다.
CREATE INDEX idx_chat_reaction_user_id ON chat_reaction (user_id);
```

**위험도: 낮음**

- 현재 행 수 0건 → 데이터 손실 위험 없음
- 제약을 **푸는** 방향 → 기존 데이터가 새 제약에 걸릴 일 없음
- 의도한 복합 제약 `UK9ifh...` 는 유지 → 진짜 중복(같은 사용자·채팅·타입)은 계속 차단됨

**적용 후 검증**

1. `SHOW INDEX FROM chat_reaction` 으로 UNIQUE가 `PRIMARY` 와 `UK9ifh...` 만 남았는지 확인
2. 롤백 트랜잭션으로 실제 INSERT 가능 여부 확인
   (서로 다른 사용자 2명 이상, 서로 다른 채팅 2건 이상, 타입 2종을 섞어서)
3. 적용 SQL을 `scripts/migrations/` 에 이력으로 남길 것

---

## 7. 재발 방지 (별도 과제)

이 건과 `age_range` 건 모두 "코드가 선언한 스키마 ≠ DB 실제 상태" 문제다.
Flyway 도입 또는 최소한 배포 시 스키마 대조를 자동화하면 같은 유형을 조기에 잡을 수 있다.

---

## 관련 파일

- `src/main/java/.../domain/chat/infrastructure/chat_reaction/ChatReaction.java`
- `src/main/java/.../domain/chat/application/service/ChatReactionServiceV1.java`
- `scripts/migrations/2026-07-20__profile_age_range_add_undefined.sql` (동일 유형 선례)
- `scripts/error-monitor.sh` (이 문제를 표면화시킨 도구)
