package com.debateseason_backend_v1.domain.profile.enums;

import java.util.HashMap;
import java.util.Map;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommunityType {

	DC_INSIDE(1L, "디시인사이드", "community/icons/dcinside.png"),
	FM_KOREA(2L, "에펨코리아", "community/icons/fmkorea.png"),
	THE_QOO(3L, "더쿠", "community/icons/theqoo.png"),
	PPOMPPU(4L, "뽐뿌", "community/icons/ppomppu.png"),
	RULIWEB(5L, "루리웹", "community/icons/ruliweb.png"),
	MLBPARK(6L, "엠팍", "community/icons/mlbpark.png"),
	INVEN(7L, "인벤", "community/icons/inven.png"),
	NATEPANN(8L, "네이트판", "community/icons/natepann.png"),
	ARCALIVE(9L, "아카라이브", "community/icons/arcalive.png"),
	CLIEN(10L, "클리앙", "community/icons/clien.png"),
	ILBE(11L, "일간베스트", "community/icons/ilbe.png"),
	INSTIZ(12L, "인스티즈", "community/icons/instiz.png"),
	BOBAEDREAM(13L, "보배드림", "community/icons/bobaedream.png"),
	HUMORUNIV(14L, "웃긴대학", "community/icons/humoruniv.png"),
	ORBI(15L, "오르비", "community/icons/orbi.png"),
	TODAYHUMOR(16L, "오늘의유머", "community/icons/todayhumor.png"),
	WOMENSGENERATION(17L, "여성시대", "community/icons/womensgeneration.png"),
	EVERYTIME(18L, "에브리타임", "community/icons/everytime.png"),
	BLIND(19L, "블라인드", "community/icons/blind.png"),
	REDDIT(20L, "Reddit", "community/icons/reddit.png"),
	X(21L, "X", "community/icons/x.png"),
	THREADS(22L, "Threads", "community/icons/threads.png"),
	INDEPENDENT(23L, "무소속", "community/icons/independent.png");

	private final Long id;
	private final String name;
	private final String iconUrl;

	private static final Map<Long, CommunityType> ID_MAP = new HashMap<>();
	private static final Map<String, CommunityType> NAME_MAP = new HashMap<>();

	static {
		for (CommunityType type : values()) {
			ID_MAP.put(type.getId(), type);
			NAME_MAP.put(type.getName(), type);
		}
	}

	public static CommunityType findById(Long id) {

		CommunityType type = ID_MAP.get(id);
		if (type == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_COMMUNITY);
		}
		return type;
	}

	public static CommunityType findByName(String name) {

		CommunityType type = NAME_MAP.get(name);
		if (type == null) {
			throw new CustomException(ErrorCode.NOT_FOUND_COMMUNITY);
		}
		return type;
	}

	public static boolean isValidId(Long id) {
		
		return ID_MAP.containsKey(id);
	}

}