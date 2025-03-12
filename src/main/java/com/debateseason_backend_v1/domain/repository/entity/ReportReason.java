package com.debateseason_backend_v1.domain.repository.entity;

import com.debateseason_backend_v1.common.enums.ReportReasonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "report_reason")
public class ReportReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false, unique = true)
    private ReportReasonType reasonType;

    @Column(name = "description", length = 100)
    private String description;

    @OneToMany(mappedBy = "reportReason", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatReportReason> chatReportReasons = new ArrayList<>();

    public static ReportReason from(ReportReasonType reasonType) {
        return ReportReason.builder()
                .reasonType(reasonType)
                .description(reasonType.getDescription())
                .build();
    }
} 