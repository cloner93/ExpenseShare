FROM gradle:8.13-jdk21 AS builder

WORKDIR /app

COPY gradle/                gradle/
COPY gradlew                gradlew
COPY gradlew.bat            gradlew.bat
COPY settings.gradle.kts    settings.gradle.kts
COPY build.gradle.kts       build.gradle.kts
COPY gradle.properties      gradle.properties

COPY core/currency/         core/currency/
COPY core/logger/           core/logger/
COPY server/                server/

RUN ./gradlew :server:dependencies --no-daemon

RUN ./gradlew :server:buildFatJar --no-daemon --stacktrace

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/server/build/libs/*-all.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
