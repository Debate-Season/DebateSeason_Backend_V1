package com.debateseason_backend_v1.domain.chatroom.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;

import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chatroom.entity.ChatRoomMananger;
import com.debateseason_backend_v1.domain.chatroom.entity.team.Team;
import com.debateseason_backend_v1.domain.chatroom.infrastructure.ChatRoomRepository;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.dummy.HightlightResponse;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.ChatForm;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTime;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.ChatRoomResponse;
import com.debateseason_backend_v1.domain.chatroom.model.request.ChatRoomRequest;

import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.ResponseOnlyHome;
import com.debateseason_backend_v1.domain.chatroom.entity.team.TeamScore;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.Top5BestChatRoom;

import com.debateseason_backend_v1.domain.issue.entity.IssueManager;
import com.debateseason_backend_v1.domain.issue.infrastructure.IssueRepository;
import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;

import com.debateseason_backend_v1.domain.media.entity.MediaManager;
import com.debateseason_backend_v1.domain.media.infrastructure.MediaRepository;
import com.debateseason_backend_v1.domain.media.type.MediaType;





import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.Media;
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
	private final ChatRepository chatRepository;
	private final MediaRepository mediaRepository;

	private final int size = 5;


	// 1. 채팅방 저장하기
	public ApiResult<Object> save(ChatRoomRequest chatRoomRequest, long issueId) {

		// 1. Issue 찾기
		Issue issue;

		issue = issueRepository.findById(issueId);

		// 2 ChatRoom 엔티티 생성
		// 무조건 title, content 둘 다 값이 있는 경우를 말한다.
		ChatRoom chatRoom = ChatRoom.toJpaEntity(issue,chatRoomRequest);
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
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId);

		// 2. User 가져오기
		User user = userRepository.findById(userId).orElseThrow(
			() -> new CustomException(ErrorCode.NOT_FOUND_USER)
		);

		// 3. 투표하기
		UserChatRoom userChatRoom = userChatRoomRepository.findByUserAndChatRoom(user, chatRoom);


		if (userChatRoom == null) {// 3. 최초 투표에만 Entity 생성, 나머지는 Update(Dirty Checking)
			userChatRoom = UserChatRoom.toJpaEntity(user,chatRoom,opinion);
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
	public ApiResult<ChatRoomResponse> fetch(Long userId,Long chatRoomId,String type) { //,String type


		// 1. 채팅방 불러오기
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId);

		// opinion의 기본값 = NEUTRAL
		String opinion = "NEUTRAL";// 아무런 의견을 표명하지 않은 경우에는 NEUTRAL로 반환을 하는데, 이건 저장할 가치가 없다.

		// 내가 이 토론방에 투표한 의견 가져오기
		UserChatRoom getMyChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId,chatRoomId);
		if(getMyChatRoom!=null){
			opinion = getMyChatRoom.getOpinion();
		}

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

		if(type!=null && type.equals("highlight")){
			// ch.chat_id, ch.content,
			//         COUNT(CASE WHEN ch_r.reaction_type = 'LOGIC' THEN 1 END) AS logic,
			//         COUNT(CASE WHEN ch_r.reaction_type = 'ATTITUDE' THEN 1 END) AS attitude
			List<Object[]> highLightChats = chatRoomRepository.findChatHighlight(userId,chatRoomId);

			// highlight가 없는 경우, data 없이 반환
			if(highLightChats.isEmpty()){


				ChatRoomResponse chatRoomResponse = ChatRoomResponse.builder()
					.chatRoomId(chatRoom.getId())
					//.teams(team)
					.highlight(null)
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
			else{	// wiki

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



		// 3. 합계,논리,태도, MVP 가져오기

		// 3-1. 합계,논리 태도 가져오기(찬성/반대)

		List<Object[]> agree = chatRoomRepository.getReactionSummaryByOpinion(chatRoomId,"AGREE");
		List<Object[]> disagree = chatRoomRepository.getReactionSummaryByOpinion(chatRoomId,"DISAGREE");

		String agreeMvp = chatRoomRepository.findTopChatRoomUserNickname(chatRoomId,"AGREE");
		String disagreeMvp = chatRoomRepository.findTopChatRoomUserNickname(chatRoomId,"DISAGREE");

		int agreeLogic=0;
		int agreeAttitude=0;

		int disagreeLogic=0;
		int disagreeAttitude=0;

		ChatRoomMananger chatRoomMananger = new ChatRoomMananger();

		// 안에 데이터가 들어 있으면 계산을 한다
		//SUM(s.LOGIC) AS logic, SUM(s.ATTITUDE) AS attitude
		if(!agree.isEmpty()){
			Object[] agreeData = agree.get(0);
			//agreeLogic =  agreeData[0] == null ? 0 : ((Number)agreeData[0]).intValue();
			agreeLogic = chatRoomMananger.countLogic(agreeData);
			//agreeAttitude = agreeData[1] == null ? 0 : ((Number)agreeData[1]).intValue();
			agreeAttitude = chatRoomMananger.countAttribute(agreeData);
		}

		/*
		TeamScore TeamAgree = TeamScore.builder()
			.team("agree")
			.total(agreeLogic+agreeAttitude)
			.logic(agreeLogic)
			.attitude(agreeAttitude)
			.mvp(agreeMvp)
			.build()
			;

		 */
		TeamScore TeamAgree =  createTeamScore(
			Team.agree.getOpinion(),
			agreeLogic+agreeAttitude,
			agreeLogic,
			agreeAttitude,
			agreeMvp)
			;

		//
		if(!disagree.isEmpty()){

			Object[] disagreeData = disagree.get(0);
			//disagreeLogic = disagreeData[0] == null ? 0 : ((Number)disagreeData[0]).intValue();
			disagreeLogic = chatRoomMananger.countLogic(disagreeData);
			//disagreeAttitude = disagreeData[1] == null ? 0 : ((Number)disagreeData[1]).intValue();
			disagreeAttitude =chatRoomMananger.countAttribute(disagreeData);
		}


		/*
		TeamScore TeamDiagree = TeamScore.builder()
			.team("disagree")
			.total(disagreeLogic+disagreeAttitude)
			.logic(disagreeLogic)
			.attitude(disagreeAttitude)
			.mvp(disagreeMvp)
			.build()
			;

		 */

		TeamScore TeamDiagree =  createTeamScore(
			Team.disagree.getOpinion(),
			disagreeLogic+disagreeAttitude,
			disagreeLogic,
			disagreeAttitude,
			disagreeMvp)
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
		return chatRoomRepository.findById(chatRoomId);
	}

	// 4. 투표한 여러 채팅방 가져오기
	public ApiResult<ResponseOnlyHome> findVotedChatRoom(Long userId,Long pageChatRoomId){

		MediaManager mediaManager = new MediaManager();
		IssueManager issueManager = new IssueManager();

		// 여기서는 생성자에 chatRepository를 주입하는데, 이렇게 하는거 아닌 것 같은데, JPA에 의존적이게 되는데, 수정해야 할 것 같다.
		ChatRoomMananger chatRoomMananger = new ChatRoomMananger(chatRepository);

		// 속보 10건 가져오기
		List<Media> fetchBreakingNews = mediaRepository.findBreakingNews(10);
		List<Object> breakingNews = mediaManager.getMultiMedia(fetchBreakingNews, MediaType.Breaking_News);


		// issue_id, issue.title, chatroom.chat_room_id, chatroom.title
		// 1. 활성화된 최상위 5개 토론방을 보여준다.
		List<Object[]> getTop5ActiveChatRooms = chatRoomRepository.findTop5ActiveChatRooms();
		List<Top5BestChatRoom> top5BestChatRooms = chatRoomMananger.getTop5ActiveChatRooms(getTop5ActiveChatRooms);

		// 2. 활성화된 최상위 5개 이슈방을 보여준다.
		// issue_id, COUNT(ch.chat_room_id)

		// 최상위 5개의 이슈 id를 가져옴
		List<Long> issueIds = issueRepository.findTop5ActiveIssuesByCountingChats().stream().map(
			e-> (Long)e[0]
		).toList();

		// ui1.issue_id, ui1.title, ui1.created_at, ui1.chat_room_count, COUNT(ui2.issue_id) AS bookmarks
		List<Object[]> getTop5BestIssueRooms = issueRepository.findIssuesWithBookmarksOrderByCreatedDate(issueIds);
		List<IssueBriefResponse> top5BestIssueRooms = issueManager.getTop5Issues(getTop5BestIssueRooms);


		List<Long> chatRoomIds;
		// 첫 페이지
		if(pageChatRoomId==null){
			chatRoomIds = userChatRoomRepository.findTopChatRoomIdsByUserId(userId,size);
		}
		else{
			// 그 이후 페이지
			chatRoomIds = userChatRoomRepository.findTopChatRoomIdsByUserIdAndChatRoomId(userId,pageChatRoomId,size);
		}


		List<Object[]> chatRoomList = userChatRoomRepository.findChatRoomWithOpinions(userId,chatRoomIds);

		// 아직 투표한 방이 없어서 아무것도 없는 상태로 가져올 수 있다.
		if (chatRoomList.isEmpty()){

			ResponseOnlyHome responseOnlyHome = ResponseOnlyHome.builder()
				.breakingNews(breakingNews)
				//.chatRoomResponse(fetchedChatRoomList)
				.top5BestChatRooms(top5BestChatRooms)
				.top5BestIssueRooms(top5BestIssueRooms)
				.build()
				;

			return ApiResult.<ResponseOnlyHome>builder()
				.status(200)
				.code(ErrorCode.SUCCESS)
				.message("채팅방을 불러왔습니다.")
				.data(responseOnlyHome)
				.build();

		}

		// 원래는 ChatRoomResponse2
		List<ResponseWithTime> fetchedChatRoomList = chatRoomMananger.responseChatRoom(chatRoomList);

		ResponseOnlyHome responseOnlyHome = ResponseOnlyHome.builder()
			.breakingNews(breakingNews)
			.top5BestChatRooms(top5BestChatRooms)
			.top5BestIssueRooms(top5BestIssueRooms)
			.chatRoomResponse(fetchedChatRoomList)
			.build()
			;


		return ApiResult.<ResponseOnlyHome>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("채팅방을 불러왔습니다.")
			.data(responseOnlyHome)
			.build();

	}


	private TeamScore createTeamScore(
		String team,
		int total,
		int logic,
		int attribute,
		String mvp
	) {
		return TeamScore.builder()
			.team(team)
			.total(total)
			.logic(logic)
			.attitude(attribute)
			.mvp(mvp)
			.build()
			;

	}

}

