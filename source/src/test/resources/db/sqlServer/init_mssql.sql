CREATE SCHEMA IF NOT EXISTS dbo;
SET SCHEMA dbo;
CREATE SEQUENCE  if not exists dbo.item_reporting_seq
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

