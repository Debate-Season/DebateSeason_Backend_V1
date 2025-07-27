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
public enum DistrictType {

	// 무응답
	UNDEFINED("", "무응답", ProvinceType.UNDEFINED),

	// 서울특별시
	JONGNO("11010", "종로구", ProvinceType.SEOUL),
	JUNG_SEOUL("11020", "중구", ProvinceType.SEOUL),
	YONGSAN("11030", "용산구", ProvinceType.SEOUL),
	SEONGDONG("11040", "성동구", ProvinceType.SEOUL),
	GWANGJIN("11050", "광진구", ProvinceType.SEOUL),
	DONGDAEMUN("11060", "동대문구", ProvinceType.SEOUL),
	JUNGNANG("11070", "중랑구", ProvinceType.SEOUL),
	SEONGBUK("11080", "성북구", ProvinceType.SEOUL),
	GANGBUK("11090", "강북구", ProvinceType.SEOUL),
	DOBONG("11100", "도봉구", ProvinceType.SEOUL),
	NOWON("11110", "노원구", ProvinceType.SEOUL),
	EUNPYEONG("11120", "은평구", ProvinceType.SEOUL),
	SEODAEMUN("11130", "서대문구", ProvinceType.SEOUL),
	MAPO("11140", "마포구", ProvinceType.SEOUL),
	YANGCHEON("11150", "양천구", ProvinceType.SEOUL),
	GANGSEO_SEOUL("11160", "강서구", ProvinceType.SEOUL),
	GURO("11170", "구로구", ProvinceType.SEOUL),
	GEUMCHEON("11180", "금천구", ProvinceType.SEOUL),
	YEONGDEUNGPO("11190", "영등포구", ProvinceType.SEOUL),
	DONGJAK("11200", "동작구", ProvinceType.SEOUL),
	GWANAK("11210", "관악구", ProvinceType.SEOUL),
	SEOCHO("11220", "서초구", ProvinceType.SEOUL),
	GANGNAM("11230", "강남구", ProvinceType.SEOUL),
	SONGPA("11240", "송파구", ProvinceType.SEOUL),
	GANGDONG("11250", "강동구", ProvinceType.SEOUL),

	// 부산광역시
	JUNG_BUSAN("21010", "중구", ProvinceType.BUSAN),
	SEO_BUSAN("21020", "서구", ProvinceType.BUSAN),
	DONG_BUSAN("21030", "동구", ProvinceType.BUSAN),
	YEONGDO("21040", "영도구", ProvinceType.BUSAN),
	BUSANJIN("21050", "부산진구", ProvinceType.BUSAN),
	DONGNAE("21060", "동래구", ProvinceType.BUSAN),
	NAM_BUSAN("21070", "남구", ProvinceType.BUSAN),
	BUK_BUSAN("21080", "북구", ProvinceType.BUSAN),
	HAEUNDAE("21090", "해운대구", ProvinceType.BUSAN),
	SAHA("21100", "사하구", ProvinceType.BUSAN),
	GEUMJEONG("21110", "금정구", ProvinceType.BUSAN),
	GANGSEO_BUSAN("21120", "강서구", ProvinceType.BUSAN),
	YEONJE("21130", "연제구", ProvinceType.BUSAN),
	SUYEONG("21140", "수영구", ProvinceType.BUSAN),
	SASANG("21150", "사상구", ProvinceType.BUSAN),
	GIJANG("21310", "기장군", ProvinceType.BUSAN),

	// 대구광역시
	JUNG_DAEGU("22010", "중구", ProvinceType.DAEGU),
	DONG_DAEGU("22020", "동구", ProvinceType.DAEGU),
	SEO_DAEGU("22030", "서구", ProvinceType.DAEGU),
	NAM_DAEGU("22040", "남구", ProvinceType.DAEGU),
	BUK_DAEGU("22050", "북구", ProvinceType.DAEGU),
	SUSEONG("22060", "수성구", ProvinceType.DAEGU),
	DALSEO("22070", "달서구", ProvinceType.DAEGU),
	DALSEONG("22310", "달성군", ProvinceType.DAEGU),
	GUNWI("22330", "군위군", ProvinceType.DAEGU),

