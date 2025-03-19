package com.debateseason_backend_v1.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.debateseason_backend_v1.app.repository.AppVersionRepository;
import com.debateseason_backend_v1.app.repository.entity.AppVersion;
import com.debateseason_backend_v1.app.service.response.AppVersionCheckResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppVersionServiceV1 {

	private final AppVersionRepository appVersionRepository;

	public AppVersionCheckResponse updateCheck(Integer versionCode) {
		// 클라이언트보다 최신 버전들 조회
		List<AppVersion> newerVersions = appVersionRepository.findNewerVersions(versionCode);

		// 강제 업데이트가 필요한 버전이 하나라도 있는지 확인
		boolean isForceUpdate = newerVersions.stream().anyMatch(AppVersion::getForceUpdate);

		return AppVersionCheckResponse.of(isForceUpdate);
	}
}
