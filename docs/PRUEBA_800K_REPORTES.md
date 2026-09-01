# Prueba de generacion de reportes Item 35 con 800.000 registros

> **Estado actual:** esta fue una prueba sintetica. El mecanismo que repetia registros hasta alcanzar 800.000 filas fue eliminado el 2 de septiembre de 2026. La version actual escribe exclusivamente los elementos recibidos de las tablas o APIs.

## 1. Objetivo

Esta prueba valida que el microservicio `rft-observability-item35-creator` puede:

- consumir solicitudes Avro desde Kafka;
- generar simultaneamente los cuatro reportes Item 35;
- escribir 800.000 registros de datos en cada CSV;
- almacenar los ficheros mediante el volumen montado por Docker Compose; y
- mantener un consumo de memoria medible para orientar la configuracion de Kubernetes.

La prueba es una prueba de volumen de salida. No representa, por si sola, la carga en memoria de recuperar 800.000 objetos diferentes desde Kudu, SQL Server o la API de Cloudera.

## 2. Entorno utilizado

El stack se levanta con:

```powershell
docker compose up -d --build
```

Incluye:

| Componente | Acceso o funcion |
| --- | --- |
| Kafka | `localhost:9092` |
| Topic | `rft.dev.observability.item.private.v1` |
| Schema Registry | `http://localhost:8081` |
| Kafka UI | `http://localhost:9080` |
| Microservicio | puertos `8042` y `9040` |
| Directorio de salida | `source/src/main/resources/reports` |

El perfil activo es `compose,uk`. Las bases locales son H2 en memoria y simulan los esquemas usados por Kudu y SQL Server mediante los scripts `init_kudu.sql` e `init_mssql.sql`.

## 3. De donde salen los datos

### 3.1 Submission Volumes, ITEM35A

En un entorno real, la fuente principal es la tabla Kudu `record_status`.

La consulta no devuelve una fila por cada registro de la tabla. Agrupa por fecha, estado y canal, y calcula `COUNT(1)`. Por tanto, aunque `record_status` tuviera 800.000 filas, el resultado final de la consulta podria contener solo unas decenas o centenas de filas agregadas.

En Compose, la tabla se simula en H2 con los datos de `init_kudu.sql`.

### 3.2 Report Generation, ITEM35B

En un entorno real, las fuentes principales son:

- `reports_file_outgoing` en Kudu;
- `regu_identity` en Kudu, para informacion de reguladores; y
- `report_eod_state` en SQL Server, como informacion auxiliar del proceso y SLA.

Parte de las consultas agrupa por fecha, tipo de fichero o cuenta usando `MIN` y `MAX`. Otras ramas, como determinados datos de regulador, pueden devolver registros con mayor detalle. El numero de filas del CSV depende de las reglas de union y transformacion, no exclusivamente del numero bruto de filas existentes en una tabla.

En Compose, estas tablas tambien se simulan con H2 y los scripts de inicializacion.

### 3.3 Storage Capacity, ITEM35C

En produccion no procede de una tabla. El microservicio consulta la API de Cloudera para obtener capacidad total y capacidad libre, empareja las series temporales y calcula capacidad usada y utilizacion.

En Compose, las llamadas estan desactivadas con `component-config.cloudera.storage.enabled: false`. Se utilizan los ficheros locales:

- `json/total_all_MBT.json`;
- `json/free_all_MBT.json`.

En la ejecucion documentada, esta transformacion produjo inicialmente 32 objetos.

### 3.4 Compute Capacity, ITEM35D

En produccion tampoco procede de una tabla. El servicio consulta la API de Cloudera para obtener las series de CPU, RAM usada y RAM total.

En Compose, CPU y RAM estan desactivadas como integraciones reales y se utilizan:

- `json/capacity-cpu.json`;
- `json/capacity-ram.json`;
- `json/capacity-ram-total.json`.

## 4. Como se generan exactamente 800.000 filas en local

Solo el perfil Compose contiene esta propiedad:

```yaml
component-config:
  csv:
    output_path: /data/reports/
    row_count: 800000
```

Los cuatro escritores de CSV delegan en `CsvRows.write`. El algoritmo es equivalente a:

```java
int rowCount = configuredRowCount > 0 ? configuredRowCount : source.size();
for (int index = 0; index < rowCount; index++) {
    writer.accept(source.get(index % source.size()));
}
```

El operador modulo hace que se recorra circularmente la lista obtenida de la fuente:

```text
Fuente: A, B, C
Salida: A, B, C, A, B, C, A, B, C, ... hasta 800.000
```

Propiedades de esta implementacion:

- no crea una segunda lista Java de 800.000 elementos;
- escribe cada registro directamente en el `CSVWriter`;
- garantiza 800.000 filas si la lista fuente contiene al menos un elemento;
- si la fuente esta vacia, no inventa datos y no escribe registros;
- si `row_count` vale `0` o no esta configurado, escribe exactamente el numero de elementos devueltos por la fuente; y
- los registros repetidos conservan los valores de las fixtures locales, por lo que no son 800.000 casos de negocio unicos.

La propiedad no esta definida en los perfiles normales. Por tanto, en un despliegue real el comportamiento por defecto sigue siendo escribir las filas realmente calculadas por consultas y APIs, sin rellenar ni duplicar hasta 800.000.

## 5. Solicitudes enviadas a Kafka

Se publicaron cuatro eventos Avro con clave:

```json
{"itemId":"item35"}
```

Los valores utilizaron esta estructura, cambiando `itemType` para cada reporte:

```json
{
  "itemId": "item35",
  "itemType": "submissionVolumes",
  "command": "request",
  "itemDate": "20240315",
  "creationTimestamp": 1710460800000,
  "fileInfo": {
    "fileName": "",
    "fileUrl": ""
  }
}
```

