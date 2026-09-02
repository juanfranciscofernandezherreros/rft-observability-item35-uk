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

This starts the item35 service, Kafka, ZooKeeper, Schema Registry, Kafka UI,
Prometheus, Loki, Promtail and Grafana. It also creates
`rft.dev.observability.item.private.v1` and registers its Avro key and value
schemas.

- Kafka UI: http://localhost:9080
- Item35 API: http://localhost:8042
- Item35 health: http://localhost:9040/actuator/health
- Item35 Prometheus metrics: http://localhost:9040/actuator/prometheus
- Schema Registry: http://localhost:8081
- Kafka from the host: `localhost:9092`
- Prometheus: http://localhost:9090
- Loki readiness: http://localhost:3100/ready
- Grafana: http://localhost:3000 (`admin` / `admin`)
- Generated CSV files: `source/src/main/resources/reports`

## Local observability

Grafana provisions the Prometheus and Loki datasources automatically. Open the
`Item35 / Item35 local observability` dashboard, or use its direct URL:

http://localhost:3000/d/item35-local-observability/item35-local-observability

The dashboard includes JVM memory, process and system CPU, HTTP request rate,
and the Docker logs emitted by Item35. Prometheus scrapes Item35 every five
seconds. Promtail discovers the containers in this Compose project through the
Docker socket and sends their logs to Loki.

Useful queries:

```promql
up{job="item35"}
```

```logql
{compose_service="item35"}
```

Prometheus, Loki and Grafana data is retained in named volumes. Running
`docker compose down --volumes` removes that local observability history.

## Launch all four reports concurrently

Wait until Kafka UI and Item35 are healthy. Then run the following PowerShell
block from the repository root. It starts all four `curl.exe` processes before
waiting for any response, so the four report requests are published
concurrently.

