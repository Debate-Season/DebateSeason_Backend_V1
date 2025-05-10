package com.debateseason_backend_v1.domain.chat.infrastructure.report;

import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportReasonType;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportStatus;
import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportTargetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Entity(name = "report")
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType;

    @Column(nullable = false)
    private Long reporterId;

    @ElementCollection
    @CollectionTable(
            name = "chat_report_reasons",
            joinColumns = @JoinColumn(name = "report_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "reportReasonTypes")
    private Set<ReportReasonType> reportReasonTypes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    @Column(length = 500)
    private String adminComment;

    private Long processedById;

}