	// 인천광역시
	JUNG_INCHEON("23010", "중구", ProvinceType.INCHEON),
	DONG_INCHEON("23020", "동구", ProvinceType.INCHEON),
	MICHUHOL("23040", "미추홀구", ProvinceType.INCHEON),
	YEONSU("23050", "연수구", ProvinceType.INCHEON),
	NAMDONG("23060", "남동구", ProvinceType.INCHEON),
	BUPYEONG("23070", "부평구", ProvinceType.INCHEON),
	GYEYANG("23080", "계양구", ProvinceType.INCHEON),
	SEO_INCHEON("23090", "서구", ProvinceType.INCHEON),
	GANGHWA("23310", "강화군", ProvinceType.INCHEON),
	ONGJIN("23320", "옹진군", ProvinceType.INCHEON),

	// 광주광역시
	DONG_GWANGJU("24010", "동구", ProvinceType.GWANGJU),
	SEO_GWANGJU("24020", "서구", ProvinceType.GWANGJU),
	NAM_GWANGJU("24030", "남구", ProvinceType.GWANGJU),
	BUK_GWANGJU("24040", "북구", ProvinceType.GWANGJU),
	GWANGSAN("24050", "광산구", ProvinceType.GWANGJU),

	// 대전광역시
	DONG_DAEJEON("25010", "동구", ProvinceType.DAEJEON),
	JUNG_DAEJEON("25020", "중구", ProvinceType.DAEJEON),
	SEO_DAEJEON("25030", "서구", ProvinceType.DAEJEON),
	YUSEONG("25040", "유성구", ProvinceType.DAEJEON),
	DAEDEOK("25050", "대덕구", ProvinceType.DAEJEON),

	// 울산광역시
	JUNG_ULSAN("26010", "중구", ProvinceType.ULSAN),
	NAM_ULSAN("26020", "남구", ProvinceType.ULSAN),
	DONG_ULSAN("26030", "동구", ProvinceType.ULSAN),
	BUK_ULSAN("26040", "북구", ProvinceType.ULSAN),
	ULJU("26310", "울주군", ProvinceType.ULSAN),

	// 세종특별자치시
	SEJONG_JOCHIWON("29011", "조치원읍", ProvinceType.SEJONG),
	SEJONG_YEONGI("29021", "연기면", ProvinceType.SEJONG),
	SEJONG_YEONDONG("29022", "연동면", ProvinceType.SEJONG),
	SEJONG_BUGANG("29023", "부강면", ProvinceType.SEJONG),
	SEJONG_GEUMNAM("29024", "금남면", ProvinceType.SEJONG),
	SEJONG_JANGUN("29025", "장군면", ProvinceType.SEJONG),
	SEJONG_YEONSEO("29026", "연서면", ProvinceType.SEJONG),
	SEJONG_JEONUI("29027", "전의면", ProvinceType.SEJONG),
	SEJONG_JEONDONG("29028", "전동면", ProvinceType.SEJONG),
	SEJONG_SOJEONG("29029", "소정면", ProvinceType.SEJONG),
	SEJONG_HANSOL("29031", "한솔동", ProvinceType.SEJONG),
	SEJONG_SAEROM("29032", "새롬동", ProvinceType.SEJONG),
	SEJONG_NASEONG("29033", "나성동", ProvinceType.SEJONG),
	SEJONG_DAJEONG("29034", "다정동", ProvinceType.SEJONG),
	SEJONG_DODAM("29035", "도담동", ProvinceType.SEJONG),
	SEJONG_EOJIN("29036", "어진동", ProvinceType.SEJONG),
	SEJONG_HAEMIL("29037", "해밀동", ProvinceType.SEJONG),
	SEJONG_AREUM("29038", "아름동", ProvinceType.SEJONG),
	SEJONG_JONGCHON("29039", "종촌동", ProvinceType.SEJONG),
	SEJONG_GOUN("29040", "고운동", ProvinceType.SEJONG),
	SEJONG_BORAM("29041", "보람동", ProvinceType.SEJONG),
	SEJONG_DAEPYEONG("29042", "대평동", ProvinceType.SEJONG),
	SEJONG_SODAM("29043", "소담동", ProvinceType.SEJONG),
	SEJONG_BANGOK("29044", "반곡동", ProvinceType.SEJONG),

