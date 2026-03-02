package com.debateseason_backend_v1.domain.issue.community;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CommunityList {
	private final Map<String,String> communityMap = new HashMap<>();

	public CommunityList() { // 빈이 생성될 때, communityMap도 같이 생성되도록 하자.
		communityMap.put("디시인사이드","community/icons/dcinside.png");
		communityMap.put("에펨코리아","community/icons/fmkorea.png");
		communityMap.put("더쿠","community/icons/theqoo.png");
		communityMap.put("뽐뿌","community/icons/ppomppu.png");
		communityMap.put("루리웹","community/icons/ruliweb.png");
		communityMap.put("엠팍","community/icons/mlbpark.png");
		communityMap.put("인벤","community/icons/inven.png");
		communityMap.put("네이트판","community/icons/natepann.png");
		communityMap.put("아카라이브","community/icons/arcalive.png");
		communityMap.put("클리앙","community/icons/clien.png");
		communityMap.put("일간베스트","community/icons/ilbe.png");
		communityMap.put("인스티즈","community/icons/instiz.png");
		communityMap.put("보배드림","community/icons/bobaedream.png");
		communityMap.put("웃긴대학","community/icons/humoruniv.png");
		communityMap.put("오르비","community/icons/orbi.png");
		communityMap.put("오늘의유머","community/icons/todayhumor.png");
		communityMap.put("여성시대","community/icons/womensgeneration.png");
		communityMap.put("에브리타임","community/icons/everytime.png");
		communityMap.put("블라인드","community/icons/blind.png");
		communityMap.put("Reddit","community/icons/reddit.png");
		communityMap.put("X","community/icons/x.png");
		communityMap.put("Threads","community/icons/threads.png");
		communityMap.put("무소속","community/icons/independent.png");
	}

	public String get(String name){
		return communityMap.get(name);
	}

	public Map<String,String> getCommunityMap(){
		return communityMap;
	}
}
