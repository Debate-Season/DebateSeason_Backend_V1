package com.debateseason_backend_v1.domain.youtubeLive.scheduler.news.template;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NewsTemplate {
	public void checkCurrentKeyState(String key,String category){

		// 현재 키 값을 확인하는 로직.
		if(key.equals("dummy")){
			log.error(category+"News : 현재 활성화 된 유트브 API 키 = "+key+". 현재 실행 중인 서버는 Local 또는 Dev일 수 있음.");
		}
		else{
			log.error("유튜브 API 할당량 모두 소진 -> Sbs.NewsLive");
		}

	}
}
