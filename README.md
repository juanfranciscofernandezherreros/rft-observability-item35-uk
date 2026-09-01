# rft-observability-item35-creator

## Local Docker Compose

The complete local stack runs without Kubernetes or Argo CD:

```powershell
docker compose up -d
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

## Local Docker Compose

The complete local stack runs without Kubernetes or Argo CD:

```powershell
docker compose up -d
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
