CREATE SCHEMA IF NOT EXISTS dbo;
SET SCHEMA dbo;

CREATE SEQUENCE IF NOT EXISTS dbo.item_reporting_seq
    START WITH 1000
    INCREMENT BY 1;

CREATE SEQUENCE IF NOT EXISTS dbo.item_reporting_state_seq
    START WITH 1000
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS dbo.item_reporting
(
    id                  INTEGER NOT NULL PRIMARY KEY,
    item_type           VARCHAR(50),
    file_name           VARCHAR(255),
    file_url            VARCHAR(255),
    file_creation_date  DATE,
    file_update_date    DATE,
    state_name          VARCHAR(50),
    state_update_date   DATE
);

CREATE TABLE IF NOT EXISTS dbo.item_reporting_state
(
    id                  INTEGER NOT NULL PRIMARY KEY,
    state_name          VARCHAR(50),
    state_date          DATETIME,
    comment             VARCHAR(2000),
    item_reporting_id   INTEGER,
    CONSTRAINT item_reporting_state_item_reporting_id_fk
        FOREIGN KEY (item_reporting_id) REFERENCES dbo.item_reporting(id)
);

CREATE TABLE IF NOT EXISTS dbo.report_eod_state
(
    report_type         VARCHAR(50) NOT NULL,
    reporting_session   VARCHAR(50) NOT NULL,
    target_type         VARCHAR(50) NOT NULL,
    reporting_process   VARCHAR(50) NOT NULL,
    started_date        DATETIME,
    PRIMARY KEY (report_type, reporting_session, target_type, reporting_process)
);

MERGE INTO dbo.report_eod_state
    (report_type, reporting_session, target_type, reporting_process, started_date)
    KEY(report_type, reporting_session, target_type, reporting_process)
VALUES
    ('TRAR000', '2024-02-02', 'AUTHORITY', 'EOD', '2024-02-02 01:00:00.000');
