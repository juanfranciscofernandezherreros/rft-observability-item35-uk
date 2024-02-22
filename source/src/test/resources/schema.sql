CREATE SCHEMA  IF NOT EXISTS `dbo`;
SET SCHEMA `dbo`;

CREATE SEQUENCE IF NOT EXISTS item_reporting_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS item_reporting( id int not null constraint item_reporting_pk primary key, item_type varchar(50),
    file_name          varchar(255),
    file_url           varchar(255),
    file_creation_date date,
    file_update_date   date,
    state_name         varchar(50),
    state_update_date  date
);

CREATE SCHEMA  IF NOT EXISTS `emir_refit_int_control_refit`;
SET SCHEMA `emir_refit_int_control_refit`;

CREATE TABLE IF NOT EXISTS record_status (
   id int not null constraint record_status_pk primary key,
    receiveddt VARCHAR(255),
    status VARCHAR(255),
    channel VARCHAR(255)
);
