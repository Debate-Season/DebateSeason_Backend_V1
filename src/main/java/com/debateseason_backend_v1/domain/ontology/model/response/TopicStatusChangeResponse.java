package com.debateseason_backend_v1.domain.ontology.model.response;

import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomStatus;

public record TopicStatusChangeResponse(
	Long chatRoomId,
	ChatRoomStatus fromStatus,
	ChatRoomStatus toStatus,
	boolean changed
) {
}
