
-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: metering/metering-app/src/main/liquibase/changelog-main.groovy
-- Ran at: 6/26/18 10:21 AM
-- Against: crss_metering@jdbc:postgresql://localhost:5432/crss?currentSchema=metering
-- Liquibase version: 3.5.3
-- *********************************************************************

-- Lock Database
UPDATE metering.databasechangeloglock SET LOCKED = TRUE, LOCKEDBY = '192.168.242.71 (192.168.242.71)', LOCKGRANTED = '2018-06-26 10:21:09.913' WHERE ID = 1 AND LOCKED = FALSE;

-- Changeset /liquibase/2.6.0/::2.6.0_20180625_01::jdimayuga
ALTER TABLE metering.txn_meter_data_daily ADD ORPHAN BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE metering.txn_meter_data_monthly ADD ORPHAN BOOLEAN DEFAULT FALSE NOT NULL;

INSERT INTO metering.databasechangelog (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('2.6.0_20180625_01', 'jdimayuga', '/liquibase/2.6.0/', NOW(), 15, '7:bdd792cc5e5c32d755769fe71ee438c7', 'addColumn tableName=txn_meter_data_daily; addColumn tableName=txn_meter_data_monthly', '', 'EXECUTED', NULL, NULL, '3.5.3', '9979670300');

-- Release Database Lock
UPDATE metering.databasechangeloglock SET LOCKED = FALSE, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1;