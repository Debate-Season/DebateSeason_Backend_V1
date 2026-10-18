package com.debateseason_backend_v1.domain.ontology.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog;
import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog.Action;
import com.debateseason_backend_v1.domain.ontology.repository.AdminChangeLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/** admin_change_log 쓰기와 before/after JSON 변환. */
@Component
@RequiredArgsConstructor
public class ChangeLogWriter {

	private final AdminChangeLogRepository changeLogRepository;
	private final ObjectMapper objectMapper;

	public AdminChangeLog record(Action action, String entityType, Long entityId, Object before, Object after,
		String reason, String proposalId, String decidedBy, String approver) {

		return changeLogRepository.save(AdminChangeLog.builder()
			.action(action)
			.entityType(entityType)
			.entityId(entityId)
			.beforeValue(write(before))
			.afterValue(write(after))
			.reason(reason)
			.proposalId(proposalId)
			.decidedBy(decidedBy)
			.approver(approver)
			.decidedAt(LocalDateTime.now())
			.build());
	}

	public String write(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("변경 이력 직렬화 실패", e);
		}
	}

	public JsonNode read(String value) {
		try {
			return objectMapper.readTree(value == null ? "{}" : value);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("변경 이력 역직렬화 실패", e);
		}
	}
}
