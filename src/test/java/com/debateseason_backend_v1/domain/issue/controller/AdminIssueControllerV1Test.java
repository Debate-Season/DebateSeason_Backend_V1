package com.debateseason_backend_v1.domain.issue.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.nio.charset.Charset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.issue.model.request.IssueRequest;
import com.debateseason_backend_v1.domain.issue.service.IssueServiceV1;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@WebMvcTest(AdminIssueControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminIssueControllerV1Test {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private IssueServiceV1 issueServiceV1;

	@Test
	@DisplayName("이슈방을 만듭니다(ADMIN)")
	public void saveIssue() throws JsonProcessingException {
		// given

		// IssueDTO
		IssueRequest issueRequest = new IssueRequest();
		issueRequest.setTitle("중국 Deepseek 개발");

		String json = objectMapper.writeValueAsString(issueRequest);

		// 응답값
		ApiResult<Object> response = ApiResult.builder()
			.status(200)
			.code(ErrorCode.SUCCESS)
			.message("이슈방 " + issueRequest.getTitle() + "가 생성되었습니다.")
			.build();

		// When & Then

		// RESPONSE
		Mockito.when(issueServiceV1.save(Mockito.any(IssueRequest.class)))
			.thenReturn(response);

		// REQUEST
		try {

			mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/issue")
					.contentType(MediaType.APPLICATION_JSON)
					.characterEncoding(Charset.forName("UTF-8"))
					.content(json)
				)
				.andDo(print())
				.andExpect(MockMvcResultMatchers.status().isOk());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
