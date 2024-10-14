
DROP ALL OBJECTS;

CREATE SCHEMA EMIR_REFIT_DEV_CONTROL_REFIT;
CREATE SCHEMA EMIR_REFIT_DEV_ACCOUNT_MNG;

CREATE TABLE EMIR_REFIT_DEV_CONTROL_REFIT.record_status
(
    rptgtmstmp            TIMESTAMP,
    status                VARCHAR(255),
    channel               VARCHAR(255)
);

CREATE TABLE EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing
(
    outgoingfilename            VARCHAR(255),
    filetype                    VARCHAR(255),
    reportingsessiontimestamp   TIMESTAMP,
    creationtimestamp           TIMESTAMP,
    accountid                   VARCHAR(255)
);

CREATE TABLE EMIR_REFIT_DEV_ACCOUNT_MNG.regu_identity
(
    tracecode            VARCHAR(255),
    regulatorid          VARCHAR(255),
    traceconnectivity    VARCHAR(255)
);


INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(rptgtmstmp,status,channel)VALUES('2024-02-18 00:00:00.000', 'ACPT', 'sftp');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(rptgtmstmp,status,channel)VALUES('2023-02-28 00:01:00.000', 'ACPT', 'web');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(rptgtmstmp,status,channel)VALUES('2024-02-02 02:00:00.000', 'RJCT', 'api');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(rptgtmstmp,status,channel)VALUES('2024-02-01 00:03:00.000', 'ACPT', 'api');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(rptgtmstmp,status,channel)VALUES('2024-02-19 04:00:00.000', 'RJCT', 'api');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(rptgtmstmp,status,channel)VALUES('2024-03-18 00:05:00.000', 'RJCT', 'sftp');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(rptgtmstmp,status,channel)VALUES('2024-02-11 06:00:00.000', 'ACPT', 'web');

INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('eudbia0o5000_S030_20240404091131_TAR_0001.xml', 'TAR030', '2024-02-02 05:12:55.421', '2024-02-02 05:12:55.421', 'eudbif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('eudbia0o5000_S030_20240404091131_TAR_0001.xml', 'TAR030', '2024-02-03 05:12:50.421', '2024-02-03 05:12:50.421', 'eudbif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('eudbia0o5000_S030_20240404091131_RJC_0001.xml', 'RJCT000', '2024-02-03 05:12:55.421', '2024-02-03 06:12:55.421', 'eudbif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('eudbia0o5000_S030_20240404091131_TSR_0001.xml', 'TSR109', '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'eudbif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('eudbia0o5000_S030_20240404091131_TSR_0001.xml', 'TSR107', '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'eudbif3q0000');

INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip', 'TAR030', '2024-02-02 05:12:55.421', '2024-02-02 05:12:55.421', 'eudritrace');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip', 'TAR030', '2024-02-03 05:12:50.421', '2024-02-03 05:12:50.421', 'eudrif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTAR_EUDRIRA1051_R60003-240301_001001-0.zip', 'RJCT000', '2024-02-03 05:12:55.421', '2024-02-04 13:12:55.421', 'eudrif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip', 'TSR109', '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'eudrif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTSR_ESMAS_R11526-240301_001001-0.zip', 'TSR107', '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'eudrif3q0000');

INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip', 'RL', '2024-02-02 05:12:55.421', '2024-02-02 05:12:55.421', 'trdrif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip', 'RL', '2024-02-03 05:12:50.421', '2024-02-03 05:12:50.421', 'trdrif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTAR_EUDRIRA1051_R60003-240301_001001-0.zip', 'TD', '2024-02-03 05:12:55.421', '2024-02-03 06:12:55.421', 'trdrif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip', 'TD', '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'trdrif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTSR_ESMAS_R11526-240301_001001-0.zip', 'RL', '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'trdrif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTAR_ESMAS_R99996-240301_001001-0.zip', 'RL078', '2024-02-04 05:12:55.421', '2024-02-04 07:12:55.421', 'trdrif3q0000');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.reports_file_outgoing(outgoingfilename,filetype,reportingsessiontimestamp,creationtimestamp,accountid)
    VALUES('TRRGS_DATTSR_ESMAS_R11526-240301_001001-0.zip', 'TD107', '2024-02-05 05:12:55.421', '2024-02-05 08:12:55.421', 'trdrif3q0000');

INSERT INTO EMIR_REFIT_DEV_ACCOUNT_MNG.regu_identity (tracecode,regulatorid, traceconnectivity) VALUES
                                                                          ('ESMAS','eudri2frb000', 'true'),
                                                                          ('CAFAA','eudri2frb777', 'false'),
                                                                          ('CAFAA','eudri2frb888', 'true'),
                                                                          ('CAFAA','eudri2frb900', 'false'),
                                                                          ('CAESR','eudri96jn000', 'true'),
                                                                          ('CAFAA','eudrira0004', 'false'),
                                                                          ('CAFAA','eudrm2frb000', 'true');
