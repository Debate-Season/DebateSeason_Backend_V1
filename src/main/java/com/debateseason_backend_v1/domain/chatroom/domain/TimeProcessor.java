package com.debateseason_backend_v1.domain.chatroom.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import lombok.RequiredArgsConstructor;

// 입력된 시간을 처리해주는 객체.
@RequiredArgsConstructor
@Component
public class TimeProcessor {

	private final ChatRepository chatRepository;

	// 대화가 한 번도 없는 방에 쓰는 문구. 예전에는 빈 문자열이었다.
	//
	// 클라이언트는 이 줄을 조건 분기 없이 그대로 그린다. 홈 카드(높이 140 고정)에서는 빈 줄이
	// 아래 여백에 흡수돼 티가 안 났지만, 이슈 상세 카드는 높이가 자유라 제목과 찬반 버튼 사이에
	// 34px 짜리 정체불명의 공백이 생겼다 — 레이아웃 버그로 보인다. 운영 26개 방 중 6개가 대화 0건이다.
	//
	// 문구를 클라마다 만들면 웹·앱이 갈라진다. time 은 '1일 전 대화' 처럼 서버가 완성된 표시
	// 문자열을 만들어 내려주는 필드이므로, 빈 값 대체도 같은 계층에 둔다.
	// (원시 숫자인 bookMarks 는 반대로 "0을 감출지"가 표현 계층인 클라 몫이다.)
	public static final String NO_CHAT_YET = "아직 대화가 없어요";

	// 1. chatRoomId를 이용해서 최근 대화 시간을 조회.
	// 대화가 없으면 NO_CHAT_YET 문구를 준다. null 은 어떤 경우에도 반환하지 않는다 —
	// 앱 DTO 가 non-nullable 이라 필드 하나가 null 이면 화면 파싱이 통째로 실패한다.
	public String findLastestChatTime(Long chatRoomId){
		Optional<LocalDateTime> latestChat = chatRepository.findMostRecentMessageTimestampByChatRoomId(chatRoomId);

		String time = NO_CHAT_YET;// 대화가 하나도 없는 방.

		if(latestChat.isPresent()){
			// 몇 분이 지났는지.
			Duration outdated = Duration.between(latestChat.get(), LocalDateTime.now());

			int realTime = (int)outdated.toMinutes();

			if(realTime == 0){
				time = "방금 전 대화";
			}
			else if(realTime >0 && realTime<60){ // mm만 표기
				time = outdated.toMinutes() + "분 전 대화"; // 분
			}
			else if(realTime >=60 && realTime <1440){ // hh:mm
				int hour = realTime/60;
				int minute = realTime%60;

				time = hour+"시간 "+minute+"분 전 대화";
			}
			else{ // day로 표기
				int day = realTime/1440;

				time = day+"일 전 대화";
			}

		}
		return time;
	}
}