	// 경기도
	SUWON_JANGAN("31010", "수원시 장안구", ProvinceType.GYEONGGI),
	SUWON_GWONSEON("31020", "수원시 권선구", ProvinceType.GYEONGGI),
	SUWON_PALDAL("31030", "수원시 팔달구", ProvinceType.GYEONGGI),
	SUWON_YEONGTONG("31040", "수원시 영통구", ProvinceType.GYEONGGI),
	SEONGNAM_SUJEONG("31050", "성남시 수정구", ProvinceType.GYEONGGI),
	SEONGNAM_JUNGWON("31060", "성남시 중원구", ProvinceType.GYEONGGI),
	SEONGNAM_BUNDANG("31070", "성남시 분당구", ProvinceType.GYEONGGI),
	UIJEONGBU("31080", "의정부시", ProvinceType.GYEONGGI),
	ANYANG_MANAN("31090", "안양시 만안구", ProvinceType.GYEONGGI),
	ANYANG_DONGAN("31100", "안양시 동안구", ProvinceType.GYEONGGI),
	BUCHEON("31110", "부천시", ProvinceType.GYEONGGI),
	GWANGMYEONG("31120", "광명시", ProvinceType.GYEONGGI),
	PYEONGTAEK("31130", "평택시", ProvinceType.GYEONGGI),
	DONGDUCHEON("31140", "동두천시", ProvinceType.GYEONGGI),
	ANSAN_SANGROK("31150", "안산시 상록구", ProvinceType.GYEONGGI),
	ANSAN_DANWON("31160", "안산시 단원구", ProvinceType.GYEONGGI),
	GOYANG_DEOGYANG("31170", "고양시 덕양구", ProvinceType.GYEONGGI),
	GOYANG_ILSANDONG("31180", "고양시 일산동구", ProvinceType.GYEONGGI),
	GOYANG_ILSANSEO("31190", "고양시 일산서구", ProvinceType.GYEONGGI),
	GWACHEON("31200", "과천시", ProvinceType.GYEONGGI),
	GURI("31210", "구리시", ProvinceType.GYEONGGI),
	NAMYANGJU("31220", "남양주시", ProvinceType.GYEONGGI),
	OSAN("31230", "오산시", ProvinceType.GYEONGGI),
	SIHEUNG("31240", "시흥시", ProvinceType.GYEONGGI),
	GUNPO("31250", "군포시", ProvinceType.GYEONGGI),
	UIWANG("31260", "의왕시", ProvinceType.GYEONGGI),
	HANAM("31270", "하남시", ProvinceType.GYEONGGI),
	YONGIN_CHEOIN("31280", "용인시 처인구", ProvinceType.GYEONGGI),
	YONGIN_GIHEUNG("31290", "용인시 기흥구", ProvinceType.GYEONGGI),
	YONGIN_SUJI("31300", "용인시 수지구", ProvinceType.GYEONGGI),
	PAJU("31310", "파주시", ProvinceType.GYEONGGI),
	ICHEON("31320", "이천시", ProvinceType.GYEONGGI),
	ANSEONG("31330", "안성시", ProvinceType.GYEONGGI),
	GIMPO("31340", "김포시", ProvinceType.GYEONGGI),
	HWASEONG("31350", "화성시", ProvinceType.GYEONGGI),
	GWANGJU_GYEONGGI("31360", "광주시", ProvinceType.GYEONGGI),
	YANGJU("31370", "양주시", ProvinceType.GYEONGGI),
	POCHEON("31380", "포천시", ProvinceType.GYEONGGI),
	YEOJU("31390", "여주시", ProvinceType.GYEONGGI),
	YEONCHEON("31410", "연천군", ProvinceType.GYEONGGI),
	GAPYEONG("31420", "가평군", ProvinceType.GYEONGGI),
	YANGPYEONG("31430", "양평군", ProvinceType.GYEONGGI),

