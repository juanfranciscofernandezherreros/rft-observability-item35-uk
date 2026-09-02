-- Fixture H2 para probar ITEM35A e ITEM35B con itemDate=20240315.
--
-- Resultado esperado:
--   ITEM35A: 5 filas de datos + 1 cabecera.
--   ITEM35B: 15 filas de datos + 1 cabecera.
--
-- El microservicio usa dos conexiones H2 distintas. Ejecutar cada bloque en
-- la conexion indicada, despues de crear las tablas con los scripts normales.

-- ============================================================================
-- BLOQUE 1: jdbc:h2:mem:kudu
-- ============================================================================

DELETE FROM EMIR_REFIT_DEV_CONTROL_REFIT.record_status;
DELETE FROM EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing;
DELETE FROM EMIR_REFIT_DEV_ACCOUNT_MNG.regu_identity;

-- ITEM35A: cinco grupos diferentes dentro de 2024-02-01 <= fecha < 2024-03-01.
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status
    (receiveddt, status, channel)
VALUES
    ('2024-02-01', 'ACPT', 'api'),
    ('2024-02-02', 'RJCT', 'api'),
    ('2024-02-11', 'ACPT', 'web'),
    ('2024-02-18', 'ACPT', 'sftp'),
    ('2024-02-19', 'RJCT', 'api');

-- ITEM35B: seis resultados PARTICIPANT.
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
    (outgoingfilename, filetype, reportingsessiontimestamp, creationtimestamp, accountid)
