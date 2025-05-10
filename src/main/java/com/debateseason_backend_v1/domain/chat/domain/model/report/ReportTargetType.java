package com.debateseason_backend_v1.domain.chat.domain.model.report;

public enum ReportTargetType {

    CHAT("채팅"),
    POST("게시글");

    ReportTargetType(String description) {
        this.description = description;
    }

    private final String description;

    public String getDescription() {
        return description;
    }

}