	// 강원특별자치도
	CHUNCHEON("32010", "춘천시", ProvinceType.GANGWON),
	WONJU("32020", "원주시", ProvinceType.GANGWON),
	GANGNEUNG("32030", "강릉시", ProvinceType.GANGWON),
	DONGHAE("32040", "동해시", ProvinceType.GANGWON),
	TAEBAEK("32050", "태백시", ProvinceType.GANGWON),
	SOKCHO("32060", "속초시", ProvinceType.GANGWON),
	SAMCHEOK("32070", "삼척시", ProvinceType.GANGWON),
	HONGCHEON("32310", "홍천군", ProvinceType.GANGWON),
	HOENGSEONG("32320", "횡성군", ProvinceType.GANGWON),
	YEONGWOL("32330", "영월군", ProvinceType.GANGWON),
	PYEONGCHANG("32340", "평창군", ProvinceType.GANGWON),
	JEONGSEON("32350", "정선군", ProvinceType.GANGWON),
	CHEORWON("32360", "철원군", ProvinceType.GANGWON),
	HWACHEON("32370", "화천군", ProvinceType.GANGWON),
	YANGGU("32380", "양구군", ProvinceType.GANGWON),
	INJE("32390", "인제군", ProvinceType.GANGWON),
	GOSEONG_GANGWON("32400", "고성군", ProvinceType.GANGWON),
	YANGYANG("32410", "양양군", ProvinceType.GANGWON),

	// 충청북도
	CHEONGJU_SANGDANG("33010", "청주시 상당구", ProvinceType.CHUNGBUK),
	CHEONGJU_SEOWON("33020", "청주시 서원구", ProvinceType.CHUNGBUK),
	CHEONGJU_HEUNGDEOK("33030", "청주시 흥덕구", ProvinceType.CHUNGBUK),
	CHEONGJU_CHEONGWON("33040", "청주시 청원구", ProvinceType.CHUNGBUK),
	CHUNGJU("33050", "충주시", ProvinceType.CHUNGBUK),
	JECHEON("33060", "제천시", ProvinceType.CHUNGBUK),
	BOEUN("33310", "보은군", ProvinceType.CHUNGBUK),
	OKCHEON("33320", "옥천군", ProvinceType.CHUNGBUK),
	YEONGDONG("33330", "영동군", ProvinceType.CHUNGBUK),
	JINCHEON("33340", "진천군", ProvinceType.CHUNGBUK),
	GOESAN("33350", "괴산군", ProvinceType.CHUNGBUK),
	EUMSEONG("33360", "음성군", ProvinceType.CHUNGBUK),
	DANYANG("33370", "단양군", ProvinceType.CHUNGBUK),
	JEUNGPYEONG("33380", "증평군", ProvinceType.CHUNGBUK),

	// 충청남도
	CHEONAN_DONGNAM("34010", "천안시 동남구", ProvinceType.CHUNGNAM),
	CHEONAN_SEOBUK("34020", "천안시 서북구", ProvinceType.CHUNGNAM),
	GONGJU("34030", "공주시", ProvinceType.CHUNGNAM),
	BORYEONG("34040", "보령시", ProvinceType.CHUNGNAM),
	ASAN("34050", "아산시", ProvinceType.CHUNGNAM),
	SEOSAN("34060", "서산시", ProvinceType.CHUNGNAM),
	NONSAN("34070", "논산시", ProvinceType.CHUNGNAM),
	GYERYONG("34080", "계룡시", ProvinceType.CHUNGNAM),
	DANGJIN("34090", "당진시", ProvinceType.CHUNGNAM),
	GEUMSAN("34310", "금산군", ProvinceType.CHUNGNAM),
	BUYEO("34330", "부여군", ProvinceType.CHUNGNAM),
	SEOCHEON("34340", "서천군", ProvinceType.CHUNGNAM),
	CHEONGYANG("34350", "청양군", ProvinceType.CHUNGNAM),
	HONGSEONG("34360", "홍성군", ProvinceType.CHUNGNAM),
	YESAN("34370", "예산군", ProvinceType.CHUNGNAM),
	TAEAN("34380", "태안군", ProvinceType.CHUNGNAM),

