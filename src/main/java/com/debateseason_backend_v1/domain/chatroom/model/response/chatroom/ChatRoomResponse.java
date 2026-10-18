package com.debateseason_backend_v1.domain.chatroom.model.response.chatroom;

import java.time.LocalDateTime;
import java.util.List;

import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.TeamScore;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ChatRoomResponse{

	@Schema(description = "채팅방 ID",example = "1")
	private long chatRoomId;

	@Schema(description = "토론방 제목",example = "동덕여대는 폭력시위이다.")
	private String title;

	@Schema(description = "토론방 내용",example = "#동덕여대 #폭력시원")
	private String content;

	// 상태 메시지는 UPPERCASE로 해서, 통일시킴
	@Schema(description = "찬성 수",example = "15")
	private int agree;

	@Schema(description = "반대 수",example = "10")
	private int disagree;

	private List<TeamScore> teams;

	//private HightlightResponse highlight;

	// CreateDate 반환
	@Schema(description = "생성 시각 (Asia/Seoul)",example = "2024-12-03T08:51:57")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createdAt;

	@Schema(description = "투표", example = "AGREE")
	private String opinion;

	// ── 상위 관계 (웹 /debate/{id} 가 스레드 id 하나로 이슈·컨테이너를 찾는다) ──

	@Schema(description = "소속 이슈 ID", example = "6")
	private Long issueId;

	@Schema(description = "소속 이슈명 (브레드크럼용)", example = "의대 정원 확대")
	private String issueTitle;

	@Schema(description = "방 종류. CONTAINER = 이슈당 1개인 채팅 버킷, THREAD = 토론 주제. 레거시(NULL) 행은 THREAD 로 내려간다",
		example = "THREAD", allowableValues = {"CONTAINER", "THREAD"})
	private String roomType;

	@Schema(description = "소속 컨테이너 방 ID. 채팅 조회 GET /api/v1/chat/rooms/{roomId}/messages 의 roomId. "
		+ "CONTAINER 자신·이관 전 레거시 방은 null", example = "155", nullable = true)
	private Long containerRoomId;

	@Schema(description = "방 상태. HIDDEN·DELETED 는 노출하지 않는다", example = "OPEN",
		allowableValues = {"OPEN", "CLOSED", "HIDDEN", "DELETED"})
	private String status;

	@Schema(description = "마지막 수정 시각 (Asia/Seoul). 수정 기록이 없는 행은 createdAt 과 같다", example = "2024-12-03T08:51:57")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime updatedAt;

	@Override
	public String toString() {
		return "id: "+chatRoomId+" title: "+title+" content: "+content+" createdAt: "+createdAt + " agree: "+agree +" disagree: "+disagree ;
	}

}
