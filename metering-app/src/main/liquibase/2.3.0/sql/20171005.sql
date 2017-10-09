-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: metering/metering-app/src/main/liquibase/changelog-main.groovy
-- Ran at: 10/9/17 9:29 AM
-- Against: crss_metering@jdbc:postgresql://localhost:5432/crss?currentSchema=metering
-- Liquibase version: 3.4.2
-- *********************************************************************

-- Lock Database
UPDATE metering.databasechangeloglock SET LOCKED = TRUE, LOCKEDBY = '172.23.0.1 (172.23.0.1)', LOCKGRANTED = '2017-10-09 09:29:56.841' WHERE ID = 1 AND LOCKED = FALSE;

-- Changeset /liquibase/2.3.0/::2.3.0_20171005_01::rlevida
ALTER TABLE metering.TXN_BCQ_PROHIBITED ADD EFFECTIVE_START_DATE TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE metering.TXN_BCQ_PROHIBITED ADD EFFECTIVE_END_DATE TIMESTAMP WITHOUT TIME ZONE;

INSERT INTO metering.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE) VALUES ('2.3.0_20171005_01', 'rlevida', '/liquibase/2.3.0/', NOW(), 12, '7:b2f6753a446f167bcc5bdb94a40d874c', 'addColumn', '', 'EXECUTED', NULL, NULL, '3.4.2');

-- Release Database Lock
UPDATE metering.databasechangeloglock SET LOCKED = FALSE, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1;

