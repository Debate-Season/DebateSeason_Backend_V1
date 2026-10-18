package com.debateseason_backend_v1.domain.sitemap;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomType;
import com.debateseason_backend_v1.domain.issue.infrastructure.entity.IssueEntity;
import com.debateseason_backend_v1.domain.issue.infrastructure.repository.IssueJpaRepository;
import com.debateseason_backend_v1.domain.issue.model.IssueStatus;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.repository.entity.ChatRoom;
import com.debateseason_backend_v1.fixtures.wiki.WikiFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 사이트맵 피드: 익명 공개, 노출 규칙, lastmod 정의, 커서 페이징.
 * 테스트 DB 를 다른 테스트와 공유하므로 이 테스트가 만든 id 만 본다 (cursor 를 그 직전으로 둔다).
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("사이트맵 피드")
class SitemapApiTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private IssueJpaRepository issueJpaRepository;
	@Autowired
	private ChatRoomRepository chatRoomRepository;
	@Autowired
	private JdbcTemplate jdbc;

	private IssueEntity issue(IssueStatus status) {
		IssueEntity issue = issueJpaRepository.save(IssueEntity.builder().title("사이트맵 " + status).status(status).build());
		WikiFixture.publish(jdbc, issue.getId()); // 상태 규칙만 보려고 모두 위키 게시본을 붙인다
		return issue;
	}

	private ChatRoom room(IssueEntity issue, ChatRoomType type, ChatRoomStatus status) {
		return chatRoomRepository.save(ChatRoom.builder().issueEntity(issue).title("주제").roomType(type).status(status).build());
	}

	private JsonNode get(String path, long cursor, int size) throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(path)
			.param("cursor", String.valueOf(cursor)).param("size", String.valueOf(size))).andReturn();
		assertThat(result.getResponse().getStatus()).isEqualTo(200);
		return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).path("data");
	}

	private static List<Long> ids(JsonNode page, String field) {
		List<Long> ids = new ArrayList<>();
		page.path("items").forEach(item -> ids.add(item.path(field).asLong()));
		return ids;
	}

	@Test
	@DisplayName("이슈: 익명 200, PUBLISHED·DORMANT 만, id 오름차순")
	void issueVisibility() throws Exception {
		IssueEntity published = issue(IssueStatus.PUBLISHED);
		IssueEntity dormant = issue(IssueStatus.DORMANT);
		IssueEntity archived = issue(IssueStatus.ARCHIVED);
		IssueEntity draft = issue(IssueStatus.DRAFT);

		List<Long> ids = ids(get("/api/v1/sitemap/issues", published.getId() - 1, 5000), "issueId");

		assertThat(ids).contains(published.getId(), dormant.getId())
			.doesNotContain(archived.getId(), draft.getId())
			.isSorted();
	}

	@Test
	@DisplayName("이슈 updatedAt: 노출 중인 토론 주제가 더 늦게 추가됐으면 그 시각, 숨긴 주제는 세지 않는다")
	void issueUpdatedAtFollowsVisibleTopics() throws Exception {
		IssueEntity issue = issue(IssueStatus.PUBLISHED);
		ChatRoom visible = room(issue, ChatRoomType.THREAD, ChatRoomStatus.OPEN);
		ChatRoom preparing = room(issue, ChatRoomType.THREAD, ChatRoomStatus.PREPARING);
		LocalDateTime base = LocalDateTime.of(2026, 1, 1, 9, 0);
		jdbc.update("UPDATE issue SET created_at = ?, updated_at = ? WHERE issue_id = ?", base, base, issue.getId());
		jdbc.update("UPDATE chat_room SET created_at = ? WHERE chat_room_id = ?", base.plusDays(3), visible.getId());
		jdbc.update("UPDATE chat_room SET created_at = ? WHERE chat_room_id = ?", base.plusDays(9), preparing.getId());

		JsonNode first = get("/api/v1/sitemap/issues", issue.getId() - 1, 1).path("items").get(0);

		assertThat(first.path("issueId").asLong()).isEqualTo(issue.getId());
		assertThat(first.path("updatedAt").asText()).isEqualTo("2026-01-04T09:00:00");
	}

	@Test
	@DisplayName("토론 주제: 스레드·레거시만, OPEN·CLOSED 만, 소속 이슈가 노출 중이어야 한다")
	void threadVisibility() throws Exception {
		IssueEntity issue = issue(IssueStatus.PUBLISHED);
		IssueEntity archived = issue(IssueStatus.ARCHIVED);
		ChatRoom container = room(issue, ChatRoomType.CONTAINER, ChatRoomStatus.OPEN);
		ChatRoom open = room(issue, ChatRoomType.THREAD, ChatRoomStatus.OPEN);
		ChatRoom closed = room(issue, ChatRoomType.THREAD, ChatRoomStatus.CLOSED);
		ChatRoom legacy = room(issue, null, ChatRoomStatus.OPEN);
		ChatRoom preparing = room(issue, ChatRoomType.THREAD, ChatRoomStatus.PREPARING);
		ChatRoom hidden = room(issue, ChatRoomType.THREAD, ChatRoomStatus.HIDDEN);
		ChatRoom underArchived = room(archived, ChatRoomType.THREAD, ChatRoomStatus.OPEN);

		List<Long> ids = ids(get("/api/v1/sitemap/threads", container.getId() - 1, 5000), "threadId");

		assertThat(ids).contains(open.getId(), closed.getId(), legacy.getId())
			.doesNotContain(container.getId(), preparing.getId(), hidden.getId(), underArchived.getId())
			.isSorted();
	}

	@Test
	@DisplayName("lastActivityAt: 마지막 CHAT 시각 (이관 후 thread_id, 레거시 chat_room_id 모두), 채팅 없으면 생성 시각")
	void lastActivity() throws Exception {
		IssueEntity issue = issue(IssueStatus.PUBLISHED);
		ChatRoom container = room(issue, ChatRoomType.CONTAINER, ChatRoomStatus.OPEN);
		ChatRoom thread = room(issue, ChatRoomType.THREAD, ChatRoomStatus.OPEN);
		ChatRoom legacy = room(issue, null, ChatRoomStatus.OPEN);
		ChatRoom quiet = room(issue, ChatRoomType.THREAD, ChatRoomStatus.OPEN);
		LocalDateTime t = LocalDateTime.of(2026, 10, 18, 22, 0);
		jdbc.update("UPDATE chat_room SET created_at = ? WHERE chat_room_id = ?", t.minusDays(30), quiet.getId());

		String insert = "INSERT INTO chat (chat_room_id, thread_id, message_type, time_stamp) VALUES (?, ?, ?, ?)";
		jdbc.update(insert, container.getId(), thread.getId(), "CHAT", t);
		jdbc.update(insert, container.getId(), thread.getId(), "CHAT", t.plusMinutes(14));
		jdbc.update(insert, container.getId(), thread.getId(), "JOIN", t.plusHours(5)); // 입장은 활동이 아니다
		jdbc.update(insert, legacy.getId(), null, "CHAT", t.minusDays(2));

		JsonNode items = get("/api/v1/sitemap/threads", thread.getId() - 1, 5000).path("items");

		assertThat(lastActivityOf(items, thread.getId())).isEqualTo("2026-10-18T22:14:00");
		assertThat(lastActivityOf(items, legacy.getId())).isEqualTo("2026-10-16T22:00:00");
		assertThat(lastActivityOf(items, quiet.getId())).isEqualTo("2026-09-18T22:00:00");
	}

	private static String lastActivityOf(JsonNode items, long threadId) {
		for (JsonNode item : items) {
			if (item.path("threadId").asLong() == threadId) {
				return item.path("lastActivityAt").asText();
			}
		}
		throw new AssertionError("피드에 없음: " + threadId);
	}

	@Test
	@DisplayName("커서 페이징: nextCursor 를 따라가면 빠짐없이 한 번씩, 마지막 페이지는 nextCursor=null")
	void cursorPaging() throws Exception {
		List<Long> created = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			created.add(issue(IssueStatus.PUBLISHED).getId());
		}

		List<Long> seen = new ArrayList<>();
		long cursor = created.get(0) - 1;
		JsonNode page;
		int calls = 0;
		do {
			page = get("/api/v1/sitemap/issues", cursor, 2);
			seen.addAll(ids(page, "issueId"));
			cursor = page.path("nextCursor").asLong();
			calls++;
		} while (!page.path("nextCursor").isNull() && calls < 100);

		assertThat(seen).containsSubsequence(created).doesNotHaveDuplicates();
		assertThat(page.path("nextCursor").isNull()).isTrue();
	}

	@Test
	@DisplayName("위키 게시본이 없는 이슈와 그 토론 주제는 피드에 없다")
	void withoutWiki() throws Exception {
		IssueEntity bare = issueJpaRepository.save(IssueEntity.builder().title("위키 없음").status(IssueStatus.PUBLISHED).build());
		ChatRoom thread = room(bare, ChatRoomType.THREAD, ChatRoomStatus.OPEN);

		assertThat(ids(get("/api/v1/sitemap/issues", bare.getId() - 1, 5000), "issueId")).doesNotContain(bare.getId());
		assertThat(ids(get("/api/v1/sitemap/threads", thread.getId() - 1, 5000), "threadId")).doesNotContain(thread.getId());
	}

	@Test
	@DisplayName("cursor·size 없이 불러도 된다")
	void defaults() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/sitemap/threads")).andReturn();

		assertThat(result.getResponse().getStatus()).isEqualTo(200);
	}
}
