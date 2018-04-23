-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: metering/metering-app/src/main/liquibase/changelog-main.groovy
-- Ran at: 4/3/18 1:40 PM
-- Against: crss_metering@jdbc:postgresql://localhost:5432/crss?currentSchema=metering
-- Liquibase version: 3.5.3
-- *********************************************************************

-- Lock Database
UPDATE metering.databasechangeloglock SET LOCKED = TRUE, LOCKEDBY = '172.23.0.1 (172.23.0.1)', LOCKGRANTED = '2018-04-03 13:40:54.606' WHERE ID = 1 AND LOCKED = FALSE;

-- Changeset /liquibase/2.5.0/::2.5.0_20180403_01::rlevida
ALTER TABLE metering.txn_bcq_header ADD uploaded_by VARCHAR(255);

INSERT INTO metering.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('2.5.0_20180403_01', 'rlevida', '/liquibase/2.5.0/', NOW(), 15, '7:42a3e3d38cd7e3ec76f68e7a70ed0049', 'addColumn tableName=txn_bcq_header', '', 'EXECUTED', NULL, NULL, '3.5.3', '2734055858');

-- Release Database Lock
UPDATE metering.databasechangeloglock SET LOCKED = FALSE, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1;

