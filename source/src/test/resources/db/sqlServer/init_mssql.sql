DROP ALL OBJECTS;

CREATE SCHEMA IF NOT EXISTS dbo;
SET SCHEMA dbo;
CREATE SEQUENCE  if not exists dbo.item_reporting_seq
    start with 1
    increment by 1;
CREATE SEQUENCE  if not exists dbo.item_reporting_state_seq
    start with 1
    increment by 1;
DROP TABLE IF EXISTS report_eod_state;

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
    comment           VARCHAR(255),
    item_reporting_id int
    constraint item_reporting_state_item_reporting_id_fk
    references item_reporting
    );

INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-1001, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35A_20240215.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-1002, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35A_20240115.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-1003, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35A_20240315.csv', null, '2024-03-01', '2024-03-05', 'sent_request', '2024-03-05');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-2001, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35B_20240215.csv', null, '2024-04-22', '2024-04-23', 'sent_request', '2024-04-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-2002, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35B_20240115.csv', null, '2024-04-22', '2024-04-23', 'sent_request', '2024-04-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-2003, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35B_20240315.csv', null, '2024-03-01', '2024-03-02', 'sent_request', '2024-03-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-3001, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35C_20240215.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-3002, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35C_20240115.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-3003, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35C_20240315.csv', null, '2024-03-22', '2024-03-23', 'sent_request', '2024-03-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-4001, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35D_20240215.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (-4002, 'item35','TRRGS_EMIR_PR_FU_ND_ITEM35D_20240315.csv', null, '2024-03-22', '2024-03-23', 'sent_request', '2024-03-23');


DROP TABLE IF EXISTS report_eod_state;
CREATE TABLE if not exists report_eod_state
(
    report_type       varchar(50) not null,
    reporting_session varchar(50) not null,
    target_type       varchar(50) not null,
    reporting_process varchar(50) not null,
    started_date     datetime
    );

INSERT INTO report_eod_state(report_type, reporting_session, target_type, reporting_process, started_date) VALUES ('TRAR000', '2024-02-02', 'AUTHORITY', 'EOD', '2024-02-02 01:00:00.000')
