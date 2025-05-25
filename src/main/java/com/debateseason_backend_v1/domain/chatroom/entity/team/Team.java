package com.debateseason_backend_v1.domain.chatroom.entity.team;

import lombok.Getter;

@Getter
public enum Team {
	agree("agree"),
	disagree("disagree");

	private final String opinion;
	Team(String opinion) {
		this.opinion=opinion;
	}
}
