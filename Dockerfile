FROM openjdk:8u171-jdk-alpine3.8 as builder

ADD . /app
WORKDIR /app

RUN chmod +x ./gradlew \
    && ./gradlew :shadowJar \
    && mv build/libs/server-1.0.jar ./server.jar

FROM openjdk:8u181-jre-alpine3.8 as environment
WORKDIR /app
COPY --from=builder /app/server.jar .
ENTRYPOINT ["java", "-jar", "server.jar"]