package com.debateseason_backend_v1.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.debateseason_backend_v1.app.repository.entity.AppVersion;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {

	// 파라미터 versionCode 보다 큰 버전들 조회
	@Query("SELECT a FROM AppVersion a WHERE a.versionCode > :versionCode ORDER BY a.versionCode DESC")
	List<AppVersion> findNewerVersions(@Param("versionCode") Integer versionCode);

}