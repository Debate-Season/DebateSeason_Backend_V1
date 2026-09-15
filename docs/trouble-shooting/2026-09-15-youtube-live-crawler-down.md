# 유튜브 뉴스 크롤러 5개 동시 정지 — 썸네일 필드 추가

- **발생**: 2026-09-15 04:09 KST
- **인지**: 2026-09-15 22:10 KST (디스코드 에러 알림)
- **복구**: 2026-09-16 00:14 KST (PR #208 배포)
- **노출 시간**: 약 18시간
- **영향**: 앱·웹 "실시간 Live" 5개 카드가 **이미 끝난 방송**을 생방송으로 표시

---

## 한 줄 요약

유튜브가 API 응답에 썸네일 사이즈를 추가했는데, 우리 DTO 가 모르는 필드를 거부하도록
돼 있어서 **배포도 없이** 뉴스 크롤러 5개가 한꺼번에 죽었다.

## 증상

디스코드로 이런 알림이 72분마다 왔다.

```
🚨 toronchul 에러 감지 — 10건
▸ 5건 · o.s.s.s.TaskUtils$LoggingErrorHandler | Unexpected error occurred in scheduled task
▸ 1건 · c.d.d.y.scheduler.news.YtnNews | YtnNews에서 발생한 JsonProcessingException 에러
▸ 1건 · c.d.d.y.scheduler.news.YonhapNews | ...
... (KBS·MBC·SBS 동일)
```

## 오해하기 쉬웠던 지점

이 사고는 **세 번 사람을 헷갈리게 한다.** 다음에 비슷한 걸 보면 여기부터 의심할 것.

1. **"5개가 동시에 죽었으니 외부 API 장애겠지"** → 아니다. API 는 200 을 정상 반환하고
   있었다. 5개가 같은 mapper 클래스를 공유해서 한꺼번에 무너진 것이다.

2. **"배포 때문이겠지"** → 아니다. 앱은 9/3부터 무중단이었다. `ActiveEnterTimestamp` 를
   보면 바로 확인된다. **배포 없이 외부 응답 변화만으로 터질 수 있다**는 게 이 사고의 핵심이다.

3. **"로그만 시끄럽지 서비스는 멀쩡하네"** → 아니다. 가장 중요한 오해다.
   API 는 200 을 잘 내려줬지만 **내용이 어제 것으로 굳어 있었다.** 에러 로그가
   곧 데이터 정지를 뜻하는 경우가 있다. `youtube_live` 를 직접 봐야 드러난다.

## 조사 경로

실제로 원인까지 간 순서. 그대로 다시 쓸 수 있다.

```bash
# 1. 스택트레이스에서 근본 원인 뽑기 (알림에는 원인이 빠져 있었다 — 아래 "후속 조치" 참고)
ssh debateseason-prod "sudo journalctl -u toronchul.service -S '60 min ago' --no-pager \
  | grep -E 'ERROR|Caused by' | tail -50"
# → Caused by: UnrecognizedPropertyException: Unrecognized field "standard"
#              (class ...mapper.Thumbnails), not marked as ignorable
#              (3 known properties: "default", "high", "medium")

# 2. 배포 때문인지 확인 — 기동 시각이 사고 시각보다 한참 전이면 코드 변경이 아니다
ssh debateseason-prod "systemctl show toronchul.service -p ActiveEnterTimestamp"
# → Thu 2026-09-03 16:44:43 UTC  (사고는 9/15. 배포 무관)

# 3. 유튜브가 실제로 뭘 내려주는지 확인 (키는 .env 에서 읽고 출력하지 않는다)
ssh debateseason-prod "cd /home/ubuntu/app && set -a && . ./.env && set +a && \
  curl -s 'https://www.googleapis.com/youtube/v3/search?part=snippet&type=video\
&eventType=live&maxResults=1&channelId=UCcQTRi69dsVYHN3exePtZ1A&key='\"\$YOUTUBE_API_KEY\" \
  | python3 -c \"import sys,json; d=json.load(sys.stdin); \
print([list(i['snippet']['thumbnails'].keys()) for i in d.get('items',[])])\""
# → ['default', 'medium', 'high', 'standard', 'maxres', 'fhd']   ← 3개만 알고 있었다

# 4. 로그 소음인지 데이터 정지인지 판별 — 이 단계를 건너뛰면 심각도를 잘못 잡는다
ssh debateseason-prod "cd /home/ubuntu/app && set -a && . ./.env && set +a && \
  DBN=\$(echo \"\$DB_URL\" | sed -E 's#.*/([^/?]+)(\\?.*)?\$#\\1#') && \
  mysql -h 127.0.0.1 -u \"\$DB_USERNAME\" -p\"\$DB_PASSWORD\" \"\$DBN\" \
  -e 'SELECT category, video_id, created_at FROM youtube_live ORDER BY category;'"
# → 5행 전부 9/14 에서 멈춰 있었다

# 5. 저장된 방송이 아직 살아있는지 (videos.list 로 종료 여부 확인)
#    liveBroadcastContent 가 'none' 이고 actualEndTime 이 있으면 끝난 방송이다
```

## 근본 원인

`snippet.thumbnails` 에 `standard`·`maxres`·`fhd` 가 추가됐다.

```
유튜브가 주는 것: default, medium, high, standard, maxres, fhd
우리가 아는 것:   default, medium, high
```

`Thumbnails` 에 `@JsonIgnoreProperties` 가 없고 크롤러가 `new ObjectMapper()` 를
그대로 쓴다. `FAIL_ON_UNKNOWN_PROPERTIES` 기본값이 `true` 라 **모르는 필드 하나에
응답 역직렬화 전체가 실패**한다. `Thumbnails` 는 5개 크롤러가 공유하므로 동시에 무너졌다.

`JsonProcessingException` 을 잡은 뒤 `throw new RuntimeException(e)` 로 다시 던지기
때문에, 스케줄러 밖에서 `TaskUtils$LoggingErrorHandler` 가 한 번 더 찍는다.
그래서 5건이 아니라 10건으로 보였다.

## 영향

`youtube_live` 5행이 9/14 이후 갱신되지 않았다. 저장돼 있던 5건은 전부 이미 종료된
방송이었다(`liveBroadcastContent: none` + `actualEndTime` 확인).

| category | videoId | 종료 시각 (KST) |
|---|---|---|
| yonhap | `KL4zSRS9IeI` | 09-15 07:15 |
| ytn | `UKxJmocD9ME` | 09-15 08:58 |
| sbs | `rZNDR9LoB-U` | 09-15 09:12 |
| kbs | `7k2NMoV-jZE` | 09-15 15:27 |
| mbc | `tIpxA1gluss` | 09-15 17:01 |

링크가 깨진 건 아니다. 종료된 라이브는 VOD 로 남아 재생은 된다. 다만 "지금 생방송 중"
이라며 어제 뉴스를 보여줬다.

## 조치

### 1. 파싱 복구 (PR #208)

`scheduler/mapper` 패키지 **전체**에 `@JsonIgnoreProperties(ignoreUnknown = true)`.

`Thumbnails` 만 고치면 유튜브가 다음에 `Snippet` 이나 `Item` 에 필드를 늘릴 때
같은 일이 반복된다. 응답 트리 전체를 닫았다.

DB 수동 정리는 하지 않았다. `@Scheduled(fixedRate)` 는 initialDelay 가 없어 앱 기동
직후 1회차가 즉시 돌고, `fetch(category)` 가 기존 행을 찾아 더티체킹으로 덮어쓴다.
**배포 몇 초 만에 5건 전부 현재 라이브로 교체됐다.**

### 2. 라이브가 없을 때 끝난 방송 지우기 (별도 PR)

위 수정은 "파싱이 죽어서" 멈춘 경우만 푼다. **라이브 자체가 없을 때는 여전히 옛 방송이
남는 구조였다.**

```java
catch (IndexOutOfBoundsException e){
    log.warn("KbsNews Live 진행 안함.");   // 로그만 찍고 끝. DB 는 그대로
}
```

이제 해당 category 행을 지운다. 다시 켜지면 기존 `fetch() == null → save()` 분기가
새로 넣어주므로 되살아난다. 상태 컬럼(`is_live`)을 두는 안도 검토했지만 접었다 —
이 테이블은 category 당 행이 하나이고 72분마다 통째로 덮어쓰므로 **보존할 이력이
애초에 없다.** 운영 DB 스키마를 바꿀 값어치가 없었다.

행을 지우면 재삽입 때 id 가 바뀌는데, `/api/v1/live/{id}` 를 **웹도 앱도 쓰지 않고**
id 를 저장하거나 딥링크로 쓰는 곳도 없어서 무해하다.

### 3. 알림에 근본 원인 싣기 (PR #208)

이번 알림에는 **정작 원인이 없었다.** `error-monitor.sh` 가
`grep -E ' ERROR | SEVERE '` 로 헤더 줄만 남기는데, 스택트레이스와 `Caused by:` 줄에는
레벨 문자열이 없어 통째로 버려졌기 때문이다. 원인이 그 버려진 줄에 있었다.

이제 ERROR 줄에 딸린 연속줄까지 들고 마지막 `Caused by:` 를 근본 원인으로,
첫 번째 우리 패키지 프레임을 발생 위치로 뽑아 함께 보낸다.

> ⚠️ `scripts/error-monitor.sh` 는 **CI 가 동기화하지 않는다.** 고쳤으면 직접 올려야 한다.
> ```bash
> scp scripts/error-monitor.sh debateseason-prod:/home/ubuntu/scripts/
> ```

## 프론트엔드 영향 — 없음

행을 지우면 카드 수가 줄어든다. 웹·앱이 견디는지 확인했고, **양쪽 다 수정이 필요 없다.**

- **웹**: `Object.values(liveContainer)` 로 순회하고, 컨테이너가 없으면 `[]`,
  비면 섹션을 숨긴다. fetch 실패도 `.catch(() => ({ youtubeLive: [] }))` 로 흡수한다.
- **앱 홈**: `if (youtube.isEmpty) return Container();` 가드가 있다. 이 가드는
  `youtube[0]` 인덱싱을 도입한 바로 그 커밋(`00c08a6`, 2025-06-03)에 같이 들어왔으므로,
  **가드 없는 버전이 배포된 적이 없다.** 구버전 앱도 안전하다.
- **앱 전체보기**: `ListView.builder(itemCount: liveList.length)` — 0이면 아무것도 안 그린다.

카테고리 문자열(`"kbs"` 등)을 키로 찍어 쓰는 코드는 웹·앱 어디에도 없다.

## 남은 것 / 후속

- **앱 "전체보기" 에 빈 상태 화면이 없다.** 0건이면 앱바 아래가 백지다. 크래시는 아니고,
  로딩·실패에는 각각 화면이 있는데 빈 목록만 빠졌다. 필수는 아니지만 이제 실제로
  마주칠 수 있는 화면이 됐다.
- **앱 홈은 5개 중 `youtube[0]` 하나만 보여준다.** 그런데 백엔드가 `new HashMap<>()` 으로
  담아 내려주므로 **순서가 정해져 있지 않다.** 어느 채널이 대표로 뜨는지는 지금도 우연이다.
- **`KboLive`·`LckLive` 는 손대지 않았다** (`@Scheduled` 주석 처리된 비활성 상태).
  되살릴 거면 같은 삭제 처리를 반드시 넣을 것 — 스포츠 중계는 뉴스와 달리 **라이브가 없는
  시간이 대부분**이라 이 문제가 훨씬 자주 드러난다.
- **`home_media_page.dart:202` 에 지뢰가 있다.**
  `youtube[0].createAt.toString().substring(0, 16)` — `createAt` 은 `DateTime?` 이라
  null 이면 `"null"` 4글자에 substring 을 해서 **RangeError 로 앱이 죽는다.** 지금은
  서버가 항상 채워주지만, 이 필드를 비우는 변경을 하면 홈 화면이 터진다.

## 배운 것

1. **외부 API 응답은 배포 없이도 바뀐다.** 외부 JSON 을 받는 DTO 는 처음부터
   `ignoreUnknown = true` 로 열어둘 것. 필드가 늘어나는 건 보통 하위호환 변경이고,
   그걸 장애로 만드는 건 우리 쪽 설정이다.

2. **에러 로그가 곧 데이터 정지일 수 있다.** "API 는 200 이니 괜찮다" 로 넘기지 말고
   그 API 가 내려주는 **내용이 갱신되고 있는지**를 DB 에서 확인할 것.

3. **알림은 원인까지 실어야 알림이다.** 원인 없는 알림은 "서버에 들어가 보라"는
   통보일 뿐이고, 그만큼 대응이 늦어진다.

4. **스케줄러의 조용한 catch 를 의심할 것.** 이번 건의 절반은
   `catch (IndexOutOfBoundsException e) { log.warn(...); }` 처럼 **아무것도 하지 않는
   예외 처리**에서 나왔다. 로그만 찍고 상태를 그대로 두면, 문제가 드러나지 않은 채
   데이터가 굳는다.
