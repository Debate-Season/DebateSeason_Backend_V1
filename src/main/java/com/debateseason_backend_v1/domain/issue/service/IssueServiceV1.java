package com.debateseason_backend_v1.domain.issue.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;
import com.debateseason_backend_v1.domain.issue.PaginationDTO;
import com.debateseason_backend_v1.domain.issue.model.response.IssueDetailResponse;
import com.debateseason_backend_v1.domain.issue.model.request.IssueRequest;
import com.debateseason_backend_v1.domain.issue.model.CommunityRecords;
import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.chat.infrastructure.chat.ChatJpaRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.IssueRepository;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserIssueRepository;
import com.debateseason_backend_v1.domain.user.infrastructure.UserJpaRepository;
import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.user.infrastructure.UserEntity;
import com.debateseason_backend_v1.domain.repository.entity.UserIssue;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;

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
	private final UserJpaRepository userRepository;

	private final ProfileRepository profileRepository;
	private final ChatJpaRepository chatRepository;

	private final ChatRoomRepository chatRoomRepository;



	// 1. save 이슈방
	public ApiResult<Object> save(IssueRequest issueRequest) {

		Issue issue = Issue.builder()
			.title(issueRequest.getTitle())
			.majorCategory(issueRequest.getMajorCategory())
			//.middleCategory(issueDTO.getMiddleCategory())
			.build();
		issueRepository.save(issue);

		return ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueRequest.getTitle() + "가 생성되었습니다.")
			.build();

	}

	//2. fetch 단건 이슈방
	@Transactional
	public ApiResult<IssueDetailResponse> fetch2(Long issueId, Long userId, Long ChatRoomId) {

		Long chats = 0L;

		List<Object[]> object = userIssueRepository.findByIssueIdAndUserId(issueId,userId);

		String bookMarkState = "no";
		// 첫 방문 항상 bookmarkState는 no
		if(!object.isEmpty()){
			Object[] object2 = object.get(0);
			bookMarkState = (String)object2[0];

		}


		// issue_id, title, COUNT(ui.issue_id) AS bookmarks
		List<Object[]> fetchIssue = issueRepository.findSingleIssueWithBookmarks(issueId);

		// 수정. issue없는 에러 조회
		if(issueRepository.findById(issueId).isEmpty()){
			throw new CustomException(ErrorCode.NOT_FOUND_ISSUE);
		}

		String issueTitle = "this is error" ;
		Long bookMarks = 0L;
		for(Object[] obj : fetchIssue){
			issueTitle = (String)obj[1];
			bookMarks = (Long)obj[2];
		}

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
			chatRoomIds = userChatRoomRepository.findTop3ChatRoomIdsByIssueId(issueId);
		}
		else{
			// 그 이후 페이지
			chatRoomIds = userChatRoomRepository.findTop3ChatRoomIdsByIssueIdAndChatRoomId(issueId,ChatRoomId);
		}

		// 채팅방은 없으면 없는대로 반환을 한다
		if(chatRoomIds.isEmpty()){

			IssueDetailResponse issueDetailResponse = IssueDetailResponse.builder()
				.title(issueTitle)
				.bookMarkState(bookMarkState)
				.bookMarks(bookMarks)
				.map(sortedMap)
				.build();


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
		List<ResponseWithTimeAndOpinion> chatRooms =  userChatRoomRepository.findChatRoomAggregates(chatRoomIds).stream().map(
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
			;

		// chat_room_id, opinion AS opinion

		List<Object[]> opinions =  userChatRoomRepository.findUserChatRoomOpinions(userId,chatRoomIds);
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

		// 오늘 신규 대화
		chats = issueRepository.countChatsTodayByIssueId(issueId);


		// 1-5 IssueDAO만들기
		IssueDetailResponse issueDetailResponse = IssueDetailResponse.builder()
			.title(issueTitle)
			.bookMarkState(bookMarkState)
			.bookMarks(bookMarks)
			.chats(chats)
			.map(sortedMap)
			.chatRoomMap(chatRooms)
			.build();

		//

		return ApiResult.<IssueDetailResponse>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueId + "조회")
			.data(issueDetailResponse)
			.build();


	}

	// 즐겨찾기 등록하기
	@Transactional // 만약에 하나로 문제가 생기면 바로 Rollback
	public ApiResult<String> bookMark(Long issueId, Long userId){

		UserEntity user = userRepository.findById(userId).orElseThrow(
			()-> new CustomException(ErrorCode.NOT_FOUND_USER)
		)
		;

		Issue issue = issueRepository.findById(issueId).orElseThrow(
			() -> new CustomException(ErrorCode.NOT_FOUND_ISSUE)
		);

		UserIssue fetchUserIssue= userIssueRepository.findByIssueAndUser(issue,user);


		// 중복 등록 방지
		if(fetchUserIssue!=null){

			String bookMarkState;

			// 더티 체킹하자
			if(fetchUserIssue.getBookmark().equals("yes")){
				// no로 바꾸자
				bookMarkState="no";
				fetchUserIssue.setBookmark("no");
			}
			else{
				bookMarkState="yes";
				fetchUserIssue.setBookmark("yes");
			}
			return ApiResult.<String>builder()
				.status(200)
				.code(ErrorCode.SUCCESS)
				.data("이슈방의 즐겨찾기가 "+bookMarkState+"로 바꿔었습니다")
				.build();


		}

		// 첫 북마크일 경우 새로 등록을 한다.
		UserIssue userIssue = new UserIssue();
		userIssue.setUser(user);
		userIssue.setIssue(issue);
		userIssue.setBookmark("yes");

		userIssueRepository.save(userIssue);

		return ApiResult.<String>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.data("이슈방 "+issueId+"을 관심등록 했습니다.")
			.build();

	}

	// 4. issueMap 가져오기
	// issue_id, title, major_category, (middle_category), created_at
	public ApiResult<PaginationDTO> fetchIssueMap(Long page, String majorCategory//, String middleCategory
	)
	{

		// 일단 issueId 여러개를 가져온다.
		List<Long> issueIds;

		if(majorCategory==null){

			// 전체 불러오기
			if(page == null){
				issueIds = issueRepository.findTop6Issues();
			}
			else{
				issueIds = issueRepository.findTop6IssuesByPage(page);
			}

		}else{
			if(page == null){
				issueIds = issueRepository.findTop6IssuesByCategory(majorCategory);
			}
			else{
				issueIds = issueRepository.findTop6IssuesByPageAndCategory(majorCategory,page);
			}
		}



		if (issueIds.isEmpty()){
			// 페이지네이션 오류
			throw new CustomException(ErrorCode.PAGE_OUT_OF_RANGE);
		}

		// issue_id, title, created_at, chat_room_count, COUNT(ui2.issue_id) AS bookmarks
		// issueIds를 넣어줌으로써 어떠한 page, category에 상관없이 하나의 쿼리로 커버가 가능하다.
		List<IssueBriefResponse> issueBriefResponse = issueRepository.findIssuesWithBookmarks(issueIds).stream().map(
			e->{
				Long issueId = (Long)e[0];
				String title = (String)e[1];

				String time = e[2].toString();
				String result = time.split("\\.")[0];
				LocalDateTime createdAt = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				Long chatRoomCount = (Long)e[3];
				Long bookMarkCount = (Long)e[4];

				return IssueBriefResponse.builder()
					.issueId(issueId)
					.title(title)
					.createdAt(createdAt)
					.countChatRoom(chatRoomCount)
					.bookMarks(bookMarkCount)
					.build()
					;
			}
		).toList();

		PaginationDTO paginationDTO = new PaginationDTO();
		paginationDTO.setItems(issueBriefResponse);

		return ApiResult.<PaginationDTO>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 불러왔습니다.")
			.data(paginationDTO)
			.build();

	}

	// 구버전
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
				.countChatRoom(chatRoomRepository.countByIssue(issueList.get(i)))
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

	/*
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

	 */

	private String findLastestChatTime(Long chatRoomId){
		Optional<LocalDateTime> latestChat = chatRepository.findMostRecentMessageTimestampByChatRoomId(chatRoomId);

		String time = null;

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
