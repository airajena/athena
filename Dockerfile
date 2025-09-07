FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN mkdir -p logs

COPY target/multi-threaded-webserver-4.0.0.jar app.jar

RUN addgroup --system appgroup && adduser --system --group appuser
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080 9090

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV KAFKA_SERVERS=kafka:9092
ENV METRICS_PORT=9090


ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
