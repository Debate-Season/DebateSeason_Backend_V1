package com.debateseason_backend_v1.security.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("서비스 API 키 설정")
class ServiceApiKeysTest {

	private static final String KEY_A = "a".repeat(32);
	private static final String KEY_B = "b".repeat(40);

	@Test
	@DisplayName("맞는 키는 서비스 이름으로 풀린다")
	void resolvesName() {
		ServiceApiKeys keys = new ServiceApiKeys("ontology:" + KEY_A);

		assertThat(keys.resolve(KEY_A)).contains("ontology");
		assertThat(keys.resolve(KEY_B)).isEmpty();
	}

	@Test
	@DisplayName("교체 중에는 같은 이름의 옛 키와 새 키가 둘 다 통한다")
	void rotation() {
		ServiceApiKeys keys = new ServiceApiKeys("ontology:" + KEY_A + " , ontology:" + KEY_B);

		assertThat(keys.resolve(KEY_A)).contains("ontology");
		assertThat(keys.resolve(KEY_B)).contains("ontology");
	}

	@Test
	@DisplayName("설정이 비어 있으면 어떤 키도 통하지 않는다")
	void emptyDisables() {
		assertThat(new ServiceApiKeys("").resolve(KEY_A)).isEmpty();
		assertThat(new ServiceApiKeys(null).resolve(KEY_A)).isEmpty();
	}

	@Test
	@DisplayName("빈 헤더 값은 통하지 않는다")
	void blankPresented() {
		ServiceApiKeys keys = new ServiceApiKeys("ontology:" + KEY_A);

		assertThat(keys.resolve("")).isEmpty();
		assertThat(keys.resolve(null)).isEmpty();
	}

	@Test
	@DisplayName("형식이 틀린 항목은 버리고 나머지는 살린다 — 앱이 안 뜨는 것보다 낫다")
	void malformedEntriesIgnored() {
		ServiceApiKeys keys = new ServiceApiKeys(
			"ontology:short,"            // 32자 미만
				+ KEY_B + ","            // 이름 없음
				+ "Bad Name:" + KEY_B + "," // 이름 규칙 위반
				+ "crawler:" + KEY_A);

		assertThat(keys.resolve("short")).isEmpty();
		assertThat(keys.resolve(KEY_B)).isEmpty();
		assertThat(keys.resolve(KEY_A)).contains("crawler");
	}

	@Test
	@DisplayName("키 안에 콜론이 있어도 첫 콜론만 구분자로 본다")
	void colonInsideSecret() {
		String secret = "x:" + "y".repeat(31);
		ServiceApiKeys keys = new ServiceApiKeys("ontology:" + secret);

		assertThat(keys.resolve(secret)).contains("ontology");
	}
}
