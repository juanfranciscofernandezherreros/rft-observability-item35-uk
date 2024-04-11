CREATE SCHEMA IF NOT EXISTS dbo;
SET SCHEMA dbo;
CREATE SEQUENCE  if not exists dbo.item_reporting_seq
    start with 1
    increment by 1;
CREATE SEQUENCE  if not exists dbo.item_reporting_state_seq
    start with 1
    increment by 1;

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

INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (1, 'item35','TRRGS_EMIR_PR_IN_ND_ITEM35A_20240229.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (2, 'item35','TRRGS_EMIR_PR_IN_ND_ITEM35A_20240129.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (3, 'item35','TRRGS_EMIR_PR_IN_ND_ITEM35D_20240129.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (4, 'item35','TRRGS_EMIR_PR_IN_ND_ITEM35C_20240229.csv', null, '2024-02-22', '2024-02-23', 'sent_request', '2024-02-23');
INSERT INTO dbo.item_reporting (id, item_type, file_name, file_url, file_creation_date, file_update_date, state_name, state_update_date) VALUES (5, 'item35','TRRGS_EMIR_PR_IN_ND_ITEM35B_20240229.csv', null, '2024-04-22', '2024-04-23', 'sent_request', '2024-04-23');

