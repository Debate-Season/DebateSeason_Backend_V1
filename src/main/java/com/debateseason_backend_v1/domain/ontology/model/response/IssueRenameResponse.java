package com.debateseason_backend_v1.domain.ontology.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이슈명 변경 결과")
public record IssueRenameResponse(

	Long issueId,

	@Schema(description = "바꾸기 전 이름. 롤백할 때 이 값으로 다시 부른다")
	String previousTitle,

	String title,

	@Schema(description = "이미 그 이름이었으면 false")
	boolean changed
) {
}
