-- Carga H2 para generar 600.000 filas de datos en ITEM35A y 600.000 en ITEM35B.
-- Ejecutar en H2 Console sobre la conexion:
--   jdbc:h2:mem:kudu
-- Usuario:
--   sa
--
-- Los mensajes Kafka deben usar itemDate=20240315. El microservicio consultara:
--   2024-02-01 <= fecha < 2024-03-01
--
-- ADVERTENCIA: esta carga sustituye los datos locales de A y B. La aplicacion
-- materializa los resultados en listas Java antes de escribir los CSV, por lo
-- que 600.000 filas pueden consumir mucha memoria o provocar OOM.

SET AUTOCOMMIT FALSE;

DELETE FROM EMIR_REFIT_DEV_CONTROL_REFIT.record_status;
DELETE FROM EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing;
DELETE FROM EMIR_REFIT_DEV_ACCOUNT_MNG.regu_identity;

-- ============================================================================
-- ITEM35A: 600.000 grupos y, por tanto, 600.000 filas en el CSV.
-- ============================================================================
--
-- La consulta de ITEM35A agrupa por receiveddt, status y channel. No basta con
-- insertar 600.000 filas con la misma fecha: producirian una unica fila CSV.
-- Se genera un receiveddt VARCHAR distinto por milisegundo. Todas las cadenas
-- permanecen dentro de febrero de 2024 y cumplen los filtros ACPT/api.

INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status
    (id, receiveddt, status, channel, nomessagesOnGiveDate)
SELECT
    X,
    FORMATDATETIME(
        DATEADD('MILLISECOND', X - 1, TIMESTAMP '2024-02-01 00:00:00'),
        'yyyy-MM-dd HH:mm:ss.SSS'
    ),
    'ACPT',
    'api',
    1
FROM SYSTEM_RANGE(1, 600000);

-- ============================================================================
-- ITEM35B: 600.000 filas de regulador sin agrupacion.
-- ============================================================================
--
-- accountid comienza por eudr, TAR030 es un tipo admitido y el tercer segmento
-- del nombre de fichero contiene la traza ESMAS. Cada fila pasa directamente a
-- la coleccion de reguladores utilizada para generar ITEM35B.

INSERT INTO EMIR_REFIT_DEV_ACCOUNT_MNG.regu_identity
    (tracecode, regulatorid, traceconnectivity)
VALUES
    ('ESMAS', 'eudri2frb000', 'true');

INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
    (id, outgoingfilename, filetype, reportingsessiontimestamp, creationtimestamp, accountid)
SELECT
    1000000 + X,
    'TRRGS_DATTAR_ESMAS_R99996-240201_001001-0.zip',
    'TAR030',
    DATEADD('MILLISECOND', X - 1, TIMESTAMP '2024-02-01 05:12:55'),
    DATEADD('MILLISECOND', X - 1, TIMESTAMP '2024-02-01 05:13:55'),
    'eudrif3q0000'
FROM SYSTEM_RANGE(1, 600000);

COMMIT;

-- ============================================================================
-- Verificaciones. Los dos resultados deben ser exactamente 600000.
-- ============================================================================

SELECT COUNT(*) AS item35a_csv_rows
FROM (
    SELECT receiveddt, status, channel
    FROM EMIR_REFIT_DEV_CONTROL_REFIT.record_status
    WHERE channel IN ('sftp', 'api', 'web')
      AND status IN ('ACPT', 'RJCT')
      AND receiveddt >= '2024-02-01'
      AND receiveddt < '2024-03-01'
    GROUP BY receiveddt, status, channel
);

SELECT COUNT(*) AS item35b_csv_rows
FROM EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
WHERE reportingsessiontimestamp >= TIMESTAMP '2024-02-01 00:00:00'
  AND reportingsessiontimestamp < TIMESTAMP '2024-03-01 00:00:00'
  AND LOWER(accountid) LIKE 'eudr%'
  AND filetype IN (
      'TAR030', 'TAR108', 'TSR107', 'TSR109', 'RJ092',
      'RJCT000', 'WARN000', 'RECS', 'RECI', 'TPST000'
  );
