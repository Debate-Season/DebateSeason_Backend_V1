package com.debateseason_backend_v1.domain.chatroom.infrastructure.entity.manager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.chatroom.domain.TimeProcessor;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTime;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatRoomManager {

	private final UserChatRoomRepository userChatRoomRepository;

	// 시간 가공 객체.
	private final TimeProcessor timeProcessor;

	// 1. 사용자가 본인이 투표한 채팅방 목록들을 가져온다.
	public List<Object[]> findRawVotedChatRoomsByUser(Long userId,Long pageChatRoomId){

		List<Long> chatRoomIds;
		// 첫 페이지
		if(pageChatRoomId==null){
			chatRoomIds = userChatRoomRepository.findTop2ChatRoomIdsByUserId(userId);

		}
		else{
			// 그 이후 페이지
			chatRoomIds = userChatRoomRepository.findTop2ChatRoomIdsByUserIdAndChatRoomId(userId,pageChatRoomId);

		}

		return userChatRoomRepository.findChatRoomWithOpinions(userId,chatRoomIds);
	}

	// 2. 가져온 채팅방 목록들 후처리.
	public List<ResponseWithTime> findVotedChatRoomsByUser(Long userId,Long pageChatRoomId){

		List<Object[]> chatRoomList = findRawVotedChatRoomsByUser(userId,pageChatRoomId);


		if(chatRoomList.isEmpty()){

			return null;

		}
		else{

			return chatRoomList.stream().map(
				e->{
					// AGREE, DISAGREE, chat_room_id, title, content, created_at 순으로 가져오기
					Long agree = (Long)e[0];
					Long disagree = (Long)e[1];
					Long chatRoomId = (Long)e[2];
					String title = (String)e[3];
					String content = (String)e[4];
					String localDateTime = e[5].toString();
					String opinion = e[6].toString();// opinion

					String result = localDateTime.split("\\.")[0];
					LocalDateTime createdAt = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

					// 얼마전에 대화가 이루어 졌는지 파악.
					String time = timeProcessor.findLastestChatTime(chatRoomId);

					return ResponseWithTime.builder()
						.chatRoomId(chatRoomId)
						.title(title)
						.content(content)
						.agree(Math.toIntExact(agree))
						.disagree(Math.toIntExact(disagree))
						.createdAt(createdAt)
						.time(time)
						.opinion(opinion)
						.build();


				}
			).collect(Collectors.toList());

		}


	}





}