Valores enviados para `itemType`:

- `submissionVolumes`;
- `reportGeneration`;
- `storageCapacity`;
- `computeCapacity`.

Las cuatro solicitudes se enviaron seguidas. El consumidor las recibio y los casos de uso se ejecutaron de forma asincrona y concurrente. Esto fuerza un escenario de memoria mas exigente que ejecutar un unico reporte.

## 6. Resultados

| Reporte | Fichero | Tamano | Filas fisicas | Filas de datos |
| --- | --- | ---: | ---: | ---: |
| ITEM35A | `TRRGS_UKEMIR_PR_FU_ND_ITEM35A_20240215.csv` | 38.560.106 bytes | 800.001 | 800.000 |
| ITEM35B | `TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20240215.csv` | 101.066.813 bytes | 800.001 | 800.000 |
| ITEM35C | `TRRGS_UKEMIR_PR_FU_ND_ITEM35C_20240215.csv` | 80.000.184 bytes | 800.001 | 800.000 |
| ITEM35D | `TRRGS_UKEMIR_PR_FU_ND_ITEM35D_20240215.csv` | 76.000.136 bytes | 800.001 | 800.000 |

Las 800.001 lineas fisicas corresponden a una cabecera y 800.000 registros de datos.

`itemDate=20240315` genera la fecha de referencia `20240215` porque la regla del servicio calcula el periodo de reporte anterior.

## 7. Medicion de memoria

Durante la generacion concurrente se muestreo `docker stats`. Al terminar se consulto tambien el contador exacto de cgroup v2:

```sh
cat /sys/fs/cgroup/memory.current
cat /sys/fs/cgroup/memory.peak
```

Resultados:

| Medida | Resultado |
| --- | ---: |
| Memoria anterior a los reportes | 703,50 MiB |
| Pico observado por muestreo | 1.044,48 MiB |
| Pico exacto de cgroup | 1.066,46 MiB, 1,041 GiB |

Como punto de partida para repetir esta misma carga sintetica en Kubernetes:

```yaml
resources:
  requests:
    memory: "1Gi"
  limits:
    memory: "1536Mi"
```

El limite de 1.536 MiB deja aproximadamente un 44 % de margen sobre el pico medido.

## 8. Que demuestra y que no demuestra la prueba

### Demuestra

- serializacion y consumo Avro mediante Kafka y Schema Registry;
- ejecucion concurrente de los cuatro casos de uso;
- escritura de cuatro CSV con 800.000 registros cada uno;
- funcionamiento del volumen entre el contenedor y el host;
- volumen final de disco de los CSV; y
- memoria consumida al escribir 3,2 millones de filas a partir de listas fuente pequenas.

### No demuestra

- consumo de memoria al mantener 800.000 DTO diferentes en una `List` Java;
- coste de transferir 800.000 resultados desde Kudu o SQL Server;
- coste de deserializar una respuesta JSON de Cloudera con 800.000 puntos;
- comportamiento de red, latencia y timeouts de las integraciones reales; ni
- que cada reporte de produccion vaya a contener necesariamente 800.000 filas.

Este matiz es importante: el escritor es eficiente porque repite en streaming una lista pequena. Si una integracion real devuelve una lista de 800.000 objetos, la memoria puede ser muy superior al pico medido. Ademas, las implementaciones de Cloudera convierten primero el cuerpo HTTP completo a `String` y despues construyen la lista de objetos, lo que puede mantener varias representaciones del mismo conjunto en memoria.

## 9. Prueba necesaria para dimensionamiento definitivo de produccion

Para validar un limite de memoria definitivo se debe realizar una segunda prueba sin `row_count` artificial:

1. Eliminar o establecer `component-config.csv.row_count: 0`.
2. Preparar una fuente con cardinalidad y tamano de campos equivalentes a produccion.
3. Para `submissionVolumes`, insertar 800.000 filas sirve para medir el trabajo de agregacion en la base, pero no producira 800.000 filas de CSV debido al `GROUP BY`.
4. Para `reportGeneration`, cargar combinaciones que sobrevivan a sus agrupaciones y uniones, midiendo por separado cada rama del reporte.
5. Para `storageCapacity` y `computeCapacity`, usar respuestas de API con el numero real de puntos esperado, no tablas artificiales.
6. Ejecutar primero cada reporte por separado y despues los cuatro concurrentemente.
7. Registrar `memory.peak`, CPU, duracion, pausas de GC, tamano del fichero y numero de filas.
8. Repetir varias veces y definir el limite sobre el mayor pico con un margen minimo del 25 al 40 %.

Hasta completar esa prueba, `1536Mi` es una recomendacion valida para la carga sintetica ejecutada, pero no debe considerarse una garantia para una entrada real de 800.000 objetos distintos por reporte.

## 10. Reproduccion y comprobacion

Estado del stack:

```powershell
docker compose ps -a
```

Seguimiento del microservicio:

```powershell
docker compose logs -f item35
```

Memoria actual:

```powershell
docker stats --no-stream rft-observability-item35-item35-1
```

Pico exacto acumulado desde que arranco el contenedor:

```powershell
docker exec rft-observability-item35-item35-1 `
  cat /sys/fs/cgroup/memory.peak
```

Conteo de lineas de un CSV sin cargarlo entero en memoria:

```powershell
$reader = [System.IO.File]::OpenText("ruta-del-reporte.csv")
$lines = 0L
try {
    while ($null -ne $reader.ReadLine()) { $lines++ }
} finally {
    $reader.Dispose()
}

"Filas fisicas: $lines"
"Filas de datos: $($lines - 1)"
```
