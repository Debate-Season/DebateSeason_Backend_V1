package com.debateseason_backend_v1.domain.chat.domain.model.report;

public enum ReportStatus {
        NONE("신고 되지 않음"),
        PENDING("신고 접수됨"),
        PROCESSING("신고 처리중"),
        ACCEPTED("신고 승인됨"),
        REJECTED("신고 거부됨");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
