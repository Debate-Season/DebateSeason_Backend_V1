package com.debateseason_backend_v1.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpinionType {
	AGREE("찬성"),
	DISAGREE("반대"),
	NEUTRAL("중립");

	private final String description;


}
