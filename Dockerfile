FROM eclipse-temurin:17-jre

WORKDIR /app

COPY source/target/rft-observability-item35-creator-1.11.0-SNAPSHOT.jar app.jar
COPY source/src/test/resources/db/sqlServer/init_kudu.sql db/init_kudu.sql
COPY source/src/test/resources/db/sqlServer/init_mssql.sql db/init_mssql.sql

RUN mkdir -p /data/reports

EXPOSE 8042 9040

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
