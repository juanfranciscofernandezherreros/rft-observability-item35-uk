# rft-observability-item35-creator

## Local Docker Compose

Build the application JAR with the Maven installation on the host. This keeps
the Docker build independent from Java trust-store and corporate proxy
configuration:

```powershell
mvn.cmd -f source/pom.xml --batch-mode clean package "-Dmaven.test.skip=true"
```

Then build the runtime image and start the complete stack:

```powershell
docker compose up -d --build
```

This starts the item35 service, Kafka, ZooKeeper, Schema Registry and Kafka UI.
It also creates `rft.dev.observability.item.private.v1` and registers its Avro
key and value schemas.

- Kafka UI: http://localhost:9080
- Item35 API: http://localhost:8042
- Item35 health: http://localhost:9040/actuator/health
- Schema Registry: http://localhost:8081
- Kafka from the host: `localhost:9092`
- Generated CSV files: `source/src/main/resources/reports`

Stop the stack while retaining Kafka data:

```powershell
docker compose down
```

Remove the stack and its Kafka data:

```powershell
docker compose down --volumes
```
