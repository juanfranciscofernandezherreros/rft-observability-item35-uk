CREATE SCHEMA IF NOT EXISTS EMIR_REFIT_DEV_CONTROL_REFIT;
SET SCHEMA EMIR_REFIT_DEV_CONTROL_REFIT;

create table IF NOT EXISTS EMIR_REFIT_DEV_CONTROL_REFIT.record_status
(
    receiveddt            VARCHAR(255),
    status                VARCHAR(255),
    channel               VARCHAR(255)
);


INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(receiveddt,status,channel)VALUES('2024-02-18', 'ACPT', 'sftp');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(receiveddt,status,channel)VALUES('2023-02-31', 'ACPT', 'web');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(receiveddt,status,channel)VALUES('2024-02-02', 'RJCT', 'api');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(receiveddt,status,channel)VALUES('2024-02-01', 'ACPT', 'api');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(receiveddt,status,channel)VALUES('2024-02-19', 'RJCT', 'api');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(receiveddt,status,channel)VALUES('2024-03-18', 'RJCT', 'sftp');
INSERT INTO EMIR_REFIT_DEV_CONTROL_REFIT.record_status(receiveddt,status,channel)VALUES('2024-02-11', 'ACPT', 'web');
