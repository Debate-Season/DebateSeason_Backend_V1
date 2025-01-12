package com.debateseason_backend_v1.domain.issue.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.dto.ChatRoomDAO;
import com.debateseason_backend_v1.domain.issue.dto.IssueDAO;
import com.debateseason_backend_v1.domain.issue.dto.IssueDTO;
import com.debateseason_backend_v1.domain.issue.model.CommunityRecords;
import com.debateseason_backend_v1.domain.issue.model.response.IssueResponse;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.CommunityRepository;
import com.debateseason_backend_v1.domain.repository.IssueRepository;
import com.debateseason_backend_v1.domain.repository.ProfileCommunityRepository;
import com.debateseason_backend_v1.domain.repository.ProfileRepository;
import com.debateseason_backend_v1.domain.repository.UserChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.UserIssueRepository;
import com.debateseason_backend_v1.domain.repository.UserRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.repository.entity.Community;
import com.debateseason_backend_v1.domain.repository.entity.Issue;
import com.debateseason_backend_v1.domain.repository.entity.Profile;
import com.debateseason_backend_v1.domain.repository.entity.ProfileCommunity;
import com.debateseason_backend_v1.domain.repository.entity.User;
import com.debateseason_backend_v1.domain.repository.entity.UserChatRoom;
import com.debateseason_backend_v1.domain.user.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class IssueServiceV1 {

	private final IssueRepository issueRepository;
	private final UserIssueRepository userIssueRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserChatRoomRepository userChatRoomRepository;
	private final UserRepository userRepository;

	private final ProfileRepository profileRepository;
	private final ProfileCommunityRepository profileCommunityRepository;
	private final CommunityRepository communityRepository;

	private final ObjectMapper objectMapper;

	// 1. save 이슈방
	public ApiResult<Object> save(IssueDTO issueDTO) {

		Issue issue = Issue.builder()
			.title(issueDTO.getTitle())
			.build();
		issueRepository.save(issue);

		ApiResult<Object> response = ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueDTO.getTitle() + "가 생성되었습니다.")
			.build();

		return response;

	}

	//2. fetch 단건 이슈방
	@Transactional
	public ApiResult<Object> fetch(Long issueId, Long userId) {

		// 1. 이슈방 불러오기
		Issue issue = null;
		try{
			issue = issueRepository.findById(issueId).orElseThrow(
				() -> new NullPointerException("There is no " + issueId)
			);

		}
		catch (NullPointerException | IllegalArgumentException e){
			return ApiResult.builder()
				.status(400)
				.code(ErrorCode.BAD_REQUEST)
				.message("선택하신 이슈방은 존재하지 않습니다.")
				.build();

		}

		// 2. User 찾기
		/*
		User user = userRepository.findById(userId).orElseThrow(
			() -> new RuntimeException("There is no " + userId)
		);
		 */

		// 2. Community 찾는 과정
		/*
			userId로 Profile 찾기
			Profile.id로 ProfileCommunity.communityId 찾기
			위에서 찾은 communityId로 Community 테이블에서 community,name 가져오기
			select 쿼리만해도 3번이 나가는데 -> 비효율적임.
		 */

		Profile profile = profileRepository.findByUserId(userId).orElseThrow(
			() -> new RuntimeException("There is no "+ userId)
		);

		ProfileCommunity profileCommunity = profileCommunityRepository.findByProfileId(profile.getId()).orElseThrow(
			() -> new RuntimeException("There is no "+ profile.getId())
		);

		Community community = communityRepository.findById(profileCommunity.getCommunityId()).orElseThrow(
			() -> new RuntimeException("There is no "+ profileCommunity.getCommunityId())
		);


		UserDTO userDTO = new UserDTO();
		userDTO.setCommunity(community.getName());
		userDTO.setId(userId);

		CommunityRecords.record(userDTO, issueId);
		Map<String, Integer> map = CommunityRecords.getSortedCommunity(issueId);
		Set<String> keySet = map.keySet();

		log.info("Ok1");
		// 1-1. User 불러오기. 참여 커뮤니티를 보여주기 위함임(내림차순으로) <-  즐겨찾기
		/*
		List<UserIssue> userIssueList = userIssueRepository.findByIssue(issue);

		//
		List<User> userList = new ArrayList<>();
		for (UserIssue e : userIssueList) {
			userList.add(e.getUser());
		}

		// Map
		Map<String, Integer> map = new HashMap<>();
		for (User u : userList) {

			String key = u.getCommunity();

			// 1. community에 없는 경우 -> 새로 추가를 한다.
			if (!map.containsKey(key)) {
				map.put(key, 1);
			}
			// 2. community에 있는 경우 -> value를 찾은 후 +1
			else {
				int count = map.get(key);
				map.put(key, count + 1);
			}

		}

		// 1-3. Map을 커뮤니티 count 내림차순으로 정렬

		List<String> keySet = new ArrayList<>(map.keySet());
		keySet.sort((o1, o2) -> map.get(o2).compareTo(map.get(o1)));

		 */

		// LinkedHashMap을 써서 순서를 보장한다.
		Map<String, Integer> sortedMap = new LinkedHashMap<>();

		for (String key : keySet) {
			sortedMap.put(key, map.get(key));
		}

		log.info("Ok2");
		// 1-4. 채팅방도 같이 넘기자. null이어도 가능! <- 수정
		List<ChatRoom> chatRoomList = chatRoomRepository.findByIssue(issue);

		//  나중에 정렬같은거 할 때, 써먹을 듯
		List<ChatRoomDAO> chatRoomDaoList = new ArrayList<>();

		// 이거 때문에 loop가 ㅈㄴ 발생한다 -> 최적화 필요!
		for (ChatRoom c : chatRoomList) {
			// 찬성/반대 조회
			List<UserChatRoom> chatRooms = userChatRoomRepository.findByChatRoom(c);

			int countAgree = 0;
			int countDisagree = 0;

			for (UserChatRoom u : chatRooms) {
				if (u.getOpinion().equals("AGREE")) {
					countAgree++;
				} else if (u.getOpinion().equals("DISAGREE")) {
					countDisagree++;
				}
				// 의견없음 -> 집계안함.
			}

			ChatRoomDAO chatRoomDAO = ChatRoomDAO.builder()
				.chatRoomId(c.getId())
				.title(c.getTitle())
				.content(c.getContent())
				//.issue(c.getIssue())
				.createdAt(c.getCreatedAt())
				.agree(countAgree)
				.disagree(countDisagree)
				.build();

			chatRoomDaoList.add(chatRoomDAO);
		}
		log.info("Ok3");

		List<ChatRoomDAO> chatRoomMap = new ArrayList<>();

		for (int i = 1; i < chatRoomDaoList.size() + 1; i++) {
			chatRoomMap.add(chatRoomDaoList.get(i - 1));
		}

		// 1-5 IssueDAO만들기
		IssueDAO issueDAO = IssueDAO.builder()
			//.issue(issue)
			.map(sortedMap)
			.chatRoomMap(chatRoomMap)
			.build();

		//
		ApiResult<Object> response = ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueId + "조회")
			.data(issueDAO)
			.build();

		return response;

	}

	//3. fetch 전체 이슈방(인덱스 페이지용) <- 나중에 수정할듯
	public ApiResult<Object> fetchAll() {
		List<Issue> issueList = issueRepository.findAll();

		// Gson,JSONArray이 없어서 Map으로 반환을 한다.
		List<IssueResponse> responseList = new ArrayList<>();

		// loop를 돌면서, issueId에 해당하는 chatRoom을 count 한다.
		for (int i = 0; i < issueList.size(); i++) {

			Long id = issueList.get(i).getId();
			IssueResponse response = IssueResponse.builder()
					.issueId(id)
					.title(issueList.get(i).getTitle())
					.createdAt(issueList.get(i).getCreatedAt())
					.countChatRoom(chatRoomRepository.countByIssue(issueList.get(i)))
					.build();
			responseList.add(response);
		}

		ApiResult<Object> response = ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 전체를 불러왔습니다.")
			.data(responseList)
			.build();

		return response;

	}

}
