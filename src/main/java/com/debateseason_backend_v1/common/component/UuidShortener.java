package com.debateseason_backend_v1.common.component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UuidShortener {

	public String shortenUuid() {

		String uuid = UUID.randomUUID().toString();

		// 1. UUID를 UTF-8 바이트 배열로 변환
		byte[] uuidBytes = uuid.getBytes(StandardCharsets.UTF_8);

		try {
			// 2. SHA-256 해시 적용
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashedBytes = digest.digest(uuidBytes);

			// 3. 해시된 바이트 배열의 앞 8바이트를 16진수 문자열로 변환
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < 4; i++) {
				result.append(String.format("%02x", hashedBytes[i]));
			}

			// 4. 8자리 문자열 반환
			return result.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("UUID 변환 중 오류 발생", e);
		}
	}
}