	// 전북특별자치도
	JEONJU_WANSAN("35010", "전주시 완산구", ProvinceType.JEONBUK),
	JEONJU_DEOKJIN("35020", "전주시 덕진구", ProvinceType.JEONBUK),
	GUNSAN("35030", "군산시", ProvinceType.JEONBUK),
	IKSAN("35040", "익산시", ProvinceType.JEONBUK),
	JEONGEUP("35050", "정읍시", ProvinceType.JEONBUK),
	NAMWON("35060", "남원시", ProvinceType.JEONBUK),
	GIMJE("35070", "김제시", ProvinceType.JEONBUK),
	WANJU("35310", "완주군", ProvinceType.JEONBUK),
	JINAN("35320", "진안군", ProvinceType.JEONBUK),
	MUJU("35330", "무주군", ProvinceType.JEONBUK),
	JANGSU("35340", "장수군", ProvinceType.JEONBUK),
	IMSIL("35350", "임실군", ProvinceType.JEONBUK),
	SUNCHANG("35360", "순창군", ProvinceType.JEONBUK),
	GOCHANG("35370", "고창군", ProvinceType.JEONBUK),
	BUAN("35380", "부안군", ProvinceType.JEONBUK),

	// 전라남도
	MOKPO("36010", "목포시", ProvinceType.JEONNAM),
	YEOSU("36020", "여수시", ProvinceType.JEONNAM),
	SUNCHEON("36030", "순천시", ProvinceType.JEONNAM),
	NAJU("36040", "나주시", ProvinceType.JEONNAM),
	GWANGYANG("36060", "광양시", ProvinceType.JEONNAM),
	DAMYANG("36310", "담양군", ProvinceType.JEONNAM),
	GOKSEONG("36320", "곡성군", ProvinceType.JEONNAM),
	GURYE("36330", "구례군", ProvinceType.JEONNAM),
	GOHEUNG("36350", "고흥군", ProvinceType.JEONNAM),
	BOSEONG("36360", "보성군", ProvinceType.JEONNAM),
	HWASUN("36370", "화순군", ProvinceType.JEONNAM),
	JANGHEUNG("36380", "장흥군", ProvinceType.JEONNAM),
	GANGJIN("36390", "강진군", ProvinceType.JEONNAM),
	HAENAM("36400", "해남군", ProvinceType.JEONNAM),
	YEONGAM("36410", "영암군", ProvinceType.JEONNAM),
	MUAN("36420", "무안군", ProvinceType.JEONNAM),
	HAMPYEONG("36430", "함평군", ProvinceType.JEONNAM),
	YEONGGWANG("36440", "영광군", ProvinceType.JEONNAM),
	JANGSEONG("36450", "장성군", ProvinceType.JEONNAM),
	WANDO("36460", "완도군", ProvinceType.JEONNAM),
	JINDO("36470", "진도군", ProvinceType.JEONNAM),
	SINAN("36480", "신안군", ProvinceType.JEONNAM),

