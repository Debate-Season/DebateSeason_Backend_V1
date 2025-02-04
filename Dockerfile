FROM amazoncorretto:17.0.7-al2023-headless AS builder

WORKDIR /build

COPY build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM amazoncorretto:17.0.7-al2023-headless

WORKDIR /app

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
     "org.springframework.boot.loader.JarLauncher"]