CREATE SCHEMA IF NOT EXISTS EMIR_REFIT_DEV_CONTROL_REFIT;
CREATE SCHEMA IF NOT EXISTS EMIR_REFIT_DEV_ACCOUNT_MNG;

CREATE TABLE IF NOT EXISTS EMIR_REFIT_DEV_CONTROL_REFIT.record_status
(
    id                    BIGINT PRIMARY KEY,
    receiveddt            VARCHAR(255),
    status                VARCHAR(255),
    channel               VARCHAR(255),
    nomessagesOnGiveDate  INTEGER
);

CREATE TABLE IF NOT EXISTS EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
(
    id                          BIGINT PRIMARY KEY,
    outgoingfilename            VARCHAR(255),
    filetype                    VARCHAR(255),
    reportingsessiontimestamp   TIMESTAMP,
    creationtimestamp           TIMESTAMP,
    accountid                   VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS EMIR_REFIT_DEV_ACCOUNT_MNG.regu_identity
(
    tracecode            VARCHAR(255),
    regulatorid          VARCHAR(255) PRIMARY KEY,
    traceconnectivity    VARCHAR(255)
);

-- ITEM35A: 5 output groups for itemDate=20240315.
MERGE INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status
    (id, receiveddt, status, channel, nomessagesOnGiveDate) KEY(id)
VALUES
    (1, '2024-02-01', 'ACPT', 'api', 1),
    (2, '2024-02-02', 'RJCT', 'api', 1),
    (3, '2024-02-11', 'ACPT', 'web', 1),
    (4, '2024-02-18', 'ACPT', 'sftp', 1),
    (5, '2024-02-19', 'RJCT', 'api', 1);

-- ITEM35B: 6 participant results.
MERGE INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
    (id, outgoingfilename, filetype, reportingsessiontimestamp, creationtimestamp, accountid) KEY(id)
VALUES
    (101, 'eudbia0o5000_S030_20240404091131_TAR_0001.xml',  'TAR030',  '2024-02-02 05:12:55.421', '2024-02-02 05:12:55.421', 'eudbif3q0000'),
    (102, 'eudbia0o5000_S030_20240404091131_TAR_0001.xml',  'TAR030',  '2024-02-03 05:12:50.421', '2024-02-03 05:12:50.421', 'eudbif3q0000'),
    (103, 'eudbia0o5000_S030_20240404091131_RJC_0001.xml',  'RJCT000', '2024-02-03 05:12:55.421', '2024-02-03 06:12:55.421', 'eudbif3q0000'),
    (104, 'eudbia0o5000_S030_20240404091131_WARN_0001.xml', 'WARN000', '2024-02-03 05:12:55.421', '2024-02-03 06:12:55.421', 'eudbif3q0000'),
    (105, 'eudbia0o5000_S030_20240404091131_TSR_0001.xml',  'TSR109',  '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'eudbif3q0000'),
    (106, 'eudbia0o5000_S030_20240404091131_TSR_0001.xml',  'TSR107',  '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'eudbif3q0000');

-- ITEM35B: 8 regulator results.
MERGE INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
    (id, outgoingfilename, filetype, reportingsessiontimestamp, creationtimestamp, accountid) KEY(id)
VALUES
    (201, 'TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip',         'TAR030',  '2024-02-02 05:12:55.421', '2024-02-02 05:12:55.421', 'eudritrace'),
    (202, 'TRRGS_DATREC_eudrp4ea0000_SR0002G-240715_001001-0.zip', 'TAR030',  '2024-02-03 05:12:50.421', '2024-02-03 05:12:50.421', 'eudrp4ea0000'),
    (203, 'TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip',         'TAR030',  '2024-02-03 05:12:50.421', '2024-02-03 05:12:50.421', 'eudrif3q0000'),
    (204, 'TRRGS_DATTAR_EUDRIRA1051_R60003-240301_001001-0.zip',   'RJCT000', '2024-02-03 05:12:55.421', '2024-02-04 13:12:55.421', 'eudrif3q0000'),
    (205, 'TRRGS_DATTAR_EUDRIRA1051_R60003-240204_001001-0.zip',   'WARN000', '2024-02-04 05:12:55.421', '2024-02-04 13:12:55.421', 'eudrif3q0000'),
    (206, 'TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip',         'TSR109',  '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'eudrif3q0000'),
    (207, 'TRRGS_DATTSR_ESMAS_R11526-240301_001001-0.zip',         'TSR107',  '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'eudrif3q0000'),
    (208, 'TRRGS_DATTSR_eudrp0pbp000_R11527-240301_001001-0.zip',  'TSR109',  '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'eudrp0pbp000');

-- ITEM35B: 1 TR result.
MERGE INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
    (id, outgoingfilename, filetype, reportingsessiontimestamp, creationtimestamp, accountid) KEY(id)
VALUES
    (301, 'TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip', 'RL078', '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'trdrif3q0000');

MERGE INTO EMIR_REFIT_DEV_ACCOUNT_MNG.regu_identity
    (tracecode, regulatorid, traceconnectivity) KEY(regulatorid)
VALUES
    ('ESMAS', 'eudri2frb000', 'true');
