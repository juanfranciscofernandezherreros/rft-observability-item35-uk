FROM eclipse-temurin:17-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY source/target/rft-observability-item35-creator-1.11.0-SNAPSHOT.jar app.jar

RUN mkdir -p /data/reports

EXPOSE 8042 9040

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
