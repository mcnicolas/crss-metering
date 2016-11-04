REL  = "2.0.0"
ITER = "ITER-01"
FILE = "20161101"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: true) {
        comment 'Manifest header'

        createTable(tableName: 'txn_mq_manifest_header', remarks: 'Meter data manifest header') {
            column(name: 'header_id',             type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'transaction_id',        type: 'VARCHAR(36)') { constraints(nullable: false, unique: true) }
            column(name: 'msp_id',                type: 'BIGINT', remarks: 'MSP Registration ID')
            column(name: 'file_count',            type: 'INT', remarks: 'The number of files declared to be uploaded')
            column(name: 'category',              type: 'VARCHAR(20)', remarks: 'Possible values: DAILY, MONTHLY, CORRECTED_METER_DATA')
            column(name: 'notification_sent',     type: 'CHAR(1)', remarks: 'Indicates that a notification was sent. Possible values: Y/N')
            column(name: 'notification_datetime', type: 'TIMESTAMP')
            column(name: 'tail_received',         type: 'CHAR(1)', remarks: 'Indicates that the trailer record was received and that the upload was successful')
            column(name: 'upload_by',             type: 'VARCHAR(100)')
            column(name: 'upload_datetime',       type: 'TIMESTAMP')
        }

        createTable(tableName: 'txn_mq_manifest_file', remarks: 'Meter data manifest for each file') {
            column(name: 'file_id',         type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'header_id',       type: 'BIGINT')
            column(name: 'transaction_id',  type: 'VARCHAR(36)')
            column(name: 'filename',        type: 'VARCHAR(50)')
            column(name: 'filetype',        type: 'VARCHAR(5)', remarks: 'Possible values: XLS, MDEF, CSV')
            column(name: 'filesize',        type: 'BIGINT')
            column(name: 'checksum',        type: 'VARCHAR(36)')
            column(name: 'process_flag',    type: 'CHAR(1)')
            column(name: 'status',          type: 'VARCHAR(10)', remarks: 'Possible values: ACCEPTED, REJECTED')
            column(name: 'error_details',   type: 'VARCHAR(250)', remarks: 'Contains any validation errors messages if there are any')
        }

        createTable(tableName: 'txn_meter_data_daily', remarks: 'Meter data table for daily upload') {
            column(name: 'meter_data_id',           type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'file_id',                 type: 'BIGINT')
            column(name: 'sein',                    type: 'VARCHAR(20)') { constraints(nullable: false) }
            column(name: 'interval',                type: 'INT')
            column(name: 'reading_datetime',        type: 'TIMESTAMP')

            // NOTE: Should be able to save variable decimal places. Maximum of 10
            column(name: 'kwd',                     type: 'DOUBLE PRECISION')
            column(name: 'kwd_channel_status',      type: 'INT')
            column(name: 'kwd_interval_status',     type: 'INT')
            column(name: 'kwhd',                    type: 'DOUBLE PRECISION')
            column(name: 'kwhd_channel_status',     type: 'INT')
            column(name: 'kwhd_interval_status',    type: 'INT')
            column(name: 'kvarhd',                  type: 'DOUBLE PRECISION')
            column(name: 'kvarhd_channel_status',   type: 'INT')
            column(name: 'kvarhd_interval_status',  type: 'INT')
            column(name: 'kwr',                     type: 'DOUBLE PRECISION')
            column(name: 'kwr_channel_status',      type: 'INT')
            column(name: 'kwr_interval_status',     type: 'INT')
            column(name: 'kwhr',                    type: 'DOUBLE PRECISION')
            column(name: 'kwhr_channel_status',     type: 'INT')
            column(name: 'kwhr_interval_status',    type: 'INT')
            column(name: 'kvarhr',                  type: 'DOUBLE PRECISION')
            column(name: 'kvarhr_channel_status',   type: 'INT')
            column(name: 'kvarhr_interval_status',  type: 'INT')
            column(name: 'van',                     type: 'DOUBLE PRECISION')
            column(name: 'van_channel_status',      type: 'INT')
            column(name: 'van_interval_status',     type: 'INT')
            column(name: 'vbn',                     type: 'DOUBLE PRECISION')
            column(name: 'vbn_channel_status',      type: 'INT')
            column(name: 'vbn_interval_status',     type: 'INT')
            column(name: 'vcn',                     type: 'DOUBLE PRECISION')
            column(name: 'vcn_channel_status',      type: 'INT')
            column(name: 'vcn_interval_status',     type: 'INT')
            column(name: 'ian',                     type: 'DOUBLE PRECISION')
            column(name: 'ian_channel_status',      type: 'INT')
            column(name: 'ian_interval_status',     type: 'INT')
            column(name: 'ibn',                     type: 'DOUBLE PRECISION')
            column(name: 'ibn_channel_status',      type: 'INT')
            column(name: 'ibn_interval_status',     type: 'INT')
            column(name: 'icn',                     type: 'DOUBLE PRECISION')
            column(name: 'icn_channel_status',      type: 'INT')
            column(name: 'icn_interval_status',     type: 'INT')
            column(name: 'pf',                      type: 'DOUBLE PRECISION')
            column(name: 'pf_channel_status',       type: 'INT')
            column(name: 'pf_interval_status',      type: 'INT')
            column(name: 'estimation_flag',         type: 'VARCHAR(15)')
            column(name: 'version',                 type: 'INT')
        }

        createTable(tableName: 'txn_meter_data_monthly', remarks: 'Meter data table for monthly upload') {
            column(name: 'meter_data_id',           type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'file_id',                 type: 'BIGINT')
            column(name: 'sein',                    type: 'VARCHAR(20)') { constraints(nullable: false) }
            column(name: 'interval',                type: 'INT')
            column(name: 'reading_datetime',        type: 'TIMESTAMP')

            // NOTE: Should be able to save variable decimal places. Maximum of 10
            column(name: 'kwd',                     type: 'DOUBLE PRECISION')
            column(name: 'kwd_channel_status',      type: 'INT')
            column(name: 'kwd_interval_status',     type: 'INT')
            column(name: 'kwhd',                    type: 'DOUBLE PRECISION')
            column(name: 'kwhd_channel_status',     type: 'INT')
            column(name: 'kwhd_interval_status',    type: 'INT')
            column(name: 'kvarhd',                  type: 'DOUBLE PRECISION')
            column(name: 'kvarhd_channel_status',   type: 'INT')
            column(name: 'kvarhd_interval_status',  type: 'INT')
            column(name: 'kwr',                     type: 'DOUBLE PRECISION')
            column(name: 'kwr_channel_status',      type: 'INT')
            column(name: 'kwr_interval_status',     type: 'INT')
            column(name: 'kwhr',                    type: 'DOUBLE PRECISION')
            column(name: 'kwhr_channel_status',     type: 'INT')
            column(name: 'kwhr_interval_status',    type: 'INT')
            column(name: 'kvarhr',                  type: 'DOUBLE PRECISION')
            column(name: 'kvarhr_channel_status',   type: 'INT')
            column(name: 'kvarhr_interval_status',  type: 'INT')
            column(name: 'van',                     type: 'DOUBLE PRECISION')
            column(name: 'van_channel_status',      type: 'INT')
            column(name: 'van_interval_status',     type: 'INT')
            column(name: 'vbn',                     type: 'DOUBLE PRECISION')
            column(name: 'vbn_channel_status',      type: 'INT')
            column(name: 'vbn_interval_status',     type: 'INT')
            column(name: 'vcn',                     type: 'DOUBLE PRECISION')
            column(name: 'vcn_channel_status',      type: 'INT')
            column(name: 'vcn_interval_status',     type: 'INT')
            column(name: 'ian',                     type: 'DOUBLE PRECISION')
            column(name: 'ian_channel_status',      type: 'INT')
            column(name: 'ian_interval_status',     type: 'INT')
            column(name: 'ibn',                     type: 'DOUBLE PRECISION')
            column(name: 'ibn_channel_status',      type: 'INT')
            column(name: 'ibn_interval_status',     type: 'INT')
            column(name: 'icn',                     type: 'DOUBLE PRECISION')
            column(name: 'icn_channel_status',      type: 'INT')
            column(name: 'icn_interval_status',     type: 'INT')
            column(name: 'pf',                      type: 'DOUBLE PRECISION')
            column(name: 'pf_channel_status',       type: 'INT')
            column(name: 'pf_interval_status',      type: 'INT')
            column(name: 'estimation_flag',         type: 'VARCHAR(15)')
            column(name: 'version',                 type: 'INT')
        }

        addForeignKeyConstraint(
                baseColumnNames: 'header_id', baseTableName: 'txn_mq_manifest_file', constraintName: 'FK_METER_MANIFEST_FILE',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'header_id', referencedTableName: 'txn_mq_manifest_header')

        addForeignKeyConstraint(
                baseColumnNames: 'file_id', baseTableName: 'txn_meter_data_daily', constraintName: 'FK_METER_DAILY',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'file_id', referencedTableName: 'txn_mq_manifest_file')

        addForeignKeyConstraint(
                baseColumnNames: 'file_id', baseTableName: 'txn_meter_data_monthly', constraintName: 'FK_METER_MONTHLY',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'file_id', referencedTableName: 'txn_mq_manifest_file')
    }

}
