package com.debateseason_backend_v1.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 사람이 아닌 호출자(데이터 파이프라인 등)용 API 키 목록.
 *
 * <p>설정 형식: {@code SERVICE_API_KEYS=ontology:<키>,ontology:<새 키>}
 * <ul>
 *   <li>이름은 이력의 {@code decided_by} 에 {@code service:<이름>} 으로 남는다.</li>
 *   <li>같은 이름으로 키를 두 개 둘 수 있다 — 교체할 때 새 키를 추가하고, 파이프라인이 옮겨 가면 옛 키를 뺀다.</li>
 *   <li>비어 있으면 서비스 키 경로가 꺼진다. 모든 서비스 키 요청이 401 이 된다.</li>
 * </ul>
 *
 * <p>원문 키는 메모리에도 남기지 않고 SHA-256 다이제스트만 들고 있다.
 * 어떤 경우에도 키 값을 로그에 쓰지 않는다.
 */
@Slf4j
@Component
public class ServiceApiKeys {

	private static final Pattern NAME = Pattern.compile("[a-z0-9-]{1,32}");
	private static final int MIN_SECRET_LENGTH = 32;

	// record 로 두면 equals/hashCode 가 배열을 참조로 비교한다. 비교하지 않는 값 객체라 클래스로 둔다.
	private static final class Entry {
		private final String name;
		private final byte[] digest;

		private Entry(String name, byte[] digest) {
			this.name = name;
			this.digest = digest;
		}

		String name() {
			return name;
		}

		byte[] digest() {
			return digest;
		}
	}

	private final List<Entry> entries;

	public ServiceApiKeys(@Value("${service-api.keys:}") String raw) {
		this.entries = parse(raw);
		log.info("서비스 API 키 {}개 로드 (이름: {})", entries.size(), entries.stream().map(Entry::name).distinct().toList());
	}

	/** 키가 맞으면 서비스 이름을 돌려준다. */
	public Optional<String> resolve(String presented) {
		if (presented == null || presented.isBlank()) {
			return Optional.empty();
		}
		byte[] digest = sha256(presented.trim());

		// 일치하는 키를 찾아도 끝까지 비교한다. 몇 번째 키에서 맞았는지가 응답 시간으로 새지 않게 한다.
		String matched = null;
		for (Entry entry : entries) {
			if (MessageDigest.isEqual(entry.digest(), digest) && matched == null) {
				matched = entry.name();
			}
		}
		return Optional.ofNullable(matched);
	}

	private static List<Entry> parse(String raw) {
		List<Entry> result = new ArrayList<>();
		if (raw == null || raw.isBlank()) {
			return result;
		}
		for (String token : raw.split(",")) {
			String item = token.trim();
			if (item.isEmpty()) {
				continue;
			}
			int sep = item.indexOf(':');
			String name = sep > 0 ? item.substring(0, sep).trim() : "";
			String secret = sep > 0 ? item.substring(sep + 1).trim() : "";

			// 형식이 틀린 항목 하나 때문에 앱이 안 뜨면 운영 전체가 멈춘다. 그 항목만 버리고 경고한다.
			if (!NAME.matcher(name).matches() || secret.length() < MIN_SECRET_LENGTH) {
				log.warn("서비스 API 키 항목을 무시합니다 - 형식은 이름:키, 이름은 [a-z0-9-], 키는 {}자 이상", MIN_SECRET_LENGTH);
				continue;
			}
			result.add(new Entry(name, sha256(secret)));
		}
		return List.copyOf(result);
	}

	private static byte[] sha256(String value) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 을 사용할 수 없습니다.", e);
		}
	}
}
