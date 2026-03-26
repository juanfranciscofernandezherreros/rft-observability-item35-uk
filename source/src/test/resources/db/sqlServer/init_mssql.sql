DROP ALL OBJECTS;

CREATE SCHEMA IF NOT EXISTS dbo;
SET SCHEMA dbo;
CREATE SEQUENCE if not exists dbo.item_reporting_seq
    start with 1000
    increment by 1;
CREATE SEQUENCE if not exists dbo.item_reporting_state_seq
    start with 1000
    increment by 1;

DROP TABLE IF EXISTS item_reporting_state;
DROP TABLE IF EXISTS item_reporting;

CREATE TABLE IF NOT EXISTS item_reporting(
                                             id int not null primary key,
                                             item_type varchar(50),
    file_name          varchar(255),
    file_url           varchar(255),
    file_creation_date date,
    file_update_date   date,
    state_name         varchar(50),
    state_update_date  date
    );

CREATE TABLE IF NOT EXISTS item_reporting_state
(
    id int not null primary key,
    state_name        VARCHAR(50),
    state_date        datetime,
    comment           VARCHAR(2000),
    item_reporting_id int
    constraint item_reporting_state_item_reporting_id_fk
    references item_reporting
    );

-- item35 records
-- SubmissionVolumes: ITEM35A
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-1001, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35A_20240215.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-1002, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35A_20240115.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-1003, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35A_20240315.csv', null, '2024-03-01', '2024-03-05', 'sent_request', '2024-03-05');
-- ReportGeneration: ITEM35B
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-2001, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20240215.csv', null, '2024-04-22', '2024-04-23', 'sent_request', '2024-04-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-2002, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20240115.csv', null, '2024-04-22', '2024-04-23', 'sent_request', '2024-04-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-2003, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20240315.csv', null, '2024-03-01', '2024-03-02', 'sent_request', '2024-03-23');
-- ReportGeneration: ITEM35B
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-2004, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35C_20240215.csv', null, '2024-04-22', '2024-04-23', 'sent_request', '2024-04-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-2005, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35C_20240115.csv', null, '2024-04-22', '2024-04-23', 'sent_request', '2024-04-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-2006, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35C_20240315.csv', null, '2024-03-01', '2024-03-02', 'sent_request', '2024-03-23');
---
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-4001, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35D_20240215.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-4002, 'item35','TRRGS_UKEMIR_PR_FU_ND_ITEM35D_20240315.csv', null, '2024-03-22', '2024-03-23', 'sent_request', '2024-03-23');
-- EU (item32) records
-- SubmissionVolumes: ITEM32A
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5001, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35A_20240215.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5002, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35A_20240115.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5003, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35A_20240315.csv', null, '2024-03-01', '2024-03-05', 'sent_request', '2024-03-05');
-- ReportGeneration: ITEM32B
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5004, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM32B_20240215.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5005, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM32B_20240115.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5006, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM32B_20240315.csv', null, '2024-03-01', '2024-03-05', 'sent_request', '2024-03-05');
---
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5501, 'item35','TRRGS_EMIR_PR_FU_NDI_ITEM35B_20240215.csv', null, '2024-04-22', '2024-04-23', 'sent_request', '2024-04-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5502, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35B_20240115.csv', null, '2024-04-22', '2024-04-23', 'sent_request', '2024-04-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5503, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35B_20240315.csv', null, '2024-03-01', '2024-03-02', 'sent_request', '2024-03-23');
-- ReportGeneration: ITEM32C
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5007, 'item35','TRAAA_REGU_TY_VS_PD_ITEM32C_20240215.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5008, 'item35','TRAAA_REGU_TY_VS_PD_ITEM32C_20240115.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-5009, 'item35','TRAAA_REGU_TY_VS_PD_ITEM32C_20240315.csv', null, '2024-03-01', '2024-03-05', 'sent_request', '2024-03-05');
-- ComputeCapacity: ITEM32D
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-7001, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35D_20240215.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-7002, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35D_20240315.csv', null, '2024-03-22', '2024-03-23', 'sent_request', '2024-03-23');


DROP TABLE IF EXISTS report_eod_state;
CREATE TABLE if not exists report_eod_state
(
    report_type       varchar(50) not null,
    reporting_session varchar(50) not null,
    target_type       varchar(50) not null,
    reporting_process varchar(50) not null,
    started_date     datetime
    );

INSERT INTO report_eod_state(report_type, reporting_session, target_type, reporting_process, started_date) VALUES ('TRAR000', '2024-02-02', 'AUTHORITY', 'EOD', '2024-02-02 01:00:00.000');
