package com.debateseason_backend_v1.domain.issue;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.domain.user.domain.UserRole;
import com.debateseason_backend_v1.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 이슈 상세 "참여 커뮤니티"(map): 노출 중인 토론 주제에 찬반 투표한 사용자의 커뮤니티를 DB 에서 센다.
 * 예전에는 상세를 연 로그인 사용자를 서버 메모리에 모아서 재시작마다 사라졌다 (웹 세션 제보, 사용자 결정 2026-10-25).
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("이슈 참여 커뮤니티")
class IssueVoterCommunitiesTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JdbcTemplate jdbc;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private IssueJpaRepository issueJpaRepository;
	@Autowired
	private ChatRoomRepository chatRoomRepository;

	private long user(Long communityId) {
		jdbc.update("INSERT INTO users (social_type, identifier, status, role) VALUES ('KAKAO', ?, 'ACTIVE', 'USER')",
			"vt-" + System.nanoTime());
		long userId = jdbc.queryForObject("SELECT MAX(user_id) FROM users", Long.class);
		if (communityId != null) {
			jdbc.update("INSERT INTO profile (user_id, nickname, community_id) VALUES (?, ?, ?)", userId, "n" + userId, communityId);
		}
		return userId;
	}

	private void vote(long userId, long roomId, String opinion) {
		jdbc.update("INSERT INTO user_chat_room (user_id, chat_room_id, opinion) VALUES (?, ?, ?)", userId, roomId, opinion);
	}

	private long thread(IssueEntity issue, ChatRoomStatus status) {
		return chatRoomRepository.save(ChatRoom.builder().issueEntity(issue).title("주제").roomType(ChatRoomType.THREAD).status(status)
			.build()).getId();
	}

	private JsonNode map(long issueId, String bearer) throws Exception {
		var request = MockMvcRequestBuilders.get("/api/v1/issue").param("issue-id", String.valueOf(issueId));
		if (bearer != null) {
			request = request.header("Authorization", bearer);
		}
		var result = mockMvc.perform(request).andReturn();
		String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
		assertThat(result.getResponse().getStatus()).as(body).isEqualTo(200);
		return objectMapper.readTree(body).path("data").path("map");
	}

	@Test
	@DisplayName("투표자 기준·사람 단위 중복 제거·많은 순. 숨긴 주제의 투표와 커뮤니티 없는 투표자는 빠진다")
	void countsVoters() throws Exception {
		IssueEntity issue = issueJpaRepository.save(IssueEntity.builder().title("투표 이슈").majorCategory("사회").build());
		long a = thread(issue, ChatRoomStatus.OPEN);
		long b = thread(issue, ChatRoomStatus.CLOSED);
		long hidden = thread(issue, ChatRoomStatus.HIDDEN);

		long dc1 = user(1L);
		long dc2 = user(1L);
		long bobae = user(13L);
		long noCommunity = user(null);
		vote(dc1, a, "AGREE");
		vote(dc1, b, "DISAGREE");   // 두 주제에 투표해도 한 사람
		vote(dc2, a, "DISAGREE");
		vote(bobae, hidden, "AGREE"); // 숨긴 주제 투표는 세지 않는다
		vote(noCommunity, a, "AGREE");

		JsonNode map = map(issue.getId(), null);

		assertThat(map.size()).isEqualTo(1);
		assertThat(map.path("community/icons/dcinside.png").asInt()).isEqualTo(2);
	}

	@Test
	@DisplayName("투표가 없으면 빈 객체, 비로그인과 로그인(프로필 없는 사용자 포함) 결과가 같다")
	void sameForEveryone() throws Exception {
		IssueEntity issue = issueJpaRepository.save(IssueEntity.builder().title("빈 이슈").majorCategory("사회").build());
		thread(issue, ChatRoomStatus.OPEN);
		long withoutProfile = user(null);

		assertThat(map(issue.getId(), null).size()).isZero();
		// 예전에는 프로필 없는 로그인 사용자의 상세 조회가 404 NOT_FOUND_PROFILE 이었다
		assertThat(map(issue.getId(), "Bearer " + jwtUtil.createAccessToken(withoutProfile, UserRole.USER)).size()).isZero();
	}
}
