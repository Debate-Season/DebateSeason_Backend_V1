package com.debateseason_backend_v1.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReasonType {
    ABUSE("욕설/비방/혐오"),
    ADULT("음란/청소년 유헤"),
    UNLAWFUL("허위 사실/불법 정보"),
    SPAM("도배/스팸"),
    ADVERTISING("홍보/영리 목적"),
    PERSONAL_INFO("개인정보 노출/유포/거래"),
    OTHER("기타");

    private final String description;
}
