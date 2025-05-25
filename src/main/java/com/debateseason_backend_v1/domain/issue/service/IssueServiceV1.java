package com.debateseason_backend_v1.domain.issue.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;

import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chatroom.entity.ChatRoomMananger;

import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;
import com.debateseason_backend_v1.domain.issue.entity.IssueManager;
import com.debateseason_backend_v1.domain.issue.PaginationDTO;
import com.debateseason_backend_v1.domain.issue.infrastructure.IssueRepository;
import com.debateseason_backend_v1.domain.issue.model.mapper.IssueDetailMapper;
import com.debateseason_backend_v1.domain.issue.model.mapper.IssueMapper;
import com.debateseason_backend_v1.domain.issue.model.response.IssueDetailResponse;
import com.debateseason_backend_v1.domain.issue.model.request.IssueRequest;
import com.debateseason_backend_v1.domain.issue.model.CommunityRecords;
import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;

import com.debateseason_backend_v1.domain.repository.ChatRoomJpaRepository;




import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserIssueRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserIssue;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;
import com.debateseason_backend_v1.domain.userIssue.UserIssueManager;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class IssueServiceV1 {

	private final IssueRepository issueRepository;
	private final UserIssueRepository userIssueRepository;
	private final UserChatRoomRepository userChatRoomRepository;
	private final UserRepository userRepository;

	private final ProfileRepository profileRepository;
	private final ChatRepository chatRepository;

	private final ChatRoomJpaRepository chatRoomJpaRepository;

	private final int cursorSize = 10;

	// 1. save 이슈방
	// Refactored
	public ApiResult<Object> save(IssueRequest issueRequest) {

		Issue IssueJpaEntity = Issue.toJpaEntity(issueRequest);

		issueRepository.save(IssueJpaEntity);

		return ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueRequest.getTitle() + "가 생성되었습니다.")
			.build();
	}

	//2. fetch 단건 이슈방
	@Transactional
	public ApiResult<IssueDetailResponse> fetch2(Long issueId, Long userId, Long ChatRoomId) {

		// 확인용
		issueRepository.findById(issueId);


		Long chats;

		// 첫 방문 항상 bookmarkState는 no
		String bookMarkState = "no";

		// User가 해당 이슈 bookMark했는지 가져오는 쿼리
		String userIssue = userIssueRepository.findBookMarkByIssueIdAndUserId(issueId,userId);



		// 그 사용자가 방문했는지 여부를 bookMarkState에 덮어씌운다.
		if(userIssue!=null){
			bookMarkState=userIssue;
		}


		// issue_id, title, COUNT(ui.issue_id) AS bookmarks
		List<Object[]> fetchIssue = issueRepository.findIssueWithBookmarks(issueId);

		IssueMapper issueMapper = IssueManager.findIssueWithIdAndTitleAndBookMarks(fetchIssue);

		String issueTitle = issueMapper.getTitle() ;
		Long bookMarks = issueMapper.getBookMarks();


		//
		Profile profile = profileRepository.findByUserId(userId).orElseThrow(
			() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE)
		);

		CommunityType communityType = profile.getCommunityType();
		if (communityType == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_COMMUNITY);
		}


		// 2. 서버 세션에 user 방문 기록 저장하기. 이는 커뮤니티 사용자 수를 내림차순으로 보여주기 위함임.
		UserDTO userDTO = new UserDTO();
		userDTO.setCommunity(communityType.getName());
		userDTO.setId(userId);

		CommunityRecords.record(userDTO, issueId);

		// LinkedHashMap을 써서 순서를 보장한다.
		LinkedHashMap<String, Integer> sortedMap = CommunityRecords.getSortedCommunity(issueId);


		// 3. chatRoomList와 각 chatRoom에 대한 찬성/반대 수 가져온다.

		List<Long> chatRoomIds ;

		// 첫 페이지
		if(ChatRoomId==null){
			chatRoomIds = userChatRoomRepository.findTopChatRoomIdsByIssueIdWithSize(issueId,cursorSize);
		}
		else{
			// 그 이후 페이지
			chatRoomIds = userChatRoomRepository.findTopChatRoomIdsByIssueIdAndChatRoomIdWithSize(issueId,ChatRoomId,cursorSize);
		}


		IssueDetailResponse issueDetailResponse;


		// 채팅방은 없으면 없는대로 반환을 한다
		if(chatRoomIds.isEmpty()){
			IssueDetailMapper issueDetailMapper = IssueDetailMapper.builder()
				.title(issueTitle)
				.bookMarkState(bookMarkState)
				.bookMarks(bookMarks)
				.map(sortedMap)
				.build();

			issueDetailResponse =IssueManager.createIssueDetailResponseWithNoChatRooms(issueDetailMapper);


			return ApiResult.<IssueDetailResponse>builder()
				.status(200)
				.code(ErrorCode.SUCCESS)
				.message("이슈방 " + issueId + "조회")
				.data(issueDetailResponse)
				.build();
		}

		// chat_room_id, title, content, created_at,
		//            COUNT(CASE WHEN ucr.opinion = 'AGREE' THEN 1 END) AS AGREE,
		//            COUNT(CASE WHEN ucr.opinion = 'DISAGREE' THEN 1 END) AS DISAGREE

		ChatRoomMananger chatRoomMananger = new ChatRoomMananger(chatRepository);
		List<Object[]> chatRoomsByIdWithOpinions = userChatRoomRepository.findChatRoomAggregates(chatRoomIds);

		List<ResponseWithTimeAndOpinion> chatRooms = chatRoomMananger.findChatRoomsCountingOpinions(chatRoomsByIdWithOpinions);

		// chat_room_id, opinion AS opinion
		// 채팅방 각각에 대해서 사용자의 Opinion을 부여한다.
		// 사용자는 채팅방 리스트에서 자신이 어떤 Opinion인지 확인이 가능하다.
		List<Object[]> opinions =  userChatRoomRepository.findUserChatRoomOpinions(userId,chatRoomIds);
		chatRoomMananger.addOpinionToChatRoom(chatRooms,opinions);

		// 오늘 신규 대화
		chats = issueRepository.countChatsTodayByIssueId(issueId);


		// 1-5 IssueDAO만들기
		IssueDetailMapper issueDetailMapper = IssueDetailMapper.builder()
			.title(issueTitle)
			.bookMarkState(bookMarkState)
			.bookMarks(bookMarks)
			.chats(chats)
			.map(sortedMap)
			.chatRoomMap(chatRooms)
			.build();

		issueDetailResponse = IssueManager.createIssueDetailResponseWithChatRooms(issueDetailMapper);

		return ApiResult.<IssueDetailResponse>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueId + "조회")
			.data(issueDetailResponse)
			.build();


	}

	// 즐겨찾기 등록하기
	// Refactored
	@Transactional // 만약에 하나로 문제가 생기면 바로 Rollback
	public ApiResult<String> bookMark(Long issueId, Long userId){

		User user = userRepository.findById(userId).orElseThrow(
			()-> new CustomException(ErrorCode.NOT_FOUND_USER)
		)
			;

		Issue issue = issueRepository.findById(issueId);

		UserIssue fetchUserIssue= userIssueRepository.findByIssueAndUser(issue,user);

		UserIssueManager userIssueManager = new UserIssueManager();


		// 중복 등록 방지
		// 이 부분은 Jpa 더티체킹을 발생시킴
		if(fetchUserIssue!=null){
			String bookMarkState = userIssueManager.updateBookMarkUserIssue(fetchUserIssue);

			return ApiResult.<String>builder()
				.status(200)
				.code(ErrorCode.SUCCESS)
				.data("이슈방의 즐겨찾기가 "+bookMarkState+"로 바꿔었습니다")
				.build();


		}

		UserIssue userIssue = userIssueManager.bookMarkUserIssue(user,issue);
		userIssueRepository.save(userIssue);

		return ApiResult.<String>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.data("이슈방 "+issueId+"을 관심등록 했습니다.")
			.build();

	}

	// 4. issueMap 가져오기
	// issue_id, title, major_category, (middle_category), created_at
	public ApiResult<PaginationDTO> fetchIssueMap(Long page, String majorCategory){//, String middleCategory

		IssueManager issueManager = new IssueManager();

		// 일단 issueId 여러개를 가져온다.
		List<Long> issueIds;

		if(majorCategory==null){

			// 전체 불러오기
			if(page == null){
				issueIds = issueRepository.findTop6Issues(cursorSize);
			}
			else{
				issueIds = issueRepository.findTop6IssuesByPage(page,cursorSize);
			}

		}else{
			if(page == null){
				issueIds = issueRepository.findTop6IssuesByCategory(majorCategory,cursorSize);
			}
			else{
				issueIds = issueRepository.findTop6IssuesByPageAndCategory(majorCategory,page,cursorSize);
			}
		}

		if (issueIds.isEmpty()){
			// 페이지네이션 오류
			throw new CustomException(ErrorCode.PAGE_OUT_OF_RANGE);
		}

		// issue_id, title, created_at, chat_room_count, COUNT(ui2.issue_id) AS bookmarks
		// issueIds를 넣어줌으로써 어떠한 page, category에 상관없이 하나의 쿼리로 커버가 가능하다.
		//List<IssueBriefResponse> issueBriefResponse = issueRepository.findIssuesWithBookmarks(issueIds).stream().map(
		List<Object[]> issues = issueRepository.findIssueCountingBookmarkAndChatRoom(issueIds);
		PaginationDTO paginationDTO = issueManager.createIssueForIssueMapOnly(issues);


		return ApiResult.<PaginationDTO>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 불러왔습니다.")
			.data(paginationDTO)
			.build();

	}

	// 구버전 Legacy
	public ApiResult<List<IssueBriefResponse>> fetchAll() {
		List<Issue> issueList = issueRepository.findAll();

		// Gson,JSONArray이 없어서 Map으로 반환을 한다.
		List<IssueBriefResponse> responseList = new ArrayList<>();

		// loop를 돌면서, issueId에 해당하는 chatRoom을 count 한다.
		for (int i = 0; i < issueList.size(); i++) {

			Long id = issueList.get(i).getId();
			IssueBriefResponse response = IssueBriefResponse.builder()
				.issueId(id)
				.title(issueList.get(i).getTitle())
				.createdAt(issueList.get(i).getCreatedAt())
				.countChatRoom(chatRoomJpaRepository.countByIssue(issueList.get(i)))
				.build();
			responseList.add(response);
		}

		// chat_room_id, title, content
		// 1. 활성화된 최상위 5개 토론방을 보여준다.
		/*
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

		for(Top5BestChatRoom e: top5BestChatRooms){
			System.out.println(e.getTitle());
		}





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

		OnlyHomeResponse onlyHomeResponse = OnlyHomeResponse.builder()
			.chatRoomResponse(responseList)
			.top5BestChatRooms(top5BestChatRooms)
			.top5BestIssueRooms(top5BestIssueRooms)
			.build()
			;



		 */

		return ApiResult.<List<IssueBriefResponse>>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 전체를 불러왔습니다.")
			.data(responseList)
			.build();

	}

	// 1. 최신 채팅 시간
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

}

