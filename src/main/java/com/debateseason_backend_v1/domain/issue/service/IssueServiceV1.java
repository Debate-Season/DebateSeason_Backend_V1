package com.debateseason_backend_v1.domain.issue.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.model.response.ChatRoomResponse;
import com.debateseason_backend_v1.domain.chatroom.model.response.OnlyHomeResponse;
import com.debateseason_backend_v1.domain.chatroom.model.response.Top5BestChatRoom;
import com.debateseason_backend_v1.domain.issue.PaginationDTO;
import com.debateseason_backend_v1.domain.issue.model.response.IssueDetailResponse;
import com.debateseason_backend_v1.domain.issue.model.request.IssueRequest;
import com.debateseason_backend_v1.domain.issue.model.CommunityRecords;
import com.debateseason_backend_v1.domain.issue.model.response.IssueBriefResponse;
import com.debateseason_backend_v1.domain.profile.enums.CommunityType;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.IssueRepository;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserIssueRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.repository.entity.User;
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
	private final UserRepository userRepository;

	private final ProfileRepository profileRepository;

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

		// 1. 이슈방 불러오기
		/*
		Issue issue;
		try{
			issue = issueRepository.findById(issueId).orElseThrow(
				() -> new NullPointerException("There is no " + issueId)
			);

		}
		catch (NullPointerException | IllegalArgumentException e){
			return ApiResult.<IssueDetailResponse>builder()
				.status(400)
				.code(ErrorCode.BAD_REQUEST)
				.message("선택하신 이슈방은 존재하지 않습니다.")
				.build();

		}

		 */

		// issue_id, title, COUNT(ui.issue_id) AS bookmarks
		List<Object[]> fetchIssue = issueRepository.findIssueWithBookmarks(issueId);

		if(fetchIssue==null){
			return ApiResult.<IssueDetailResponse>builder()
				.status(400)
				.code(ErrorCode.BAD_REQUEST)
				.message("선택하신 이슈방은 존재하지 않습니다.")
				.build();
		}

		String issueTitle = "this is error" ;
		Long bookMarks = 0L;
		for(Object[] obj : fetchIssue){
			issueTitle = (String)obj[1];
			bookMarks = (Long)obj[2];
		}

		Profile profile = profileRepository.findByUserId(userId).orElseThrow(
			() -> new RuntimeException("There is no profile "+ userId)
		);

		CommunityType communityType = profile.getCommunityType();
		if (communityType == null) {
			throw new RuntimeException("No community assigned for profile: " + profile.getId());
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
			chatRoomIds = userChatRoomRepository.findTop2ChatRoomIdsByIssueId(issueId);
		}
		else{
			// 그 이후 페이지
			chatRoomIds = userChatRoomRepository.findTop2ChatRoomIdsByIssueIdAndChatRoomId(issueId,ChatRoomId);
		}

		// 채팅방은 없으면 없는대로 반환을 한다
		if(chatRoomIds.isEmpty()){

			IssueDetailResponse issueDetailResponse = IssueDetailResponse.builder()
				.title(issueTitle)
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
		List<ChatRoomResponse> chatRooms =  userChatRoomRepository.findChatRoomAggregates(chatRoomIds).stream().map(
			e->{
				Long chatRoomId = (Long)e[0];
				String title = (String)e[1];
				String content = (String)e[2];

				String time = e[3].toString();
				String result = time.split("\\.")[0];
				LocalDateTime createdAt = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				int agree = Math.toIntExact((Long)e[4]);
				int disagree = Math.toIntExact((Long)e[5]);

				System.out.println("id:" +chatRoomId);

				return ChatRoomResponse.builder()
					.chatRoomId(chatRoomId)
					.title(title)
					.content(content)
					.createdAt(createdAt)
					.opinion("NEUTRAL")
					.agree(agree)
					.disagree(disagree)
					.build();


			}
		).toList()
			;

		// chat_room_id, opinion AS opinion

		List<Object[]> opinions =  userChatRoomRepository.findUserChatRoomOpinions(userId,chatRoomIds);
		if(!opinions.isEmpty()){
			for(Object [] obj : opinions){
				if(obj[0]!=null){
					// 투표를 하면 무조건 chatRoomId가 null이 아니다.
					Long chatRoomId = (Long)obj[0];
					String opinion = (String)obj[1];

					for(ChatRoomResponse e: chatRooms){
						if(e.getChatRoomId()==chatRoomId){
							e.setOpinion(opinion);
							break;
						}
					}
				}


			}
		}


		// 1-5 IssueDAO만들기
		IssueDetailResponse issueDetailResponse = IssueDetailResponse.builder()
			.title(issueTitle)
			.bookMarks(bookMarks)
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

		User user = userRepository.findById(userId).orElseThrow(
			() -> new RuntimeException("There is no UserNumber like " + userId)
		)
		;

		Issue issue = issueRepository.findById(issueId).orElseThrow(
			() -> new RuntimeException("There is no IssueNumber like "+issueId)
		);

		UserIssue userIssue = new UserIssue();
		userIssue.setUser(user);
		userIssue.setIssue(issue);

		userIssueRepository.save(userIssue);

		return ApiResult.<String>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.data("이슈방 "+issueId+"을 관심등록 했습니다.")
			.build();

	}
	
	// 3. issue를 커서 방식으로 가져오기
	public ApiResult<Object> fetchAll(Long userId,Long page) {

		// 일단 issueId 여러개를 가져온다.
		List<Long> issueIds = issueRepository.findIssuesByPage(page*2).stream().toList();

		if (issueIds.isEmpty()){
			return ApiResult.builder()
				.status(200)
				.code(ErrorCode.SUCCESS)
				.message("페이지를 불러올 수 없습니다.  페이지 번호: "+page)
				.build();
		}

		// issue_id, title, created_at, chat_room_count, COUNT(ui2.issue_id) AS bookmarks
		List<IssueBriefResponse> issueBriefRespons = issueRepository.findIssuesWithBookmarks(issueIds).stream().map(
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

		return ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 불러왔습니다.")
			.data(issueBriefRespons)
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
				issueIds = issueRepository.findTop2Issues();
			}
			else{
				issueIds = issueRepository.findTop2IssuesByPage(page);
			}

		}else{
			if(page == null){
				issueIds = issueRepository.findTop2IssuesByCategory(majorCategory);
			}
			else{
				issueIds = issueRepository.findTop2IssuesByPageAndCategory(majorCategory,page);
			}
		}



		if (issueIds.isEmpty()){
			return ApiResult.<PaginationDTO>builder()
				.status(200)
				.code(ErrorCode.SUCCESS)
				.message("페이지를 불러올 수 없습니다.  페이지 번호: "+page)
				.build();
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
	public ApiResult<OnlyHomeResponse> fetchAll() {
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
		List<IssueBriefResponse> top5BestIssueRooms = issueRepository.findIssuesWithBookmarks(issueIds).stream().map(
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



		return ApiResult.<OnlyHomeResponse>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 전체를 불러왔습니다.")
			.data(onlyHomeResponse)
			.build();

	}

}
