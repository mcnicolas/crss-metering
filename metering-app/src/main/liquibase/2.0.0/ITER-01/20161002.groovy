REL  = "2.0.0"
ITER = "ITER-01"
FILE = "20161002"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: 'true') {
        comment 'Create hibernate sequence'
        createSequence(sequenceName: 'hibernate_sequence')
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_02", author: 'clim', failOnError: true) {
        comment 'Initial metering metadata tables'

        createTable(tableName: 'txn_meter_metadata_header', remarks: 'Create metering metadata header table') {
            column(name: 'transaction_id',        type: 'BIGINT') { constraints(primaryKey: true, nullable: false)}
            column(name: 'msp_id',                type: 'BIGINT')
            column(name: 'upload_datetime',       type: 'TIMESTAMP')
            column(name: 'notification_sent',     type: 'CHAR(1)')
            column(name: 'notification_datetime', type: 'TIMESTAMP')
        }

        createTable(tableName: 'txn_meter_metadata_detail', remarks: 'Create metering data details table') {
            column(name: 'file_id',         type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'transaction_id',  type: 'BIGINT')
            column(name: 'filename',        type: 'VARCHAR(50)')
            column(name: 'filesize',        type: 'BIGINT')
            column(name: 'checksum',        type: 'VARCHAR(50)')
            column(name: 'status',          type: 'VARCHAR(10)')
        }

        addForeignKeyConstraint(
                baseColumnNames: 'transaction_id', baseTableName: 'txn_meter_metadata_header', constraintName: 'fk_meter_transaction',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'transaction_id', referencedTableName: 'txn_meter_metadata_header')
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_03", author: 'clim', failOnError: true) {
        comment 'Initial MDEF tables'

        createTable(tableName: 'txn_meter_header', remarks: 'Create meter header table') {
            column(name: 'customer_id',         type: 'VARCHAR(20)')
            column(name: 'customer_name',       type: 'VARCHAR(20)')
            column(name: 'customer_addr1',      type: 'VARCHAR(20)')
            column(name: 'customer_addr2',      type: 'VARCHAR(20)')
            column(name: 'account_no',          type: 'VARCHAR(20)')
            column(name: 'total_channels',      type: 'BIGINT')
            column(name: 'start_datetime',      type: 'VARCHAR(12)')
            column(name: 'end_datetime',        type: 'VARCHAR(12)')
            column(name: 'dst_flag',            type: 'CHAR(1)')
        }

        createTable(tableName: 'txn_channel_header', remarks: 'Create channel header table') {
            column(name: 'customer_id',         type: 'VARCHAR(20)')
            column(name: 'record_id',           type: 'VARCHAR(20)')
            column(name: 'meter_no',            type: 'VARCHAR(12)')
            column(name: 'start_datetime',      type: 'VARCHAR(12)')
            column(name: 'end_datetime',        type: 'VARCHAR(12)')
            column(name: 'meter_channel_no',    type: 'CHAR(2)')
            column(name: 'customer_channel_no', type: 'CHAR(2)')
            column(name: 'uom',                 type: 'VARCHAR(5)')
            column(name: 'channel_status',      type: 'CHAR(1)')
            column(name: 'interval_status',     type: 'CHAR(1)')
            column(name: 'start_meter',         type: 'VARCHAR(12)')
            column(name: 'end_meter',           type: 'VARCHAR(12)')
            column(name: 'dial_multiplier',     type: 'VARCHAR(10)')
            column(name: 'server_type',         type: 'CHAR(1)')
            column(name: 'interval',            type: 'BIGINT')
            column(name: 'status',              type: 'VARCHAR(2)')
            column(name: 'flow_direction',      type: 'CHAR(1)')
            column(name: 'kva_set',             type: 'CHAR(2)')
            column(name: 'origin',              type: 'CHAR(1)')
        }

        createTable(tableName: 'txn_interval', remarks: 'Create meter interval table') {
            column(name: 'customer_id',      type: 'VARCHAR(20)')
            column(name: 'meter_reading',    type: 'DOUBLE PRECISION')
            column(name: 'channel_status',   type: 'CHAR(16)')
            column(name: 'interval_status',  type: 'CHAR(16)')
            column(name: 'reading_datetime', type: 'TIMESTAMP')
        }
    }

}
