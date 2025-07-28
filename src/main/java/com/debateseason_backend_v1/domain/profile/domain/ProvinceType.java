package com.debateseason_backend_v1.domain.profile.domain;

import java.util.HashMap;
import java.util.Map;

import com.debateseason_backend_v1.common.exception.CustomException;
import com.debateseason_backend_v1.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProvinceType {

	UNDEFINED("", "무응답"),
	SEOUL("11", "서울특별시"),
	BUSAN("21", "부산광역시"),
	DAEGU("22", "대구광역시"),
	INCHEON("23", "인천광역시"),
	GWANGJU("24", "광주광역시"),
	DAEJEON("25", "대전광역시"),
	ULSAN("26", "울산광역시"),
	SEJONG("29", "세종특별자치시"),
	GYEONGGI("31", "경기도"),
	GANGWON("32", "강원특별자치도"),
	CHUNGBUK("33", "충청북도"),
	CHUNGNAM("34", "충청남도"),
	JEONBUK("35", "전북특별자치도"),
	JEONNAM("36", "전라남도"),
	GYEONGBUK("37", "경상북도"),
	GYEONGNAM("38", "경상남도"),
	JEJU("39", "제주특별자치도");

	private final String code;
	private final String name;

	// 코드 인덱싱을 위한 정적 맵
	private static final Map<String, ProvinceType> BY_CODE = new HashMap<>();

	static {
		for (ProvinceType provinceType : values()) {
			BY_CODE.put(provinceType.code, provinceType);
		}
	}

	public static ProvinceType fromCode(String code) {
		ProvinceType provinceType = BY_CODE.get(code);
		if (provinceType == null) {
			throw new CustomException(ErrorCode.NOT_SUPPORTED_PROVINCE);
		}
		return provinceType;
	}

	@JsonCreator
	public static ProvinceType from(String code) {
		return fromCode(code);
	}

}