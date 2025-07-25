package com.debateseason_backend_v1.domain.youtubeLive.application.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chatroom.domain.TimeProcessor;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomMapper;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeMapper;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.entity.YoutubeLiveEntity;
import com.debateseason_backend_v1.domain.youtubeLive.application.repository.YoutubeLiveRepository;
import com.debateseason_backend_v1.domain.youtubeLive.model.response.YoutubeLiveDetail;
import com.debateseason_backend_v1.domain.youtubeLive.model.response.YoutubeLiveResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class YoutubeLiveService {

	private final ChatRoomRepository chatRoomRepository;
	private final YoutubeLiveRepository youtubeLiveRepository;

	// 시간 처리해주는 객체
	private final TimeProcessor timeProcessor;


	// youtubeLive 모두 보기
	public ApiResult<YoutubeLiveResponse> getAllYoutubeLives() {

		List<YoutubeLiveEntity> fetchedAllYoutubeLives = youtubeLiveRepository.findAll();

		YoutubeMapper youtubeMapper = new YoutubeMapper();
		List<YoutubeLiveDto> youtubeliveList = youtubeMapper.toDomain(fetchedAllYoutubeLives);

		YoutubeLiveResponse youtubeLives = new YoutubeLiveResponse();
		youtubeLives.setYoutubeLives(youtubeliveList);
		return ApiResult.<YoutubeLiveResponse>builder()
			.status(200)
			.data(youtubeLives)
			.code(ErrorCode.SUCCESS)
			.message("Youtube 라이브 모두 보기를 요청했습니다.")
			.build()
			;
	}

	// YoutubeLive 1개 가져오기
	public ApiResult<YoutubeLiveDetail> getYoutubeLiveDetail(int id) {

		// 1. id 값으로 YoutubeLive 상세보기
		YoutubeLiveEntity youtubeLiveEntity = youtubeLiveRepository.findById(id);


		YoutubeLiveDto youtubeLiveDto = YoutubeLiveDto.builder()
			.id(youtubeLiveEntity.getId())
			.title(youtubeLiveEntity.getTitle())
			.supplier(youtubeLiveEntity.getSupplier())
			.videoId(youtubeLiveEntity.getVideoId())
			.category(youtubeLiveEntity.getCategory())
			.createAt(youtubeLiveEntity.getCreatedAt())
			.src(youtubeLiveEntity.getScr())
			.build()
			;

		// 2. 토론방 인기 5개 가져오기
		// chat_room_id(0), title(1), created_at(2), AGREE(3), DISAGREE(4)
		// 1. 활성화된 최상위 5개 토론방을 보여준다.
		List<ChatRoomMapper> top5BestChatRooms = chatRoomRepository.findTop5ChatRoomsOrderedByChatCounts().stream().map(
			e->{
				Long chatRoomId = (Long)e[0];
				String chatRoomTitle = (String)e[1];

				String rawCreatedAt = e[2].toString();
				String result = rawCreatedAt.split("\\.")[0];
				LocalDateTime createdAt = LocalDateTime.parse(result, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				Long agree = (Long)e[3];
				Long disagree = (Long)e[4];

				String outDated = timeProcessor.findLastestChatTime(chatRoomId);

				return
					ChatRoomMapper.builder()
						.chatRoomId(chatRoomId)
						.chatRoomTitle(chatRoomTitle)
						.createdAt(createdAt)
						.agree(agree)
						.disagree(disagree)
						.outDated(outDated)
						.build()
					;

			}
		).toList();

		YoutubeLiveDetail youtubeLiveDetail = new YoutubeLiveDetail(youtubeLiveDto,top5BestChatRooms);

		return ApiResult.<YoutubeLiveDetail>builder()
			.status(200)
			.data(youtubeLiveDetail)
			.code(ErrorCode.SUCCESS)
			.message("Youtube 라이브 상세보기를 요청했습니다.")
			.build()
			;
	}

}
