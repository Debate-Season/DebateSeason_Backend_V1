package com.debateseason_backend_v1.domain.ontology.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.ontology.entity.AdminChangeLog;

public interface AdminChangeLogRepository extends JpaRepository<AdminChangeLog, Long> {

	Optional<AdminChangeLog> findByProposalIdAndAction(String proposalId, AdminChangeLog.Action action);
}
