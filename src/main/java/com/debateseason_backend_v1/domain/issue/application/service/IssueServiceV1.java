package com.debateseason_backend_v1.domain.issue.application.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.infrastructure.entity.manager.ChatRoomPaginationManager;
import com.debateseason_backend_v1.domain.chatroom.infrastructure.entity.processor.ChatRoomProcessor;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.type.ResponseWithTimeAndOpinion;
import com.debateseason_backend_v1.domain.issue.community.CommunitySorterV4;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueMapper;
import com.debateseason_backend_v1.domain.issue.model.response.PaginationDTO;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.manager.IssueProcessorManager;
import com.debateseason_backend_v1.domain.issue.infrastructure.manager.IssuePaginationManager;
import com.debateseason_backend_v1.domain.issue.application.repository.IssueRepository;
import com.debateseason_backend_v1.domain.issue.model.request.IssueRequest;
import com.debateseason_backend_v1.domain.issue.mapper.IssueBriefResponse;
import com.debateseason_backend_v1.domain.issue.mapper.IssueDetailResponse;
import com.debateseason_backend_v1.domain.profile.domain.CommunityType;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileEntity;
import com.debateseason_backend_v1.domain.profile.infrastructure.ProfileJpaRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserIssueRepository;
import com.debateseason_backend_v1.domain.repository.entity.UserIssue;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;
import com.debateseason_backend_v1.domain.user.infrastructure.UserEntity;
import com.debateseason_backend_v1.domain.user.infrastructure.UserJpaRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class IssueServiceV1 {

	private final IssueJpaRepository issueJpaRepository; // 제거 예정.
	private final IssueRepository issueRepository;

	private final UserIssueRepository userIssueRepository;
	private final UserChatRoomRepository userChatRoomRepository;
	private final UserJpaRepository userRepository;

	private final ProfileJpaRepository profileRepository;

	private final ChatRoomRepository chatRoomRepository;

	// 페이지네이션을 위한 manager
	private final IssuePaginationManager issuePaginationManager;
	private final ChatRoomPaginationManager chatRoomPaginationManager;

	// DB에서 가져온 데이터 -> DTO로 가공
	private final IssueProcessorManager issueProcessorManager;
	private final ChatRoomProcessor chatRoomProcessor;

	// CommunitySorterV2 -> V3로 변경( HashMap에 대한 동기화 문제가 발생할 수 있어서 V3로 변경. )
	private final CommunitySorterV4 communityMananger;

	// 1. save 이슈방
	public ApiResult<Object> save(IssueRequest issueRequest) {

		// JPA 엔티티
		IssueEntity issueEntity = IssueEntity.builder()
			.title(issueRequest.getTitle())
			.majorCategory(issueRequest.getMajorCategory())
			//.middleCategory(issueDTO.getMiddleCategory())
			.build();
		
		issueRepository.save(issueEntity);

		return ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueRequest.getTitle() + "가 생성되었습니다.")
			.build();

	}

	//2. fetch 단건 이슈방
	@Transactional
	public ApiResult<IssueDetailResponse> fetchV2(Long issueId, Long userId, Long ChatRoomId) {

		// 북마크 상태 조회 (비로그인 시 기본값 "no")
		String bookMarkState = "no";
		if (userId != null) {
			List<Object[]> object = userIssueRepository.findByIssueIdAndUserId(issueId, userId);
			if (!object.isEmpty()) {
				Object[] object2 = object.get(0);
				bookMarkState = (String)object2[0];
			}
		}

		//  이슈와 북마크 개수 가져오기
		List<Object[]> fetchIssue = issueRepository.findSingleIssueWithBookmarks(issueId);
		Object[] tmp = fetchIssue.get(0);
		IssueMapper issueMapper = IssueMapper.builder()
			.title((String)tmp[1])
			.bookMarks((long)tmp[2])
			.build();

		// 커뮤니티 맵 (비로그인 시 방문 기록 없이 기존 데이터만 반환)
		if (userId != null) {
			ProfileEntity profile = profileRepository.findByUserId(userId).orElseThrow(
				() -> new CustomException(ErrorCode.NOT_FOUND_PROFILE)
			);

			CommunityType communityType = profile.getCommunityType();
			if (communityType == null) {
				throw new CustomException(ErrorCode.NOT_FOUND_COMMUNITY);
			}

			UserDTO userDTO = new UserDTO();
			userDTO.setCommunity(communityType.getName());
			userDTO.setId(userId);

			communityMananger.record(userDTO, issueId);
		}

		LinkedHashMap<String, Integer> sortedMap = communityMananger.getSortedCommunity(issueId);

		//
		List<Long> chatRoomIds = chatRoomPaginationManager.getChatRoomsByPage(issueId, ChatRoomId);

		// 4. 채팅방을 반환 (없으면 없는대로 반환을 한다)
		long chats = 0L;
		List<ResponseWithTimeAndOpinion> chatRooms = null;

		if (!chatRoomIds.isEmpty()) {
			chatRooms = chatRoomProcessor.getChatRoomWithOpinionCount(chatRoomIds);

			// 로그인 사용자만 개인 투표 상태 조회
			if (userId != null) {
				List<Object[]> opinions = userChatRoomRepository.findUserChatRoomOpinions(userId, chatRoomIds);
				markUserOpinion(opinions, chatRooms);
			}

			chats = issueRepository.countChatsTodayByIssueId(issueId);
		}

		IssueDetailResponse issueDetailResponse = IssueDetailResponse.builder()
			.title(issueMapper.getTitle())
			.bookMarkState(bookMarkState)
			.bookMarks(issueMapper.getBookMarks())
			.chats(chats)
			.map(sortedMap)
			.chatRoomMap(chatRooms)
			.build();


		return ApiResult.<IssueDetailResponse>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueId + "조회")
			.data(issueDetailResponse)
			.build();

	}

	// 3. issueMap 가져오기
	// issue_id, title, major_category, (middle_category), created_at
	public ApiResult<PaginationDTO> fetchIssueMap(Long page, String majorCategory//, String middleCategory
	) {

		// 일단 issueId 여러개를 가져온다.
		List<Long> issueIds = issuePaginationManager.getIssue(majorCategory,page);


		// issue_id, title, created_at, chat_room_count, COUNT(ui2.issue_id) AS bookmarks
		// issueIds를 넣어줌으로써 어떠한 page, category에 상관없이 하나의 쿼리로 커버가 가능하다.

		// 이슈를 북마크 수와 함께 가져오기 -> 수정하기
		List<Object[]> rawIssueBookmark = issueRepository.findIssuesWithBookmarks(issueIds);

		// 후처리
		List<IssueBriefResponse> issueBriefResponse = issueProcessorManager.createIssueBriefResponse(rawIssueBookmark);

		// 응답 DTO - 생성자 주입
		PaginationDTO paginationDTO = new PaginationDTO(issueBriefResponse);

		return ApiResult.<PaginationDTO>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 불러왔습니다.")
			.data(paginationDTO)
			.build();

	}

	// 즐겨찾기 등록하기
	@Transactional // 만약에 하나로 문제가 생기면 바로 Rollback
	public ApiResult<String> bookMark(Long issueId, Long userId) {

		// user 도메인
		UserEntity userEntity = userRepository.findById(userId).orElseThrow(
			() -> new CustomException(ErrorCode.NOT_FOUND_USER)
		); // 없으면, 예외 발생

		IssueEntity issueEntity = issueRepository.findById(issueId); // 없으면, 예외 발생

		UserIssue fetchUserIssueEntity = userIssueRepository.findByIssueEntityAndUser(issueEntity, userEntity);

		String data;

		// 중복 등록 방지
		if (fetchUserIssueEntity != null) {

			String bookMarkState;

			// 더티 체킹하자
			if (fetchUserIssueEntity.getBookmark().equals("yes")) {
				// no로 바꾸자
				bookMarkState = "no";
				fetchUserIssueEntity.setBookmark("no");
			} else {
				bookMarkState = "yes";
				fetchUserIssueEntity.setBookmark("yes");
			}

			//
			data = "이슈방의 즐겨찾기가 " + bookMarkState + "로 바꿔었습니다";

		}
		else{

			// 첫 북마크일 경우 새로 등록을 한다.
			UserIssue userIssueEntity = UserIssue.builder()
				.user(userEntity)
				.issueEntity(issueEntity)
				.bookmark("yes")
				.build()
				;
			userIssueRepository.save(userIssueEntity);

			//
			data = "이슈방 " + issueId + "을 관심등록 했습니다.";
		}



		return ApiResult.<String>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.data(data)
			.build();

	}

	private void markUserOpinion(List<Object[]> opinions,List<ResponseWithTimeAndOpinion> chatRooms){

		if (!opinions.isEmpty()) {
			for (Object[] obj : opinions) {
				if (obj[0] != null) {
					// 투표를 하면 무조건 chatRoomId가 null이 아니다.
					Long chatRoomId = (Long)obj[0];
					String opinion = (String)obj[1];

					for (ResponseWithTimeAndOpinion e : chatRooms) {
						if (e.getChatRoomId() == chatRoomId) {
							e.setOpinion(opinion);
							break;
						}
					}
				}

			}
		}

	}

	// 구버전
	public ApiResult<List<IssueBriefResponse>> fetchV1() {
		List<IssueEntity> issueEntityList = issueJpaRepository.findAll();

		// Gson,JSONArray이 없어서 Map으로 반환을 한다.
		List<IssueBriefResponse> responseList = new ArrayList<>();

		// loop를 돌면서, issueId에 해당하는 chatRoom을 count 한다.
		for (int i = 0; i < issueEntityList.size(); i++) {

			Long id = issueEntityList.get(i).getId();
			IssueBriefResponse response = IssueBriefResponse.builder()
				.issueId(id)
				.title(issueEntityList.get(i).getTitle())
				.createdAt(issueEntityList.get(i).getCreatedAt())
				.countChatRoom(chatRoomRepository.countByIssueEntity(issueEntityList.get(i)))
				.build();
			responseList.add(response);
		}


		return ApiResult.<List<IssueBriefResponse>>builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 전체를 불러왔습니다.")
			.data(responseList)
			.build();

	}




}

