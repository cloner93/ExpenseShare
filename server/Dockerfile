# Stage 1: Build application
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradle gradle
COPY gradlew settings.gradle.kts gradle.properties build.gradle.kts ./
COPY build-logic build-logic
COPY core core
COPY server server

# Remove any host-specific java.home and build server distribution
RUN sed -i '/org.gradle.java.home/d' gradle.properties && \
    chmod +x gradlew && \
    ./gradlew :server:installDist --no-daemon

# Stage 2: Minimal runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/server/build/install/server ./

EXPOSE 8082
ENV PORT=8082

CMD ["./bin/server"]
