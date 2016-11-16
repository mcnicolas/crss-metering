REL  = "2.0.0"
ITER = "ITER-01"
FILE = "20161104"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        createTable(tableName: 'txn_bcq_upload_file', remarks: 'BCQ upload file') {
            column(name: 'file_id',         type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'transaction_id',  type: 'VARCHAR(36)') { constraints(nullable: false) }
            column(name: 'file_name',       type: 'VARCHAR(50)')
            column(name: 'file_size',       type: 'BIGINT')
            column(name: 'submitted_date',  type: 'TIMESTAMP')
        }

        createTable(tableName: 'txn_bcq_header', remarks: 'BCQ header') {
            column(name: 'bcq_header_id',                   type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'file_id',                         type: 'BIGINT')
            column(name: 'selling_mtn',                     type: 'VARCHAR(255)')
            column(name: 'buying_participant',              type: 'VARCHAR(255)')
            column(name: 'selling_participant_name',        type: 'VARCHAR(255)')
            column(name: 'selling_participant_short_name',  type: 'VARCHAR(255)')
            column(name: 'status',                          type: 'VARCHAR(17)',)
            column(name: 'trading_date',                    type: 'TIMESTAMP')
        }

        createTable(tableName: 'txn_bcq_data', remarks: 'BCQ data') {
            column(name: 'bcq_data_id',    type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'bcq_header_id',  type: 'BIGINT')
            column(name: 'reference_mtn',  type: 'VARCHAR(255)') { constraints(nullable: false) }
            column(name: 'start_time',     type: 'TIMESTAMP') { constraints(nullable: false) }
            column(name: 'end_time',       type: 'TIMESTAMP') { constraints(nullable: false) }
            column(name: 'bcq',            type: 'NUMERIC(19,9)') { constraints(nullable: false) }
        }

        addForeignKeyConstraint(
                baseColumnNames: 'file_id', baseTableName: 'txn_bcq_header', constraintName: 'fk_bcq_file',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'file_id', referencedTableName: 'txn_bcq_upload_file')

        addForeignKeyConstraint(
                baseColumnNames: 'bcq_header_id', baseTableName: 'txn_bcq_data', constraintName: 'fk_bcq_header',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'bcq_header_id', referencedTableName: 'txn_bcq_header')

        addUniqueConstraint(
                columnNames: 'selling_mtn, buying_participant, trading_date',
                constraintName: 'uk_bcq_header',
                tableName: 'txn_bcq_header')

        addUniqueConstraint(
                columnNames: 'bcq_header_id, end_time',
                constraintName: 'uk_bcq_data',
                tableName: 'txn_bcq_data')
    }

}