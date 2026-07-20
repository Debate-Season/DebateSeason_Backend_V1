# ⚠️ 이 파일은 현재 배포에 사용되지 않는다 (ECS 시절 잔재).
#
# 2026-03-03 ECS → Lightsail 전환(3415e0c) 이후 배포는
# `.github/workflows/PROD_CICD.yml`이 jar를 scp로 전송하고
# systemd(toronchul.service)가 `java -jar app.jar`로 실행하는 방식이다.
# 이 Dockerfile을 참조하는 워크플로는 없다.
#
# 전환 당시 이 파일의 `ENV TZ=Asia/Seoul`이 함께 유실되어 운영 JVM이 UTC로
# 동작했고, 채팅 시간이 9시간 어긋나는 문제가 있었다(44043fd에서 수정).
# 아래 설정을 "현재 적용 중"으로 오해하지 말 것. 특히 -XX:+UseSerialGC는
# 현재 systemd ExecStart에 없어 실제로는 G1GC로 동작한다.

FROM amazoncorretto:17.0.7-al2023-headless AS builder

WORKDIR /build

COPY build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM amazoncorretto:17.0.7-al2023-headless

WORKDIR /app

ENV TZ=Asia/Seoul

COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

EXPOSE 80

# 작은 메모리 환경에 최적화된 Serial GC 사용
ENTRYPOINT ["java"]
CMD ["-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-XX:+UseSerialGC", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "org.springframework.boot.loader.launch.JarLauncher"]