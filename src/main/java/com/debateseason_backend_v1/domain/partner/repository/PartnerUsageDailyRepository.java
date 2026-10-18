package com.debateseason_backend_v1.domain.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.debateseason_backend_v1.domain.partner.entity.PartnerUsageDaily;

public interface PartnerUsageDailyRepository extends JpaRepository<PartnerUsageDaily, PartnerUsageDaily.Key> {
}
