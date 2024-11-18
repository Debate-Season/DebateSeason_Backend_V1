FROM eclipse-temurin:17-jre-jammy AS extractor

WORKDIR /extract
COPY build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Run stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN adduser --system --group spring
USER spring

COPY --from=extractor /extract/dependencies/ ./
COPY --from=extractor /extract/spring-boot-loader/ ./
COPY --from=extractor /extract/snapshot-dependencies/ ./
COPY --from=extractor /extract/application/ ./

EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "org.springframework.boot.loader.JarLauncher"]