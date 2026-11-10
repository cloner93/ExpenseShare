FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY server-dist ./

EXPOSE 8082
ENV PORT=8082

CMD ["./bin/server"]