```powershell
$endpoint = "http://localhost:9080/api/clusters/item35-local/topics/rft.dev.observability.item.private.v1/messages"
$itemDate = "20240315"
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

$submissionVolumesPayload = @"
{"partition":0,"key":"{\"itemId\":\"item35\"}","headers":{},"content":"{\"itemId\":\"item35\",\"itemType\":\"submissionVolumes\",\"command\":\"request\",\"itemDate\":\"$itemDate\",\"creationTimestamp\":$timestamp,\"fileInfo\":{\"fileName\":\"\",\"fileUrl\":\"\"}}","keySerde":"SchemaRegistry","valueSerde":"SchemaRegistry"}
"@

$reportGenerationPayload = @"
{"partition":0,"key":"{\"itemId\":\"item35\"}","headers":{},"content":"{\"itemId\":\"item35\",\"itemType\":\"reportGeneration\",\"command\":\"request\",\"itemDate\":\"$itemDate\",\"creationTimestamp\":$timestamp,\"fileInfo\":{\"fileName\":\"\",\"fileUrl\":\"\"}}","keySerde":"SchemaRegistry","valueSerde":"SchemaRegistry"}
"@

$storageCapacityPayload = @"
{"partition":0,"key":"{\"itemId\":\"item35\"}","headers":{},"content":"{\"itemId\":\"item35\",\"itemType\":\"storageCapacity\",\"command\":\"request\",\"itemDate\":\"$itemDate\",\"creationTimestamp\":$timestamp,\"fileInfo\":{\"fileName\":\"\",\"fileUrl\":\"\"}}","keySerde":"SchemaRegistry","valueSerde":"SchemaRegistry"}
"@

$computeCapacityPayload = @"
{"partition":0,"key":"{\"itemId\":\"item35\"}","headers":{},"content":"{\"itemId\":\"item35\",\"itemType\":\"computeCapacity\",\"command\":\"request\",\"itemDate\":\"$itemDate\",\"creationTimestamp\":$timestamp,\"fileInfo\":{\"fileName\":\"\",\"fileUrl\":\"\"}}","keySerde":"SchemaRegistry","valueSerde":"SchemaRegistry"}
"@

$jobs = @(
    Start-Job -Name "ITEM35A-submissionVolumes" -ScriptBlock {
        param($url, $payload)
        $payload | curl.exe --fail-with-body --silent --show-error `
            --request POST `
            --header "Content-Type: application/json" `
            --data-binary "@-" `
            --write-out "ITEM35A HTTP %{http_code}`n" `
            $url
    } -ArgumentList $endpoint, $submissionVolumesPayload

    Start-Job -Name "ITEM35B-reportGeneration" -ScriptBlock {
        param($url, $payload)
        $payload | curl.exe --fail-with-body --silent --show-error `
            --request POST `
            --header "Content-Type: application/json" `
            --data-binary "@-" `
            --write-out "ITEM35B HTTP %{http_code}`n" `
            $url
    } -ArgumentList $endpoint, $reportGenerationPayload

    Start-Job -Name "ITEM35C-storageCapacity" -ScriptBlock {
        param($url, $payload)
        $payload | curl.exe --fail-with-body --silent --show-error `
            --request POST `
            --header "Content-Type: application/json" `
            --data-binary "@-" `
            --write-out "ITEM35C HTTP %{http_code}`n" `
            $url
    } -ArgumentList $endpoint, $storageCapacityPayload

    Start-Job -Name "ITEM35D-computeCapacity" -ScriptBlock {
        param($url, $payload)
        $payload | curl.exe --fail-with-body --silent --show-error `
            --request POST `
            --header "Content-Type: application/json" `
            --data-binary "@-" `
            --write-out "ITEM35D HTTP %{http_code}`n" `
            $url
    } -ArgumentList $endpoint, $computeCapacityPayload
)

$jobs | Wait-Job | Receive-Job
$jobs | Remove-Job
```

### Bash: launch ITEM35A and ITEM35B

The following commands publish `itemDate=20240315`, so the local H2 fixtures
for February 2024 produce 5 ITEM35A rows and 15 ITEM35B rows.

ITEM35A:

```bash
timestamp=$(date +%s%3N)

curl --fail-with-body --silent --show-error \
  --request POST \
  --header 'Content-Type: application/json' \
  --data-binary @- \
  'http://localhost:9080/api/clusters/item35-local/topics/rft.dev.observability.item.private.v1/messages' <<EOF
{
  "partition": 0,
  "key": "{\"itemId\":\"item35\"}",
  "headers": {},
  "content": "{\"itemId\":\"item35\",\"itemType\":\"submissionVolumes\",\"command\":\"request\",\"itemDate\":\"20240315\",\"creationTimestamp\":${timestamp},\"fileInfo\":{\"fileName\":\"\",\"fileUrl\":\"\"}}",
  "keySerde": "SchemaRegistry",
  "valueSerde": "SchemaRegistry"
}
EOF
```

ITEM35B:

```bash
timestamp=$(date +%s%3N)

curl --fail-with-body --silent --show-error \
  --request POST \
  --header 'Content-Type: application/json' \
  --data-binary @- \
  'http://localhost:9080/api/clusters/item35-local/topics/rft.dev.observability.item.private.v1/messages' <<EOF
{
  "partition": 0,
  "key": "{\"itemId\":\"item35\"}",
  "headers": {},
  "content": "{\"itemId\":\"item35\",\"itemType\":\"reportGeneration\",\"command\":\"request\",\"itemDate\":\"20240315\",\"creationTimestamp\":${timestamp},\"fileInfo\":{\"fileName\":\"\",\"fileUrl\":\"\"}}",
  "keySerde": "SchemaRegistry",
  "valueSerde": "SchemaRegistry"
}
EOF
```

The four expected HTTP responses are `200`. Kafka preserves message order in
the topic's single partition, while Item35 delegates each consumed request to
its asynchronous task executor. Generated files appear under
`source/src/main/resources/reports`.

Stop the stack while retaining Kafka data:

```powershell
docker compose down
```

Remove the stack and its Kafka data:

```powershell
docker compose down --volumes
```
