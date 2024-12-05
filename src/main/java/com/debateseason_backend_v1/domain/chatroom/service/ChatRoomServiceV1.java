package com.debateseason_backend_v1.domain.chatroom.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDAO;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDTO;
import com.debateseason_backend_v1.domain.chatroom.dto.ResponseDTO;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.IssueRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserChatRoom;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomServiceV1 {

	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final IssueRepository issueRepository; // 혹시나 Service쓰면, 나중에 순환참조 발생할 것 같아서 Repository로 함.
	private final UserChatRoomRepository userChatRoomRepository;

	private final ObjectMapper objectMapper;

	// 1. 채팅방 저장하기
	public ResponseEntity<?> save(ChatRoomDTO chatRoomDTO, long issueId) {

		// 1. Issue 찾기
		Issue issue = issueRepository.findById(issueId).orElseThrow(
			() -> new RuntimeException("There is no " + issueId)
		);

		// 2 ChatRoom 엔티티 생성
		ChatRoom chatRoom = ChatRoom.builder()
			.issue(issue)
			.title(chatRoomDTO.getTitle())
			.content(chatRoomDTO.getContent())
			.build();

		// 3. save ChatRoom
		chatRoomRepository.save(chatRoom);

		return ResponseEntity.ok("Successfully make ChatRoom!");
	}

	// 2. 채팅방 찬반 투표하기
	// Dirty Checking을 위해서 Transactional을 통한 변경감지
	@Transactional
	public ResponseEntity<?> vote(String opinion, Long chatRoomId, Long userId) {

		//1. 채팅방 가져오기
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(
			() -> new RuntimeException("Cannot vote! : " + chatRoomId)
		);

		// 2. User 가져오기
		User user = userRepository.findById(userId).orElseThrow(
			() -> new RuntimeException("There is no User: " + userId)
		);

		if (userChatRoomRepository.findByUserAndChatRoom(user, chatRoom) == null) {
			// 3. 최초 저장시에만 Entity 생성, 나머지는 Update(Dirty Checking)

			UserChatRoom userChatRoom = UserChatRoom.builder()
				.user(user)
				.chatRoom(chatRoom)
				.opinion(opinion)
				.build();

			userChatRoomRepository.save(userChatRoom);
		} else {
			// DirtyChecking
			UserChatRoom userChatRoom = userChatRoomRepository.findByUserAndChatRoom(user, chatRoom);
			userChatRoom.setOpinion(opinion);
		}

		//2. 변경사항 반영하기
        /*
		if (opinion.equals("yes")) {
			long countYes = chatRoom.getYes();
			chatRoom.setYes((int)(countYes + 1));
		} else {
			long countNo = chatRoom.getNo();
			chatRoom.setNo((int)(countNo + 1));
		}

         */

		return ResponseEntity.ok("Vote Successfully");
	}

	// 3. 채팅방 단건 불러오기
	public ResponseEntity<?> fetch(Long chatRoomId) {

		// 1. 채팅방 불러오기
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(
				() -> new RuntimeException("There is no ChatRoom: " + chatRoomId)
			);

		// 2. UserChatRoom 가져오기 (특정 이슈방에 대한 찬성/반대를 추출하기 위함)
		List<UserChatRoom> userChatRoom = userChatRoomRepository.findByChatRoom(chatRoom);

		// 2-1. 찬성 반대 count하기
		int countAgree = 0;
		int countDisagree = 0;
		for (UserChatRoom e : userChatRoom) {
			if (e.getOpinion().equals("AGREE")) {
				countAgree++;
			} else if (e.getOpinion().equals("DISAGREE")) {
				countDisagree++;
			}
			// 아무런 의견도 없는 경우는 걍 PASS
		}

		// 2-2. ChatRoomDAO로 옮기기
		ChatRoomDAO chatRoomDAO = ChatRoomDAO.builder()
			.id(chatRoom.getId())
			//.issue(chatRoom.getIssue())
			.title(chatRoom.getTitle())
			.content(chatRoom.getContent())
			.agree(countAgree)
			.disagree(countDisagree)
			.build();

		// 3. 관련 채팅들 불러오기가 추가될지도?

		ResponseDTO responseDTO = ResponseDTO.builder()
			.chatRoomDAO(chatRoomDAO)
			//.chatList(modifiedChatList)
			.build();

		String json;

		try {
			json = objectMapper.writeValueAsString(responseDTO);
		} catch (JsonProcessingException e) {
			log.error("ChatRoomSeviceV1 : " + e.getMessage());
			throw new RuntimeException(e);
		}
		return ResponseEntity.ok(json);

	}
}
