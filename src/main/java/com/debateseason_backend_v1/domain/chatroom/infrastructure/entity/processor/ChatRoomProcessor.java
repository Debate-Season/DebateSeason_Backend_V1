package com.debateseason_backend_v1.domain.chatroom.infrastructure.entity.processor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.chatroom.domain.RankingWindow;
import com.debateseason_backend_v1.domain.chatroom.domain.TimeProcessor;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.Top5BestChatRoom;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// DB에서 가져온 데이터 가공 -> DTO로 변환
@Slf4j
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
	// 2. "실시간 핫한 토론" — 30분 → 8시간 → 24시간 → 72시간 순으로 칸을 채우고,
	//    그래도 남는 자리는 최근 생성 토론방으로 채운다 (RankingWindow 참고).
	//
	// 결과 개수를 5로 가정하면 안 된다. 예전에는 for(i=0;i<5;i++) 로 List.get(i) 를 돌아서
	// 자격 있는 방이 5개 미만이면 IndexOutOfBoundsException → 500 이었다.
	// 지금은 쿼리가 창이 비어도 최신 방으로 5칸을 채우지만, 방 자체가 5개 미만인 환경
	// (신규 배포·테스트 DB)에서는 여전히 짧게 나온다. 크기 기반으로 돈다.
	public List<Top5BestChatRoom> getTop5ActiveRooms(){

		// now 를 한 번만 읽는다. 두 번 읽으면 경계 시각을 사이에 두고 창이 어긋날 수 있다.
		LocalDateTime now = LocalDateTime.now();
		List<Object[]> top5BestChatRooms = chatRoomRepository.findTop5ActiveChatRooms(
			RankingWindow.windowStart(now, RankingWindow.TIER_30M),
			RankingWindow.windowStart(now, RankingWindow.TIER_8H),
			RankingWindow.windowStart(now, RankingWindow.TIER_24H),
			RankingWindow.windowStart(now, RankingWindow.TIER_72H),
			RankingWindow.bucketEnd(now)
		);

		return top5BestChatRooms.stream()
			.map(raw -> {
				Long issueId = (Long)raw[0];
				String issueTitle = (String)raw[1];

				Long chatRoomId = (Long)raw[2];
				String chatRoomTitle = (String)raw[3];

				// 창 밖(폴백)으로 올라온 방은 대화가 없다. 그 경우 TimeProcessor 가 안내 문구를 준다.
				String time = timeProcessor.findLastestChatTime(chatRoomId);

				return Top5BestChatRoom.builder()
					.issueId(issueId)
					.issueTitle(issueTitle)
					.debateId(chatRoomId)
					.debateTitle(chatRoomTitle)
					.time(time)
					.build();
			})
			.collect(Collectors.toList());

	}




}
