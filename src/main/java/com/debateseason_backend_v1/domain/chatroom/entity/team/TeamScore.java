package com.debateseason_backend_v1.domain.chatroom.entity.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamScore {

	private String team;
	private int total;
	private int logic;
	private int attitude;
	private String mvp;
}
