CREATE SCHEMA IF NOT EXISTS EMIR_REFIT_DEV_RECO;
SET SCHEMA EMIR_REFIT_DEV_RECO;

create table IF NOT EXISTS EMIR_REFIT_DEV_RECO.record_status
(
    receiveddt            VARCHAR(255),
    status                VARCHAR(255),
    channel               VARCHAR(255)
);


INSERT INTO EMIR_REFIT_DEV_RECO.record_status(receiveddt,status,channel)VALUES('2024-01-18', 'ACPT', 'sftp');
INSERT INTO EMIR_REFIT_DEV_RECO.record_status(receiveddt,status,channel)VALUES('2023-01-31', 'ACPT', 'web');
INSERT INTO EMIR_REFIT_DEV_RECO.record_status(receiveddt,status,channel)VALUES('2024-01-02', 'RJCT', 'api');
INSERT INTO EMIR_REFIT_DEV_RECO.record_status(receiveddt,status,channel)VALUES('2024-01-01', 'ACPT', 'api');
INSERT INTO EMIR_REFIT_DEV_RECO.record_status(receiveddt,status,channel)VALUES('2024-01-19', 'RJCT', 'api');
INSERT INTO EMIR_REFIT_DEV_RECO.record_status(receiveddt,status,channel)VALUES('2024-03-18', 'RJCT', 'sftp');
INSERT INTO EMIR_REFIT_DEV_RECO.record_status(receiveddt,status,channel)VALUES('2024-01-11', 'ACPT', 'web');
