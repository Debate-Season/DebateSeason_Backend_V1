FROM amazoncorretto:17.0.7-al2023-headless
WORKDIR /app

RUN dnf install -y shadow-utils && \
    adduser -r spring

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "app.jar"]