VALUES
    ('eudbia0o5000_S030_20240404091131_TAR_0001.xml',  'TAR030',  '2024-02-02 05:12:55.421', '2024-02-02 05:12:55.421', 'eudbif3q0000'),
    ('eudbia0o5000_S030_20240404091131_TAR_0001.xml',  'TAR030',  '2024-02-03 05:12:50.421', '2024-02-03 05:12:50.421', 'eudbif3q0000'),
    ('eudbia0o5000_S030_20240404091131_RJC_0001.xml',  'RJCT000', '2024-02-03 05:12:55.421', '2024-02-03 06:12:55.421', 'eudbif3q0000'),
    ('eudbia0o5000_S030_20240404091131_WARN_0001.xml', 'WARN000', '2024-02-03 05:12:55.421', '2024-02-03 06:12:55.421', 'eudbif3q0000'),
    ('eudbia0o5000_S030_20240404091131_TSR_0001.xml',  'TSR109',  '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'eudbif3q0000'),
    ('eudbia0o5000_S030_20240404091131_TSR_0001.xml',  'TSR107',  '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'eudbif3q0000');

-- ITEM35B: ocho resultados de regulador.
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
    (outgoingfilename, filetype, reportingsessiontimestamp, creationtimestamp, accountid)
VALUES
    ('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip',              'TAR030',  '2024-02-02 05:12:55.421', '2024-02-02 05:12:55.421', 'eudritrace'),
    ('TRRGS_DATREC_eudrp4ea0000_SR0002G-240715_001001-0.zip',      'TAR030',  '2024-02-03 05:12:50.421', '2024-02-03 05:12:50.421', 'eudrp4ea0000'),
    ('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip',              'TAR030',  '2024-02-03 05:12:50.421', '2024-02-03 05:12:50.421', 'eudrif3q0000'),
    ('TRRGS_DATTAR_EUDRIRA1051_R60003-240301_001001-0.zip',        'RJCT000', '2024-02-03 05:12:55.421', '2024-02-04 13:12:55.421', 'eudrif3q0000'),
    ('TRRGS_DATTAR_EUDRIRA1051_R60003-240204_001001-0.zip',        'WARN000', '2024-02-04 05:12:55.421', '2024-02-04 13:12:55.421', 'eudrif3q0000'),
    ('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip',              'TSR109',  '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'eudrif3q0000'),
    ('TRRGS_DATTSR_ESMAS_R11526-240301_001001-0.zip',              'TSR107',  '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'eudrif3q0000'),
    ('TRRGS_DATTSR_eudrp0pbp000_R11527-240301_001001-0.zip',       'TSR109',  '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'eudrp0pbp000');

-- ITEM35B: un resultado TR. Los tipos RL y TD no se usan: la configuracion
-- productiva del reporte consulta RL078 y TD107.
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
    (outgoingfilename, filetype, reportingsessiontimestamp, creationtimestamp, accountid)
VALUES
    ('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip', 'RL078', '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'trdrif3q0000');

-- Identidades necesarias para que RegulatorService procese los resultados.
-- CAFME y NATIONAL BANK OF ROMANIA se completan mediante application-uk.yml.
INSERT INTO EMIR_REFIT_DEV_ACCOUNT_MNG.regu_identity
    (tracecode, regulatorid, traceconnectivity)
VALUES
    ('ESMAS', 'eudri2frb000', 'true');

-- Control ITEM35A. Debe devolver 5.
SELECT COUNT(*) AS item35a_rows
FROM (
    SELECT receiveddt, status, channel
    FROM EMIR_REFIT_DEV_CONTROL_REFIT.record_status
    WHERE channel IN ('sftp', 'api', 'web')
      AND status IN ('ACPT', 'RJCT')
      AND receiveddt >= '2024-02-01'
      AND receiveddt < '2024-03-01'
    GROUP BY receiveddt, status, channel
);

-- Controles de las tres partes de ITEM35B: deben devolver 6, 8 y 1.
SELECT COUNT(*) AS item35b_participant_rows
FROM (
    SELECT CAST(reportingsessiontimestamp AS DATE), filetype
    FROM EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
    WHERE reportingsessiontimestamp >= TIMESTAMP '2024-02-01 00:00:00'
      AND reportingsessiontimestamp < TIMESTAMP '2024-03-01 00:00:00'
      AND LOWER(accountid) LIKE 'eudb%'
      AND filetype IN ('TAR030', 'TAR108', 'TSR107', 'TSR109', 'RJ092', 'RJCT000', 'WARN000')
    GROUP BY CAST(reportingsessiontimestamp AS DATE), filetype
);

SELECT COUNT(*) AS item35b_regulator_rows
FROM EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
WHERE reportingsessiontimestamp >= TIMESTAMP '2024-02-01 00:00:00'
  AND reportingsessiontimestamp < TIMESTAMP '2024-03-01 00:00:00'
  AND LOWER(accountid) LIKE 'eudr%'
  AND filetype IN ('TAR030', 'TAR108', 'TSR107', 'TSR109', 'RJ092', 'RJCT000', 'WARN000', 'RECS', 'RECI', 'TPST000');

SELECT COUNT(*) AS item35b_tr_rows
FROM (
    SELECT filetype, reportingsessiontimestamp, accountid
    FROM EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
    WHERE reportingsessiontimestamp >= TIMESTAMP '2024-02-01 00:00:00'
      AND reportingsessiontimestamp < TIMESTAMP '2024-03-01 00:00:00'
      AND LOWER(accountid) LIKE 'tr%'
      AND filetype IN ('RL078', 'TD107')
    GROUP BY filetype, reportingsessiontimestamp, accountid
);

-- ============================================================================
-- BLOQUE 2: jdbc:h2:mem:DBO;MODE=MSSQLServer
-- ============================================================================

DELETE FROM dbo.report_eod_state;

-- Este registro permite probar la rama que calcula el SLA de regulador desde
-- el estado EOD. No anade una fila nueva al CSV.
INSERT INTO dbo.report_eod_state
    (report_type, reporting_session, target_type, reporting_process, started_date)
VALUES
    ('TRAR000', '2024-02-02', 'AUTHORITY', 'EOD', '2024-02-02 01:00:00.000');

SELECT COUNT(*) AS report_eod_rows
FROM dbo.report_eod_state;
