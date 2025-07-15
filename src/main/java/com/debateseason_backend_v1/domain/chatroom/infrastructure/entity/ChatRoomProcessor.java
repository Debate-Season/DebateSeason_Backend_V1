package com.debateseason_backend_v1.domain.chatroom.infrastructure.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

// DB에서 가져온 데이터 가공 -> DTO로 변환
@Component
@RequiredArgsConstructor
public class ChatRoomProcessor {

	private final ChatRepository chatRepository;
	private final ChatRoomRepository chatRoomRepository;



	// chat_room_id, title, content, created_at,
	//            COUNT(CASE WHEN ucr.opinion = 'AGREE' THEN 1 END) AS AGREE,
	//            COUNT(CASE WHEN ucr.opinion = 'DISAGREE' THEN 1 END) AS DISAGREE

	// chatRoomIds에 해당하는 채팅방 관련 정보(제목, 본문)+ 찬성/반대 가져오기
	public List<ResponseWithTimeAndOpinion> getChatRoomWithOpinionCount(List<Long> chatRoomIds){

		// chatRoomIds에 해당하는 채팅방 관련 정보(제목, 본문, 찬성/반대) 가져오기
		return chatRoomRepository.findChatRoomAggregates(chatRoomIds)
			.stream()
			.map(
				e -> {
					Long chatRoomId = (Long)e[0];
					String title = (String)e[1];
					String content = (String)e[2];

					String localDateTime = e[3].toString();
					String result = localDateTime.split("\\.")[0];
					LocalDateTime createdAt = LocalDateTime.parse(result,
						DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

					int agree = Math.toIntExact((Long)e[4]);
					int disagree = Math.toIntExact((Long)e[5]);

					String time = findLastestChatTime(chatRoomId);

					return ResponseWithTimeAndOpinion.builder()
						.chatRoomId(chatRoomId)
						.title(title)
						.content(content)
						.createdAt(createdAt)
						.opinion("NEUTRAL")
						.agree(agree)
						.disagree(disagree)
						.time(time)
						.build();

				}
			)
			.collect(Collectors.toList());

	}

	private String findLastestChatTime(Long chatRoomId) {
		Optional<LocalDateTime> latestChat = chatRepository.findMostRecentMessageTimestampByChatRoomId(chatRoomId);

		String time = null;

		if (latestChat.isPresent()) {
			// 몇 분이 지났는지.
			Duration outdated = Duration.between(latestChat.get(), LocalDateTime.now());

			int realTime = 0; // 대화가 아무것도 없는 상태는 항상 null이다.
			realTime = (int)outdated.toMinutes();

			if (realTime == 0) {
				time = "방금 전 대화";
			} else if (realTime > 0 && realTime < 60) { // mm만 표기
				time = outdated.toMinutes() + "분 전 대화"; // 분
			} else if (realTime >= 60 && realTime < 1440) { // hh:mm
				int hour = realTime / 60;
				int minute = realTime % 60;

				time = hour + "시간 " + minute + "분 전 대화";
			} else { // day로 표기
				int day = realTime / 1440;

				time = day + "일 전 대화";
			}

		}
		return time;
	}
}
