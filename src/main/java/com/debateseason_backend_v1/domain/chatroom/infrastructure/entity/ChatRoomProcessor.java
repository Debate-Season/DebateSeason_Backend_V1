package com.debateseason_backend_v1.domain.chatroom.infrastructure.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.chatroom.domain.TimeProcessor;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.Top5BestChatRoom;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

// DB에서 가져온 데이터 가공 -> DTO로 변환
@Component
@RequiredArgsConstructor
public class ChatRoomProcessor {

	private final ChatRoomRepository chatRoomRepository;

	// 시간 처리해주는 객체
	private final TimeProcessor timeProcessor;

	// chat_room_id, title, content, created_at,
	//            COUNT(CASE WHEN ucr.opinion = 'AGREE' THEN 1 END) AS AGREE,
	//            COUNT(CASE WHEN ucr.opinion = 'DISAGREE' THEN 1 END) AS DISAGREE

	// 1. chatRoomIds에 해당하는 채팅방 관련 정보(제목, 본문)+ 찬성/반대 가져오기
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

					String time = timeProcessor.findLastestChatTime(chatRoomId);

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

	// issue_id, issue.title, chatroom.chat_room_id, chatroom.title
	// 2. 활성화된 최상위 5개 토론방을 보여준다.
	// 값만 가져오면 되는 것을 굳이 DB를 왜 조회할까?
	public List<Top5BestChatRoom> getTop5ActiveRooms(){

		List<Object[]> top5BestChatRooms = chatRoomRepository.findTop5ActiveChatRooms();

		// 정적 배열로 수정을 함으로써, 성능 효율을 향상.
		Top5BestChatRoom [] chatRooms = new Top5BestChatRoom[5];

		// size = 5
		for(int i=0; i<5; i++){
			//
			Object[] rawChatRooms = top5BestChatRooms.get(i);

			//
			Long issueId = (Long)rawChatRooms[0];
			String issueTitle = (String)rawChatRooms[1];

			Long chatRoomId = (Long)rawChatRooms[2];
			String chatRoomTitle = (String)rawChatRooms[3];
			String time = timeProcessor.findLastestChatTime(chatRoomId);

			Top5BestChatRoom top5BestChatRoom = Top5BestChatRoom.builder()
				.issueId(issueId)
				.issueTitle(issueTitle)
				.debateId(chatRoomId)
				.debateTitle(chatRoomTitle)
				.time(time)
				.build()
				;

			chatRooms[i]=top5BestChatRoom;

		}

		// Mapper 클래스가 List이므로, 구체적인 클래스가 아니라 인터페이스로 바꿈.
		return Arrays.stream(chatRooms).toList();

	}




}
