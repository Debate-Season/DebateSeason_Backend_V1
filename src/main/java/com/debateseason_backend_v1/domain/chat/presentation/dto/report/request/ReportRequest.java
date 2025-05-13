package com.debateseason_backend_v1.domain.chat.presentation.dto.report.request;


import com.debateseason_backend_v1.domain.chat.domain.model.report.ReportReasonType;
import lombok.Getter;

import java.util.Set;

@Getter
public class ReportRequest {
    Set<ReportReasonType> reasonType;
    String reasonDetail;

}
