package com.debateseason_backend_v1.domain.ontology.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이슈 공개 결과")
public record IssueLaunchResponse(
	Long issueId,
	@Schema(description = "게시된 위키 리비전") Long wikiRevisionId,
	@Schema(description = "공개 뒤 상태 (PUBLISHED)") String status,
	@Schema(description = "타임라인 사건 수") int eventCount,
	@Schema(description = "같은 proposalId 재호출이면 false") boolean changed
) {
}
