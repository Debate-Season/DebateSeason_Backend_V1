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


	// 1. chatRoomId를 이용해서 최근 대화 시간을 조회.
	public String findLastestChatTime(Long chatRoomId){
		Optional<LocalDateTime> latestChat = chatRepository.findMostRecentMessageTimestampByChatRoomId(chatRoomId);

		String time = "";// 최초 생성인 경우 빈 문자열로 응답.

		if(latestChat.isPresent()){
			// 몇 분이 지났는지.
			Duration outdated = Duration.between(latestChat.get(), LocalDateTime.now());

			int realTime = 0; // 대화가 아무것도 없는 상태는 항상 null이다.
			realTime = (int)outdated.toMinutes();

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
