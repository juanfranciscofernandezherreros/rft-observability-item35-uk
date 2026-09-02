# Tablas utilizadas por ITEM35

Este documento inventaría las tablas referenciadas por las entidades JPA, los repositorios y los scripts SQL del microservicio.

## Inventario

| Motor | Esquema | Tabla | Nombre completo | Uso en runtime | Operaciones | Reportes relacionados |
|---|---|---|---|---|---|---|
| Kudu/Impala | `EMIR_REFIT_DEV_CONTROL_REFIT` | `record_status` | `EMIR_REFIT_DEV_CONTROL_REFIT.record_status` | Sí | `SELECT` agrupado | ITEM35A Submission Volumes |
| Kudu/Impala | `EMIR_REFIT_DEV_CONTROL_REFIT` | `reports_file_outgoing` | `EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing` | Sí | `SELECT` y agregaciones | ITEM35B Report Generation |
| Kudu/Impala | `EMIR_REFIT_DEV_ACCOUNT_MNG` | `regu_identity` | `EMIR_REFIT_DEV_ACCOUNT_MNG.regu_identity` | Sí | `SELECT` por `tracecode` | ITEM35B Report Generation |
| SQL Server | `dbo` | `report_eod_state` | `dbo.report_eod_state` | Sí | `SELECT` | ITEM35B Report Generation |
| SQL Server | `dbo` | `item_reporting` | `dbo.item_reporting` | Sí | `SELECT`, `INSERT` y `UPDATE` | ITEM35A, ITEM35B, ITEM35C e ITEM35D |
| SQL Server | `dbo` | `item_reporting_state` | `dbo.item_reporting_state` | No | Sin acceso desde el código actual | Sin consumo directo |

## Detalle

### `record_status`

- **Entidad:** `RecordStatusEntity`.
- **Columnas:** `id`, `receiveddt`, `status`, `channel`, `nomessagesOnGiveDate`.
- **Finalidad:** obtiene los volúmenes de mensajes agrupados por fecha, estado y canal para ITEM35A.
- **Código:** `source/src/main/java/com/sixgroup/refit/observability/item35/creator/infrastructure/entity/kudu/control/RecordStatusEntity.java`.
- **Repositorio:** `source/src/main/java/com/sixgroup/refit/observability/item35/creator/infrastructure/repository/kudu/control/RecordStatusKudu.java`.

### `reports_file_outgoing`

- **Entidad:** `ReportingFileEntity`.
- **Columnas:** `id`, `filetype`, `outgoingfilename`, `reportingsessiontimestamp`, `creationtimestamp`, `accountid`.
- **Finalidad:** obtiene los ficheros salientes y sus tiempos de generación para participantes, reguladores y TR en ITEM35B.
- **Código:** `source/src/main/java/com/sixgroup/refit/observability/item35/creator/infrastructure/entity/kudu/control/ReportingFileEntity.java`.
- **Repositorio:** `source/src/main/java/com/sixgroup/refit/observability/item35/creator/infrastructure/repository/kudu/control/ReportingFileKudu.java`.

### `regu_identity`

- **Entidad:** `ReguIdentityEntity`.
- **Columnas:** `regulatorid`, `tracecode`, `traceconnectivity`.
- **Finalidad:** resuelve la identidad del regulador y su conectividad a partir del `tracecode` para ITEM35B.
- **Código:** `source/src/main/java/com/sixgroup/refit/observability/item35/creator/infrastructure/entity/kudu/account/ReguIdentityEntity.java`.
- **Repositorio:** `source/src/main/java/com/sixgroup/refit/observability/item35/creator/infrastructure/repository/kudu/account/ReguIdentityKudu.java`.

### `report_eod_state`

- **Entidad:** `ReportEoDStateEntity`.
- **Columnas:** `report_type`, `reporting_session`, `target_type`, `reporting_process`, `started_date`.
- **Finalidad:** aporta la hora de inicio del proceso EOD utilizada para calcular el tiempo de generación en ITEM35B.
- **Código:** `source/src/main/java/com/sixgroup/refit/observability/item35/creator/infrastructure/entity/sqlserver/ReportEoDStateEntity.java`.
- **Repositorio:** `source/src/main/java/com/sixgroup/refit/observability/item35/creator/infrastructure/repository/sqlserver/reportstate/SqlServerReportEodProcessStateRepository.java`.

### `item_reporting`

- **Entidad:** `ItemReportingEntity`.
- **Columnas:** `id`, `item_type`, `file_name`, `file_url`, `file_creation_date`, `file_update_date`, `state_name`, `state_update_date`.
- **Finalidad:** persiste y consulta el estado de ejecución y entrega de cada fichero generado por ITEM35A, ITEM35B, ITEM35C e ITEM35D.
- **Código:** `source/src/main/java/com/sixgroup/refit/observability/item/state/infrastructure/entity/ItemReportingEntity.java`.
- **Repositorio:** `source/src/main/java/com/sixgroup/refit/observability/item/state/infrastructure/repository/sqlserver/SqlServerItemReportingRepository.java`.

### `item_reporting_state`

- **Definición:** tabla declarada únicamente en el script SQL de pruebas.
- **Columnas:** `id`, `state_name`, `state_date`, `comment`, `item_reporting_id`.
- **Relación:** `item_reporting_id` referencia a `item_reporting`.
- **Uso actual:** no existe una entidad ni un repositorio que acceda directamente a esta tabla en el código Java.
- **Script local:** `source/src/main/resources/db/init_mssql.sql`.

## Origen de ITEM35C e ITEM35D

ITEM35C Storage Capacity e ITEM35D Compute Capacity no obtienen sus métricas principales de tablas JPA del proyecto. Sus datos proceden de las integraciones externas configuradas para Cloudera. Ambos reportes utilizan `item_reporting` únicamente para mantener el estado del fichero y del proceso.
