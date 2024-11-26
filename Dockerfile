FROM amazoncorretto:17.0.7-al2023-headless AS builder

WORKDIR /build

# 빌드 결과물 복사
COPY build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Run stage
FROM amazoncorretto:17.0.7-al2023-headless

WORKDIR /app

RUN dnf install -y shadow-utils && \
    useradd -r -s /bin/false spring && \
    dnf clean all

COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

RUN chown -R spring:spring /app
USER spring

EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "org.springframework.boot.loader.launch.JarLauncher"]