package com.debateseason_backend_v1.domain.chatroom.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.ChatRoomResponse;
import com.debateseason_backend_v1.domain.chatroom.model.request.ChatRoomRequest;

import com.debateseason_backend_v1.domain.chatroom.model.response.RealHomeResponse;
import com.debateseason_backend_v1.domain.chatroom.model.response.TeamScore;
import com.debateseason_backend_v1.domain.chatroom.model.response.Top5BestChatRoom;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.ChatRoomResponse2;
import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.IssueRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserChatRoom;

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

	// 1. 채팅방 저장하기
	public ApiResult<Object> save(ChatRoomRequest chatRoomRequest, long issueId) {

		// 1. Issue 찾기
		Issue issue = null ;
		try {
			issue = issueRepository.findById(issueId).orElseThrow(
				() -> new NullPointerException("There is no " + issueId)
			);

		}
		catch (NullPointerException | IllegalArgumentException e){
			return ApiResult.builder()
				.status(400)
				.code(ErrorCode.BAD_REQUEST)
				.message("선택하신 이슈방은 존재하지 않으므로, 채팅방을 생성할 수 없습니다.")
				.build();
		}


		// 2 ChatRoom 엔티티 생성
		ChatRoom chatRoom = ChatRoom.builder()
			.issue(issue)
			.title(chatRoomRequest.getTitle())
			.content(chatRoomRequest.getContent())
			.build();

		// 3. save ChatRoom
		chatRoomRepository.save(chatRoom);

		return ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("채팅방 " + chatRoomRequest.getTitle() + "이 생성되었습니다.")
			.build();
	}

	// 2. 채팅방 찬반 투표하기
	// Dirty Checking을 위해서 Transactional을 통한 변경감지
	@Transactional
	public ApiResult<String> vote(String opinion, Long chatRoomId, Long userId) {

		//1. 채팅방 가져오기
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(
			() -> new RuntimeException("Cannot vote! : " + chatRoomId)
		);

		// 2. User 가져오기
		User user = userRepository.findById(userId).orElseThrow(
			() -> new RuntimeException("There is no User: " + userId)
		);

		UserChatRoom userChatRoom = userChatRoomRepository.findByUserAndChatRoom(user, chatRoom);

		if (userChatRoom == null) {
			// 3. 최초 저장시에만 Entity 생성, 나머지는 Update(Dirty Checking)

			userChatRoom = UserChatRoom.builder()
				.user(user)
				.chatRoom(chatRoom)
				.opinion(opinion)
				.build();

			userChatRoomRepository.save(userChatRoom); // 1. 투표를 할 때 userChatRoom에 저장이 된다.
		} else {
			// DirtyChecking
			userChatRoom.setOpinion(opinion);
		}

		return ApiResult.<String>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message(opinion + "을 투표하셨습니다.")
			.build();
	}

	// 3. 채팅방 단건 불러오기
	// Opinion값 같이 넘겨주면 될 듯하다. 없으면 null
	public ApiResult<ChatRoomResponse> fetch(Long userId,Long chatRoomId) { //,String type

		// 우선 해당 채팅방이 유효한지 먼저 파악부터 해야한다.
		if(chatRoomRepository.findById(chatRoomId).isEmpty()){
			return ApiResult.<ChatRoomResponse>builder()
				.status(400)
				.code(ErrorCode.BAD_REQUEST)
				.message("선택하신 채팅방은 존재하지 않습니다.")
				.build();
		}


		String opinion = "NEUTRAL";// 아무런 의견을 표명하지 않은 경우에는 NEUTRAL로 반환을 하는데, 이건 저장할 가치가 없다.
		UserChatRoom tmpuserChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId,chatRoomId);
		if(tmpuserChatRoom!=null){
			opinion = tmpuserChatRoom.getOpinion();
		}

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


		// highlight인 경우에만 따로 출력을 한다.
		/*
		if(type!=null && type.equals("highlight")){
			// ch.chat_id, ch.content,
			//         COUNT(CASE WHEN ch_r.reaction_type = 'LOGIC' THEN 1 END) AS logic,
			//         COUNT(CASE WHEN ch_r.reaction_type = 'ATTITUDE' THEN 1 END) AS attitude
			List<Object[]> highLightChats = chatRoomRepository.findChatHighlight(userId,chatRoomId);

			// highlight가 없는 경우, data 없이 반환
			if(highLightChats.isEmpty()){
				return ApiResult.<ChatRoomResponse>builder()
					.status(200)
					.code(ErrorCode.SUCCESS)
					.message("채팅방을 불러왔습니다.")
					.data(null)
					.build();

			}
			else{
				
				// 합계
				int logic = 0;
				int attitude = 0;
				List<ChatForm> chats = new ArrayList<>();

				for(Object[] obj : highLightChats){
					String content = (String)obj[1];
					logic+=((Number)obj[2]).intValue();
					attitude+=((Number)obj[3]).intValue();

					if( ((Number)obj[2]).intValue() < 5 && ((Number)obj[3]).intValue()<5){
						continue;
					}
					chats.add(
						ChatForm.builder()
							.content(content)
							.attitude(((Number)obj[3]).intValue())
							.logic(((Number)obj[2]).intValue())
							.build()
					);

				}

				HightlightResponse hightlightResponse =
					HightlightResponse.builder()
						.totalAttitude(attitude)
						.totalLogic(logic)
						.highlightChats(chats)
						.build();

				ChatRoomResponse chatRoomResponse = ChatRoomResponse.builder()
					.chatRoomId(chatRoom.getId())
					//.teams(team)
					.highlight(hightlightResponse)
					.title(chatRoom.getTitle())
					.createdAt(chatRoom.getCreatedAt())
					.content(chatRoom.getContent())
					.agree(countAgree)
					.disagree(countDisagree)
					.opinion(opinion)
					.build();


				return ApiResult.<ChatRoomResponse>builder()
					.status(200)
					.code(ErrorCode.SUCCESS)
					.message("채팅방을 불러왔습니다.")
					.data(chatRoomResponse)
					.build();


			}


		}

		 */

		// 3. 합계,논리,태도, MVP 가져오기

		// 3-1. 합계,논리 태도 가져오기(찬성/반대)

		List<Object[]> agreeDTO = chatRoomRepository.getReactionSummaryByOpinion(chatRoomId,"AGREE");
		List<Object[]> disagreeDTO = chatRoomRepository.getReactionSummaryByOpinion(chatRoomId,"DISAGREE");

		String agreeMvp = chatRoomRepository.findTopChatRoomUserNickname(chatRoomId,"AGREE");
		String disagreeMvp = chatRoomRepository.findTopChatRoomUserNickname(chatRoomId,"DISAGREE");
		System.out.println(disagreeMvp);

		int agreeLogic=0;
		int agreeAttitude=0;


		int disagreeLogic=0;
		int disagreeAttitude=0;

		// 안에 데이터가 들어 있으면 계산을 한다
		//SUM(s.LOGIC) AS logic, SUM(s.ATTITUDE) AS attitude
		if(!agreeDTO.isEmpty()){
			Object[] agreeData = agreeDTO.get(0);
			agreeLogic =  agreeData[0] == null ? 0 : ((Number)agreeData[0]).intValue();
			agreeAttitude = agreeData[1] == null ? 0 : ((Number)agreeData[1]).intValue();
		}

		TeamScore TeamAgree = TeamScore.builder()
			.team("agree")
			.total(agreeLogic+agreeAttitude)
			.logic(agreeLogic)
			.attitude(agreeAttitude)
			.mvp(agreeMvp)
			.build()
			;

		//
		if(!disagreeDTO.isEmpty()){

			Object[] disagreeData = disagreeDTO.get(0);
			disagreeLogic = disagreeData[0] == null ? 0 : ((Number)disagreeData[0]).intValue();
			disagreeAttitude = disagreeData[1] == null ? 0 : ((Number)disagreeData[1]).intValue();
		}


		TeamScore TeamDiagree = TeamScore.builder()
			.team("disagree")
			.total(disagreeLogic+disagreeAttitude)
			.logic(disagreeLogic)
			.attitude(disagreeAttitude)
			.mvp(disagreeMvp)
			.build()
			;

		List<TeamScore> team = Arrays.asList(TeamAgree,TeamDiagree);



		// 2-2. ChatRoomDAO로 옮기기

		ChatRoomResponse chatRoomResponse = ChatRoomResponse.builder()
			.chatRoomId(chatRoom.getId())
			.teams(team)
			.title(chatRoom.getTitle())
			.createdAt(chatRoom.getCreatedAt())
			.content(chatRoom.getContent())
			.agree(countAgree)
			.disagree(countDisagree)
			.opinion(opinion)
			.build();

		// 3. 관련 채팅들 불러오기가 추가될지도? -> 향후 고려

		// interest를 반환해야 한다.
		return ApiResult.<ChatRoomResponse>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("채팅방을 불러왔습니다.")
			.data(chatRoomResponse)
			.build();

	}

	public ChatRoom findChatRoomById(Long chatRoomId) {

		return chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다 chatRoomID: " + chatRoomId));
	}

	// 4. 투표한 여러 채팅방 가져오기
	public ApiResult<RealHomeResponse> findVotedChatRoom(Long userId,Long pageChatRoomId){

		// chat_room_id, title, content
		// 1. 활성화된 최상위 5개 토론방을 보여준다.
		List<Top5BestChatRoom> top5BestChatRooms = chatRoomRepository.findTop5ActiveChatRooms().stream().map(
			e->{
				Long chatRoomId = (Long)e[0];
				String title = (String)e[1];
				String content = (String)e[2];

				return Top5BestChatRoom.builder()
					.chatRoomId(chatRoomId)
					.title(title)
					.content(content)
					.build()
					;


			}
		).toList();


		// 2. 활성화된 최상위 5개 이슈방을 보여준다.
		// issue_id, COUNT(ch.chat_room_id)

		// 최상위 5개의 이슈 id를 가져옴
		List<Long> issueIds = issueRepository.findTop5ActiveIssuesByCountingChats().stream().map(
			e-> (Long)e[0]
		).toList();

		// ui1.issue_id, ui1.title, ui1.created_at, ui1.chat_room_count, COUNT(ui2.issue_id) AS bookmarks
		List<IssueBriefResponse> top5BestIssueRooms = issueRepository.findIssuesWithBookmarksOrderByCreatedDate(issueIds).stream().map(
			e->{
				Long issueId = (Long)e[0];
				String title = (String)e[1];

				String time = e[2].toString();
				String result = time.split("\\.")[0];
				LocalDateTime createdAt = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				Long chatRoomCount = (Long)e[3];
				Long bookMarksCount = (Long)e[4];

				return IssueBriefResponse.builder()
					.issueId(issueId)
					.title(title)
					.createdAt(createdAt)
					.countChatRoom(chatRoomCount)
					.bookMarks(bookMarksCount)
					.build()
					;

			}
		).toList();


		List<Long> chatRoomIds;
		// 첫 페이지
		if(pageChatRoomId==null){
			chatRoomIds = userChatRoomRepository.findTop2ChatRoomIdsByUserId(userId);
		}
		else{
			// 그 이후 페이지
			chatRoomIds = userChatRoomRepository.findTop2ChatRoomIdsByUserIdAndChatRoomId(userId,pageChatRoomId);
		}


		List<Object[]> chatRoomList = userChatRoomRepository.findChatRoomByChatRoomIds(chatRoomIds);

		// 아직 투표한 방이 없어서 아무것도 없는 상태로 가져올 수 있다.
		if (chatRoomList.isEmpty()){

			RealHomeResponse realHomeResponse = RealHomeResponse.builder()
				//.chatRoomResponse(fetchedChatRoomList)
				.top5BestChatRooms(top5BestChatRooms)
				.top5BestIssueRooms(top5BestIssueRooms)
				.build()
				;

			return ApiResult.<RealHomeResponse>builder()
				.status(200)
				.code(ErrorCode.SUCCESS)
				.message("채팅방을 불러왔습니다.")
				.data(realHomeResponse)
				.build();

		}

		List<ChatRoomResponse2> fetchedChatRoomList = chatRoomList.stream().map(
			e->{
				// AGREE, DISAGREE, chat_room_id, title, content, created_at 순으로 가져오기
				Long agree = (Long)e[0];
				Long disagree = (Long)e[1];
				Long chatRoomId = (Long)e[2];
				String title = (String)e[3];
				String content = (String)e[4];
				String time = e[5].toString();

				String result = time.split("\\.")[0];


				LocalDateTime createdAt = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));



				return ChatRoomResponse2.builder()
					.chatRoomId(chatRoomId)
					.title(title)
					.content(content)
					.agree(Math.toIntExact(agree))
					.disagree(Math.toIntExact(disagree))
					.createdAt(createdAt)
					.build();

			}
		).collect(Collectors.toList());


		RealHomeResponse realHomeResponse = RealHomeResponse.builder()
			.chatRoomResponse(fetchedChatRoomList)
			.top5BestChatRooms(top5BestChatRooms)
			.top5BestIssueRooms(top5BestIssueRooms)
			.build()
			;

		return ApiResult.<RealHomeResponse>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("채팅방을 불러왔습니다.")
			.data(realHomeResponse)
			.build();

	}

}
