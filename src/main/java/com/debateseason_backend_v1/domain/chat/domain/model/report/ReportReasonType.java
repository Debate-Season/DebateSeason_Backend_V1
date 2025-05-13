package com.debateseason_backend_v1.domain.chat.domain.model.report;

public enum ReportReasonType {
    ABUSE("욕설/비방/차별/혐"),
    SEXUAL_CONTENT("음란/청소년 유해"),
    FALSE_INFO("허위/불법 정보"),
    SPAM("도배/스팸"),
    PROMOTION("홍보/영리 목적"),
    PRIVACY_LEAK("개인 정보 노출/유포/거래"),
    ETC("기타");

    private final String description;

    ReportReasonType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


}
