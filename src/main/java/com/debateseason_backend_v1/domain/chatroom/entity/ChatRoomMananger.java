package com.debateseason_backend_v1.domain.chatroom.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.Top5BestChatRoom;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTime;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;


import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChatRoomMananger {

	private ChatRepository chatRepository;

	public ChatRoomMananger(ChatRepository chatRepository){
		this.chatRepository = chatRepository;
	}


	// 1. 채팅방의 찬성/반대 각각을 count해서 반환하는 메소드.
	public List<ResponseWithTimeAndOpinion> findChatRoomsCountingOpinions(List<Object[]> chatRooms){

		return chatRooms.stream().map(
			e->{
				Long chatRoomId = (Long)e[0];
				String title = (String)e[1];
				String content = (String)e[2];

				String localDateTime= e[3].toString();
				String result = localDateTime.split("\\.")[0];
				LocalDateTime createdAt = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

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
		).collect(Collectors.toList());

	}

	// 2. 여러 채팅방에 특정 사용자의 Opinion 여부를 표현합니다.
	// 만약 자신이 채팅방에 찬성하면 AGREE, 반대는 DISAGREE, 아무것도 안하면 NEUTRAL로 표시된다.
	// 이를 통해서 사용자는 채팅방 목록에서 자신이 해당 채팅방에 투표했는지 여부를 확인할 수 있다.
	// Input은 오직 ResponseWithTimeAndOpinion만 가능하다.
	public void addOpinionToChatRoom(List<ResponseWithTimeAndOpinion> chatRooms,List<Object[]> opinions){
		if(!opinions.isEmpty()){
			for(Object [] obj : opinions){
				if(obj[0]!=null){
					// 투표를 하면 무조건 chatRoomId가 null이 아니다.
					Long chatRoomId = (Long)obj[0];
					String opinion = (String)obj[1];

					for(ResponseWithTimeAndOpinion e: chatRooms){
						if(e.getChatRoomId()==chatRoomId){
							e.setOpinion(opinion);
							break;
						}
					}
				}

			}
		}

	}


	// 3. 최신 채팅 시간(chatRoom 전용)
	private String findLastestChatTime(Long chatRoomId){
		Optional<LocalDateTime> latestChat = chatRepository.findMostRecentMessageTimestampByChatRoomId(chatRoomId);

		String time = null; // 대화가 아무것도 없는 상태는 항상 null이다.

		if(latestChat.isPresent()){
			// 몇 분이 지났는지.
			Duration outdated = Duration.between(latestChat.get(), LocalDateTime.now());

			time = new StringBuilder()
				.append(outdated.toMinutes())
				.append("분 전 대화")
				.toString();

		}
		return time;
	}

	// 4-1. Logic 수 세기
	public int countLogic(Object[] logicData){
		return logicData[0] == null ? 0 : ((Number)logicData[0]).intValue();
	}

	// 4-2. Attribute 수 세기
	public int countAttribute(Object[] attributeData){
		return attributeData[1] == null ? 0 : ((Number)attributeData[1]).intValue();
	}

	// 5. 활성화된 최상위 토론방 5개 가져오기.
	public List<Top5BestChatRoom> getTop5ActiveChatRooms(List<Object[]> chatRooms){
		return chatRooms.stream().map(
			e->{
				Long issueId = (Long)e[0];
				String issueTitle = (String)e[1];

				Long chatRoomId = (Long)e[2];
				String chatRoomTitle = (String)e[3];
				String time = findLastestChatTime(chatRoomId);
				/*
				Optional<LocalDateTime> latestChat = chatRepository.findLatestTimeStampByChatRoomId(chatRoomId);

				String time = null; // 대화가 아무것도 없는 상태는 항상 null이다.

				if(latestChat.isPresent()){
					// 몇 분이 지났는지.
					Duration outdated = Duration.between(latestChat.get(), LocalDateTime.now());

					time = new StringBuilder()
						.append(outdated.toMinutes())
						.append("분 전 대화")
						.toString();
				}
				 */
				return Top5BestChatRoom.builder()
					.issueId(issueId)
					.issueTitle(issueTitle)
					.debateId(chatRoomId)
					.debateTitle(chatRoomTitle)
					.time(time)
					.build()
					;


			}
		).toList();

	}

	// 6. 채팅방 응답
	public List<ResponseWithTime> responseChatRoom(List<Object[]> chatRoomList){

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
				String time = findLastestChatTime(chatRoomId);

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