	// 경상북도
	POHANG_NAM("37010", "포항시 남구", ProvinceType.GYEONGBUK),
	POHANG_BUK("37020", "포항시 북구", ProvinceType.GYEONGBUK),
	GYEONGJU("37030", "경주시", ProvinceType.GYEONGBUK),
	GIMCHEON("37040", "김천시", ProvinceType.GYEONGBUK),
	ANDONG("37050", "안동시", ProvinceType.GYEONGBUK),
	GUMI("37060", "구미시", ProvinceType.GYEONGBUK),
	YEONGJU("37070", "영주시", ProvinceType.GYEONGBUK),
	YEONGCHEON("37080", "영천시", ProvinceType.GYEONGBUK),
	SANGJU("37090", "상주시", ProvinceType.GYEONGBUK),
	MUNGYEONG("37100", "문경시", ProvinceType.GYEONGBUK),
	GYEONGSAN("37110", "경산시", ProvinceType.GYEONGBUK),
	UISEONG("37310", "의성군", ProvinceType.GYEONGBUK),
	CHEONGSONG("37320", "청송군", ProvinceType.GYEONGBUK),
	YEONGYANG("37330", "영양군", ProvinceType.GYEONGBUK),
	YEONGDEOK("37340", "영덕군", ProvinceType.GYEONGBUK),
	CHEONGDO("37350", "청도군", ProvinceType.GYEONGBUK),
	GORYEONG("37360", "고령군", ProvinceType.GYEONGBUK),
	SEONGJU("37370", "성주군", ProvinceType.GYEONGBUK),
	CHILGOK("37380", "칠곡군", ProvinceType.GYEONGBUK),
	YECHEON("37390", "예천군", ProvinceType.GYEONGBUK),
	BONGHWA("37400", "봉화군", ProvinceType.GYEONGBUK),
	ULJIN("37410", "울진군", ProvinceType.GYEONGBUK),
	ULLEUNG("37420", "울릉군", ProvinceType.GYEONGBUK),

	// 경상남도
	CHANGWON_UICHANG("38010", "창원시 의창구", ProvinceType.GYEONGNAM),
	CHANGWON_SEONGSAN("38020", "창원시 성산구", ProvinceType.GYEONGNAM),
	CHANGWON_MASANHOIPO("38030", "창원시 마산합포구", ProvinceType.GYEONGNAM),
	CHANGWON_MASANHEWON("38040", "창원시 마산회원구", ProvinceType.GYEONGNAM),
	CHANGWON_JINHAE("38050", "창원시 진해구", ProvinceType.GYEONGNAM),
	JINJU("38060", "진주시", ProvinceType.GYEONGNAM),
	TONGYEONG("38070", "통영시", ProvinceType.GYEONGNAM),
	SACHEON("38080", "사천시", ProvinceType.GYEONGNAM),
	GIMHAE("38090", "김해시", ProvinceType.GYEONGNAM),
	MIRYANG("38100", "밀양시", ProvinceType.GYEONGNAM),
	GEOJE("38110", "거제시", ProvinceType.GYEONGNAM),
	YANGSAN("38120", "양산시", ProvinceType.GYEONGNAM),
	UIRYEONG("38310", "의령군", ProvinceType.GYEONGNAM),
	HAMAN("38320", "함안군", ProvinceType.GYEONGNAM),
	CHANGNYEONG("38330", "창녕군", ProvinceType.GYEONGNAM),
	GOSEONG_GYEONGNAM("38340", "고성군", ProvinceType.GYEONGNAM),
	NAMHAE("38350", "남해군", ProvinceType.GYEONGNAM),
	HADONG("38360", "하동군", ProvinceType.GYEONGNAM),
	SANCHEONG("38370", "산청군", ProvinceType.GYEONGNAM),
	HAMYANG("38380", "함양군", ProvinceType.GYEONGNAM),
	GEOCHANG("38390", "거창군", ProvinceType.GYEONGNAM),
	HAPCHEON("38400", "합천군", ProvinceType.GYEONGNAM),

	// 제주특별자치도
	JEJU_SI("39010", "제주시", ProvinceType.JEJU),
	SEOGWIPO("39020", "서귀포시", ProvinceType.JEJU);

	private final String code;
	private final String name;
	private final ProvinceType provinceType;

	// 코드 인덱싱을 위한 정적 맵
	private static final Map<String, DistrictType> BY_CODE = new HashMap<>();

	static {
		for (DistrictType districtType : values()) {
			BY_CODE.put(districtType.code, districtType);
		}
	}

	public static DistrictType fromCode(String code) {
		DistrictType districtType = BY_CODE.get(code);
		if (districtType == null) {
			throw new CustomException(ErrorCode.NOT_SUPPORTED_DISTRICT);
		}
		return districtType;
	}

	@JsonCreator
	public static DistrictType from(String code) {
		return fromCode(code);
	}
}