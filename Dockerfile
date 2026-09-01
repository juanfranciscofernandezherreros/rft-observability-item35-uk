FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY source/pom.xml pom.xml
RUN mvn --batch-mode dependency:go-offline

COPY source/src src
RUN mvn --batch-mode clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/target/rft-observability-item35-creator-1.11.0-SNAPSHOT.jar app.jar
COPY --from=build /workspace/src/test/resources/db/sqlServer/init_kudu.sql db/init_kudu.sql
COPY --from=build /workspace/src/test/resources/db/sqlServer/init_mssql.sql db/init_mssql.sql

RUN mkdir -p /data/reports

EXPOSE 8042 9040

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
