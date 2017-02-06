REL  = '2.0.0'
ITER = 'ITER-14'
FILE = '20170206'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: true) {
        comment 'Meter Quantity Tables'

        createTable(tableName: 'TXN_MQ_MANIFEST_HEADER', remarks: 'Meter data manifest header') {
            column(name: 'HEADER_ID',             type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'TRANSACTION_ID',        type: 'VARCHAR(36)') {
                constraints(nullable: false, unique: true)
            }
            column(name: 'FILE_COUNT',            type: 'INT', remarks: 'The number of files declared to be uploaded') {
                constraints(nullable: false)
            }
            column(name: 'CATEGORY',              type: 'VARCHAR(20)',
                    remarks: 'Possible values: DAILY, MONTHLY, CORRECTED_DAILY, CORRECTED_MONTHLY') {
                constraints(nullable: false)
            }
            column(name: 'NOTIFICATION_SENT',     type: 'CHAR(1)',
                    defaultValue: 'N',
                    remarks: 'Indicates that a notification was sent. Possible values: Y/N') {
                constraints(nullable: false)
            }
            column(name: 'NOTIFICATION_DATETIME', type: 'TIMESTAMP')
            column(name: 'TAIL_RECEIVED',         type: 'CHAR(1)',
                    defaultValue: 'N',
                    remarks: 'Indicates that the trailer record was received and that the upload was successful') {
                constraints(nullable: false)
            }
            column(name: 'UPLOAD_BY',             type: 'VARCHAR(100)') {
                constraints(nullable: false)
            }
            column(name: 'UPLOAD_DATETIME',       type: 'TIMESTAMP') {
                constraints(nullable: false)
            }
        }

        createTable(tableName: 'TXN_MQ_MANIFEST_FILE', remarks: 'Meter data manifest for each file') {
            column(name: 'FILE_ID',          type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'HEADER_ID',        type: 'BIGINT')
            column(name: 'TRANSACTION_ID',   type: 'VARCHAR(36)')
            column(name: 'FILENAME',         type: 'VARCHAR(100)')
            column(name: 'FILETYPE',         type: 'VARCHAR(5)', remarks: 'Possible values: XLS, MDEF, CSV')
            column(name: 'FILESIZE',         type: 'BIGINT')
            column(name: 'CHECKSUM',         type: 'VARCHAR(36)')
            column(name: 'UPLOAD_DATETIME',  type: 'TIMESTAMP')
            column(name: 'PROCESS_DATETIME', type: 'TIMESTAMP')
            column(name: 'PROCESS_FLAG',     type: 'CHAR(1)', remarks: 'Indicates that the file was parsed')
            column(name: 'STATUS',           type: 'VARCHAR(10)', remarks: 'Possible values: ACCEPTED, REJECTED')
            column(name: 'ERROR_DETAILS',    type: 'VARCHAR(500)', remarks: 'Contains any validation errors messages if there are any')
        }

        createTable(tableName: 'TXN_METER_DATA_DAILY', remarks: 'Meter data table for daily upload') {
            column(name: 'METER_DATA_ID',           type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'FILE_ID',                 type: 'BIGINT')
            column(name: 'MSP_SHORTNAME',           type: 'VARCHAR(50)')
            column(name: 'CATEGORY',                type: 'VARCHAR(20)')
            column(name: 'SEIN',                    type: 'VARCHAR(20)') { constraints(nullable: false) }
            column(name: 'INTERVAL',                type: 'INT', remarks: 'Minute interval in an hour')
            column(name: 'READING_DATETIME',        type: 'BIGINT')
            column(name: 'KWD',                     type: 'NUMBER(25,17)')
            column(name: 'KWD_CHANNEL_STATUS',      type: 'INT')
            column(name: 'KWD_INTERVAL_STATUS',     type: 'INT')
            column(name: 'KWHD',                    type: 'NUMBER(25,17)')
            column(name: 'KWHD_CHANNEL_STATUS',     type: 'INT')
            column(name: 'KWHD_INTERVAL_STATUS',    type: 'INT')
            column(name: 'KVARHD',                  type: 'NUMBER(25,17)')
            column(name: 'KVARHD_CHANNEL_STATUS',   type: 'INT')
            column(name: 'KVARHD_INTERVAL_STATUS',  type: 'INT')
            column(name: 'KWR',                     type: 'NUMBER(25,17)')
            column(name: 'KWR_CHANNEL_STATUS',      type: 'INT')
            column(name: 'KWR_INTERVAL_STATUS',     type: 'INT')
            column(name: 'KWHR',                    type: 'NUMBER(25,17)')
            column(name: 'KWHR_CHANNEL_STATUS',     type: 'INT')
            column(name: 'KWHR_INTERVAL_STATUS',    type: 'INT')
            column(name: 'KVARHR',                  type: 'NUMBER(25,17)')
            column(name: 'KVARHR_CHANNEL_STATUS',   type: 'INT')
            column(name: 'KVARHR_INTERVAL_STATUS',  type: 'INT')
            column(name: 'VAN',                     type: 'NUMBER(25,17)')
            column(name: 'VAN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'VAN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'VBN',                     type: 'NUMBER(25,17)')
            column(name: 'VBN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'VBN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'VCN',                     type: 'NUMBER(25,17)')
            column(name: 'VCN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'VCN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'IAN',                     type: 'NUMBER(25,17)')
            column(name: 'IAN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'IAN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'IBN',                     type: 'NUMBER(25,17)')
            column(name: 'IBN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'IBN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'ICN',                     type: 'NUMBER(25,17)')
            column(name: 'ICN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'ICN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'PF',                      type: 'NUMBER(25,17)')
            column(name: 'PF_CHANNEL_STATUS',       type: 'INT')
            column(name: 'PF_INTERVAL_STATUS',      type: 'INT')
            column(name: 'ESTIMATION_FLAG',         type: 'VARCHAR(15)')
            column(name: 'CREATED_DATE_TIME',       type: 'TIMESTAMP')
            column(name: 'CONSUMED',                type: 'BOOLEAN', defaultValue: 'FALSE') {
                constraints(nullable: false)
            }
        }

        createTable(tableName: 'TXN_METER_DATA_MONTHLY', remarks: 'Meter data table for monthly upload') {
            column(name: 'METER_DATA_ID',           type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'FILE_ID',                 type: 'BIGINT')
            column(name: 'MSP_SHORTNAME',           type: 'VARCHAR(50)')
            column(name: 'CATEGORY',                type: 'VARCHAR(20)')
            column(name: 'SEIN',                    type: 'VARCHAR(20)') { constraints(nullable: false) }
            column(name: 'INTERVAL',                type: 'INT')
            column(name: 'READING_DATETIME',        type: 'BIGINT')
            column(name: 'KWD',                     type: 'NUMBER(25,17)')
            column(name: 'KWD_CHANNEL_STATUS',      type: 'INT')
            column(name: 'KWD_INTERVAL_STATUS',     type: 'INT')
            column(name: 'KWHD',                    type: 'NUMBER(25,17)')
            column(name: 'KWHD_CHANNEL_STATUS',     type: 'INT')
            column(name: 'KWHD_INTERVAL_STATUS',    type: 'INT')
            column(name: 'KVARHD',                  type: 'NUMBER(25,17)')
            column(name: 'KVARHD_CHANNEL_STATUS',   type: 'INT')
            column(name: 'KVARHD_INTERVAL_STATUS',  type: 'INT')
            column(name: 'KWR',                     type: 'NUMBER(25,17)')
            column(name: 'KWR_CHANNEL_STATUS',      type: 'INT')
            column(name: 'KWR_INTERVAL_STATUS',     type: 'INT')
            column(name: 'KWHR',                    type: 'NUMBER(25,17)')
            column(name: 'KWHR_CHANNEL_STATUS',     type: 'INT')
            column(name: 'KWHR_INTERVAL_STATUS',    type: 'INT')
            column(name: 'KVARHR',                  type: 'NUMBER(25,17)')
            column(name: 'KVARHR_CHANNEL_STATUS',   type: 'INT')
            column(name: 'KVARHR_INTERVAL_STATUS',  type: 'INT')
            column(name: 'VAN',                     type: 'NUMBER(25,17)')
            column(name: 'VAN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'VAN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'VBN',                     type: 'NUMBER(25,17)')
            column(name: 'VBN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'VBN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'VCN',                     type: 'NUMBER(25,17)')
            column(name: 'VCN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'VCN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'IAN',                     type: 'NUMBER(25,17)')
            column(name: 'IAN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'IAN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'IBN',                     type: 'NUMBER(25,17)')
            column(name: 'IBN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'IBN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'ICN',                     type: 'NUMBER(25,17)')
            column(name: 'ICN_CHANNEL_STATUS',      type: 'INT')
            column(name: 'ICN_INTERVAL_STATUS',     type: 'INT')
            column(name: 'PF',                      type: 'NUMBER(25,17)')
            column(name: 'PF_CHANNEL_STATUS',       type: 'INT')
            column(name: 'PF_INTERVAL_STATUS',      type: 'INT')
            column(name: 'ESTIMATION_FLAG',         type: 'VARCHAR(15)')
            column(name: 'CREATED_DATE_TIME',       type: 'TIMESTAMP')
            column(name: 'CONSUMED',                type: 'BOOLEAN', defaultValue: 'FALSE') {
                constraints(nullable: false)
            }
        }

        addForeignKeyConstraint(baseColumnNames: 'HEADER_ID', baseTableName: 'TXN_MQ_MANIFEST_FILE', constraintName: 'FK_METER_MANIFEST_FILE',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'HEADER_ID', referencedTableName: 'TXN_MQ_MANIFEST_HEADER')

        addForeignKeyConstraint(baseColumnNames: 'FILE_ID', baseTableName: 'TXN_METER_DATA_DAILY', constraintName: 'FK_METER_DAILY',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'FILE_ID', referencedTableName: 'TXN_MQ_MANIFEST_FILE')

        addForeignKeyConstraint(baseColumnNames: 'FILE_ID', baseTableName: 'TXN_METER_DATA_MONTHLY', constraintName: 'FK_METER_MONTHLY',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'FILE_ID', referencedTableName: 'TXN_MQ_MANIFEST_FILE')

        addUniqueConstraint(tableName: 'TXN_METER_DATA_DAILY',
                columnNames: 'SEIN, READING_DATETIME, CREATED_DATE_TIME',
                constraintName: 'METER_DATA_DAILY_UNIQUE')

        addUniqueConstraint(tableName: 'TXN_METER_DATA_MONTHLY',
                columnNames: 'SEIN, READING_DATETIME, CREATED_DATE_TIME',
                constraintName: 'METER_DATA_MONTHLY_UNIQUE')
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_02", author: 'jlapugot', failOnError: true) {
        comment 'BCQ Tables'

        createTable(tableName: 'TXN_BCQ_UPLOAD_FILE', remarks: 'BCQ upload file') {
            column(name: 'FILE_ID',             type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'TRANSACTION_ID',      type: 'VARCHAR(36)') { constraints(nullable: false) }
            column(name: 'FILE_NAME',           type: 'VARCHAR(255)')
            column(name: 'FILE_SIZE',           type: 'BIGINT')
            column(name: 'SUBMITTED_DATE',      type: 'TIMESTAMP')
            column(name: 'VALIDATION_STATUS',   type: 'VARCHAR(8)')
        }

        createTable(tableName: 'TXN_BCQ_HEADER', remarks: 'BCQ header') {
            column(name: 'BCQ_HEADER_ID',                   type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'BILLING_ID',                      type: 'VARCHAR(50)')
            column(name: 'FILE_ID',                         type: 'BIGINT')
            column(name: 'SELLING_MTN',                     type: 'VARCHAR(255)')
            column(name: 'BUYING_PARTICIPANT_NAME',         type: 'VARCHAR(255)')
            column(name: 'BUYING_PARTICIPANT_SHORT_NAME',   type: 'VARCHAR(255)')
            column(name: 'BUYING_PARTICIPANT_USER_ID',      type: 'INT')
            column(name: 'SELLING_PARTICIPANT_NAME',        type: 'VARCHAR(255)')
            column(name: 'SELLING_PARTICIPANT_SHORT_NAME',  type: 'VARCHAR(255)')
            column(name: 'SELLING_PARTICIPANT_USER_ID',     type: 'INT')
            column(name: 'STATUS',                          type: 'VARCHAR(30)')
            column(name: 'TRADING_DATE',                    type: 'TIMESTAMP')
            column(name: 'DEADLINE_DATE',                   type: 'TIMESTAMP')
            column(name: 'UPDATED_VIA',                     type: 'VARCHAR(50)')
        }

        createTable(tableName: 'TXN_BCQ_DATA', remarks: 'BCQ data') {
            column(name: 'BCQ_DATA_ID',    type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'BCQ_HEADER_ID',  type: 'BIGINT')
            column(name: 'REFERENCE_MTN',  type: 'VARCHAR(255)') { constraints(nullable: false) }
            column(name: 'START_TIME',     type: 'TIMESTAMP') { constraints(nullable: false) }
            column(name: 'END_TIME',       type: 'TIMESTAMP') { constraints(nullable: false) }
            column(name: 'BCQ',            type: 'NUMERIC(28,9)') { constraints(nullable: false) }
        }

        createTable(tableName: 'TXN_BCQ_SPECIAL_EVENT') {
            column(name: 'EVENT_ID',      type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'DEADLINE_DATE', type: 'TIMESTAMP')
            column(name: 'REMARKS',       type: 'VARCHAR(500)')
            column(name: 'CREATED_DATE',  type: 'TIMESTAMP')
        }

        createTable(tableName: 'TXN_BCQ_EVENT_TRADING_DATE') {
            column(name: 'EVENT_ID',     type: 'BIGINT')
            column(name: 'TRADING_DATE', type: 'TIMESTAMP')
        }

        createTable(tableName: 'TXN_BCQ_EVENT_PARTICIPANT') {
            column(name: 'EVENT_ID',            type: 'BIGINT')
            column(name: 'TRADING_PARTICIPANT', type: 'VARCHAR(100)')
            column(name: 'PARTICIPANT_NAME',    type: 'VARCHAR(255)')
        }

        addForeignKeyConstraint(baseColumnNames: 'FILE_ID', baseTableName: 'TXN_BCQ_HEADER', constraintName: 'FK_BCQ_FILE',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'FILE_ID', referencedTableName: 'TXN_BCQ_UPLOAD_FILE')

        addForeignKeyConstraint(baseColumnNames: 'BCQ_HEADER_ID', baseTableName: 'TXN_BCQ_DATA', constraintName: 'FK_BCQ_HEADER',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'BCQ_HEADER_ID', referencedTableName: 'TXN_BCQ_HEADER')

        addUniqueConstraint(columnNames: 'SELLING_MTN, BILLING_ID, TRADING_DATE, SELLING_PARTICIPANT_SHORT_NAME',
                constraintName: 'UK_BCQ_HEADER',
                tableName: 'TXN_BCQ_HEADER')

        addUniqueConstraint(columnNames: 'BCQ_HEADER_ID, END_TIME',
                constraintName: 'UK_BCQ_DATA',
                tableName: 'TXN_BCQ_DATA')

        addForeignKeyConstraint(
                baseColumnNames:       'EVENT_ID',
                baseTableName:         'TXN_BCQ_EVENT_TRADING_DATE',
                constraintName:        'FK_BCQ_EVENT_DATE',
                deferrable:             false,
                initiallyDeferred:      false,
                referencedColumnNames: 'EVENT_ID',
                referencedTableName:   'TXN_BCQ_SPECIAL_EVENT'
        )

        addForeignKeyConstraint(
                baseColumnNames:       'EVENT_ID',
                baseTableName:         'TXN_BCQ_EVENT_PARTICIPANT',
                constraintName:        'FK_BCQ_EVENT_PARTICIPANT',
                deferrable:             false,
                initiallyDeferred:      false,
                referencedColumnNames: 'EVENT_ID',
                referencedTableName:   'TXN_BCQ_SPECIAL_EVENT'
        )
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_03", author: 'clim', failOnError: 'true') {
        comment 'Create Meter Quantity sequences'

        createSequence(sequenceName: 'MQ_MANIFEST_HEADER_SEQ',
                startValue: '1',
                incrementBy: '1',
                cycle: 'true')

        createSequence(sequenceName: 'MQ_MANIFEST_FILE_SEQ',
                startValue: '1',
                incrementBy: '1',
                cycle: 'true')

        createSequence(sequenceName: 'MQ_DATA_DAILY_SEQ',
                startValue: '1',
                incrementBy: '1',
                cycle: 'true')

        createSequence(sequenceName: 'MQ_DATA_MONTHLY_SEQ',
                startValue: '1',
                incrementBy: '1',
                cycle: 'true')

        createSequence(sequenceName: 'HIBERNATE_SEQUENCE',
                startValue: '1',
                incrementBy: '1',
                cycle: 'true')
    }

}
