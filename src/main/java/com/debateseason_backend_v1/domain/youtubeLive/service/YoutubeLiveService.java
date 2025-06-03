package com.debateseason_backend_v1.domain.youtubeLive.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.debateseason_backend_v1.common.response.ApiResult;
import com.debateseason_backend_v1.domain.chat.application.repository.ChatRepository;
import com.debateseason_backend_v1.domain.chatroom.domain.ChatRoomMapper;
import com.debateseason_backend_v1.domain.chatroom.model.response.chatroom.messages.Top5BestChatRoom;
import com.debateseason_backend_v1.domain.repository.ChatRoomRepository;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLive;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeLiveDto;
import com.debateseason_backend_v1.domain.youtubeLive.domain.YoutubeMapper;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.YoutubeLiveEntity;
import com.debateseason_backend_v1.domain.youtubeLive.infrastructure.YoutubeLiveRepository;
import com.debateseason_backend_v1.domain.youtubeLive.model.response.YoutubeLiveDetail;
import com.debateseason_backend_v1.domain.youtubeLive.model.response.YoutubeLiveResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class YoutubeLiveService {

	private final ChatRepository chatRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final YoutubeLiveRepository youtubeLiveRepository;


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

				String outDated = findLastestChatTime(chatRoomId);

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


	private String findLastestChatTime(Long chatRoomId){
		Optional<LocalDateTime> latestChat = chatRepository.findMostRecentMessageTimestampByChatRoomId(chatRoomId);

		String time = null;

		if(latestChat.isPresent()){
			// 몇 분이 지났는지.
			Duration outdated = Duration.between(latestChat.get(), LocalDateTime.now());

			int realTime = 0; // 대화가 아무것도 없는 상태는 항상 null이다.
			realTime = (int)outdated.toMinutes();

			if(realTime == 0){
				time = "방금 전 대화";
			}
			else if(realTime >0 && realTime<60){ // mm만 표기
				time = outdated.toMinutes() + "분 전 대화"; // 분
			}
			else if(realTime >=60 && realTime <1440){ // hh:mm
				int hour = realTime/60;
				int minute = realTime%60;

				time = hour+"시간 "+minute+"분 전 대화";
			}
			else{ // day로 표기
				int day = realTime/1440;

				time = day+"일 전 대화";
			}

		}
		return time;
	}
}
