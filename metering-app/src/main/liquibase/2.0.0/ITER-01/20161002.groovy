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

        createTable(tableName: 'txn_meter_upload_header', remarks: 'Create metering metadata header table') {
            column(name: 'transaction_id',        type: 'BIGINT') { constraints(primaryKey: true, nullable: false)}
            column(name: 'msp_id',                type: 'BIGINT', remarks: 'MSP Registration ID')
            column(name: 'category',              type: 'VARCHAR(10)', remarks: 'Possible values: Daily, Monthly')
            column(name: 'notification_sent',     type: 'CHAR(1)', remarks: 'Possible values: Y/N')
            column(name: 'notification_datetime', type: 'TIMESTAMP')
            column(name: 'upload_by',             type: 'VARCHAR(100)')
            column(name: 'upload_datetime',       type: 'TIMESTAMP')
            column(name: 'version',               type: 'BIGINT')
        }

        createTable(tableName: 'txn_meter_upload_file', remarks: 'Create metering data details table') {
            column(name: 'file_id',         type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'transaction_id',  type: 'BIGINT')
            column(name: 'filename',        type: 'VARCHAR(50)')
            column(name: 'filetype',        type: 'VARCHAR(5)', remarks: 'Possible values: XLS, MDEF')
            column(name: 'filesize',        type: 'BIGINT')
            column(name: 'checksum',        type: 'VARCHAR(50)', remarks: 'MD5 checksum of the uploaded file')
            column(name: 'status',          type: 'VARCHAR(10)', remarks: 'Possible values: ACCEPTED, REJECTED')
        }

        addForeignKeyConstraint(
                baseColumnNames: 'transaction_id', baseTableName: 'txn_meter_upload_file', constraintName: 'FK_METER_FILE',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'transaction_id', referencedTableName: 'txn_meter_upload_header')
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_03", author: 'clim', failOnError: true) {
        comment 'Initial MDEF tables'

        createTable(tableName: 'txn_meter_header', remarks: 'Create meter header table') {
            column(name: 'meter_header_id',     type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'file_id',             type: 'BIGINT')
            column(name: 'customer_id',         type: 'VARCHAR(20)')
            column(name: 'customer_name',       type: 'VARCHAR(20)')
            column(name: 'customer_addr1',      type: 'VARCHAR(20)')
            column(name: 'customer_addr2',      type: 'VARCHAR(20)')
            column(name: 'account_no',          type: 'VARCHAR(20)')
            column(name: 'total_channels',      type: 'BIGINT')
            column(name: 'start_datetime',      type: 'TIMESTAMP')
            column(name: 'end_datetime',        type: 'TIMESTAMP')
            column(name: 'dst_flag',            type: 'CHAR(1)')
        }

        createTable(tableName: 'txn_channel_header', remarks: 'Create channel header table') {
            column(name: 'channel_header_id',   type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'meter_header_id',     type: 'BIGINT')
            column(name: 'record_id',           type: 'VARCHAR(20)')
            column(name: 'meter_no',            type: 'VARCHAR(12)')
            column(name: 'start_datetime',      type: 'VARCHAR(12)')
            column(name: 'stop_datetime',       type: 'VARCHAR(12)')
            column(name: 'meter_channel_no',    type: 'CHAR(2)')
            column(name: 'customer_channel_no', type: 'CHAR(2)')
            column(name: 'uom_code',            type: 'VARCHAR(5)')
            column(name: 'channel_status',      type: 'CHAR(1)')
            column(name: 'interval_status',     type: 'CHAR(1)')
            column(name: 'start_meter',         type: 'VARCHAR(12)')
            column(name: 'stop_meter',          type: 'VARCHAR(12)')
            column(name: 'meter_multiplier',    type: 'VARCHAR(10)')
            column(name: 'server_type',         type: 'CHAR(1)')
            column(name: 'interval_per_hour',   type: 'BIGINT')
            column(name: 'validation_result',   type: 'VARCHAR(2)')
            column(name: 'flow_direction',      type: 'CHAR(1)')
            column(name: 'kva_set',             type: 'BIGINT')
            column(name: 'origin',              type: 'CHAR(1)')
        }

        createTable(tableName: 'txn_interval', remarks: 'Create meter interval table') {
            column(name: 'interval_id',       type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'channel_header_id', type: 'BIGINT')
            column(name: 'meter_reading',     type: 'DOUBLE PRECISION')
            column(name: 'channel_status',    type: 'CHAR(16)')
            column(name: 'interval_status',   type: 'CHAR(16)')
            column(name: 'reading_datetime',  type: 'VARCHAR(12)')
        }

        addForeignKeyConstraint(
                baseColumnNames: 'file_id', baseTableName: 'txn_meter_header', constraintName: 'FK_METER_HEADER',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'file_id', referencedTableName: 'txn_meter_upload_file')

        addForeignKeyConstraint(
                baseColumnNames: 'meter_header_id', baseTableName: 'txn_channel_header', constraintName: 'FK_METER_CHANNEL',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'meter_header_id', referencedTableName: 'txn_meter_header')

        addForeignKeyConstraint(
                baseColumnNames: 'channel_header_id', baseTableName: 'txn_interval', constraintName: 'FK_CHANNEL_INTERVAL',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'channel_header_id', referencedTableName: 'txn_channel_header')
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_04", author: 'clim', failOnError: true) {
        comment 'Initial XLS tables'

        createTable(tableName: 'txn_meter_data_xls', remarks: 'Create XLS meter data table') {
            column(name: 'meter_data_id',    type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'file_id',          type: 'BIGINT')
            column(name: 'sein',             type: 'VARCHAR(20)')
            column(name: 'reading_datetime', type: 'TIMESTAMP')
            column(name: 'kwd',              type: 'DOUBLE')
            column(name: 'kwhd',             type: 'DOUBLE')
            column(name: 'kvarhd',           type: 'DOUBLE')
            column(name: 'kwr',              type: 'DOUBLE')
            column(name: 'kwhr',             type: 'DOUBLE')
            column(name: 'kvarhr',           type: 'DOUBLE')
        }
    }

}
