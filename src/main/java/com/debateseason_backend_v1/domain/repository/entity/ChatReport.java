package com.debateseason_backend_v1.domain.repository.entity;


import com.debateseason_backend_v1.common.enums.ReportReasonType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_report")
@Entity
public class ChatReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;
    
    @OneToMany(mappedBy = "chatReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatReportReason> chatReportReasons = new ArrayList<>();

    @Column(name = "reason_detail", length = 100)
    private String reasonDetail;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ReportStatus {
        PENDING("접수"),
        REVIEWING("검토중"),
        ACCEPTED("승인"),
        REJECTED("거부");

        private final String description;

        ReportStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public void updateStatus(ReportStatus status) {
        this.status = status;
    }
    
    public void addReportReason(ReportReason reason) {
        ChatReportReason chatReportReason = ChatReportReason.of(this, reason);
        this.chatReportReasons.add(chatReportReason);
    }
    
    public List<ReportReasonType> getReasonTypes() {
        if (chatReportReasons == null) {
            return new ArrayList<>();
        }
        return this.chatReportReasons.stream()
                .map(crr -> crr.getReportReason().getReasonType())
                .collect(Collectors.toList());
    }
    
    public ReportReasonType getReasonType() {
        if (chatReportReasons == null || chatReportReasons.isEmpty()) {
            return ReportReasonType.OTHER;
        }
        return chatReportReasons.get(0).getReportReason().getReasonType();
    }
}

