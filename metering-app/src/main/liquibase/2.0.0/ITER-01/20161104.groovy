REL  = "2.0.0"
ITER = "ITER-01"
FILE = "20161104"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        createTable(tableName: 'txn_bcq_upload_file', remarks: 'BCQ upload file') {
            column(name: 'file_id',         type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'transaction_id',  type: 'VARCHAR(36)') { constraints(nullable: false) }
            column(name: 'filename',        type: 'VARCHAR(50)')
            column(name: 'filesize',        type: 'BIGINT')
            column(name: 'status',          type: 'VARCHAR(13)', remarks:
                    'Possible values: CONFIRMED, NOT_CONFIRMED, NULLIFIED, NOT_NULLIFIED')
        }

        createTable(tableName: 'txn_bcq_data', remarks: 'BCQ data') {
            column(name: 'bcq_data_id',               type: 'BIGINT') { constraints(primaryKey: true, nullable: false) }
            column(name: 'file_id',                   type: 'BIGINT') { constraints(nullable: false) }
            column(name: 'selling_participant_name',  type: 'VARCHAR(255)') { constraints(nullable: false) }
            column(name: 'selling_mtn',               type: 'VARCHAR(255)') { constraints(nullable: false) }
            column(name: 'buying_participant_id',     type: 'BIGINT') { constraints(nullable: false) }
            column(name: 'reference_mtn',             type: 'VARCHAR(255)') { constraints(nullable: false) }
            column(name: 'start_time',                type: 'TIMESTAMP') { constraints(nullable: false) }
            column(name: 'end_time',                  type: 'TIMESTAMP') { constraints(nullable: false) }
            column(name: 'bcq',                       type: 'NUMERIC(19,2)') { constraints(nullable: false) }
        }

        addForeignKeyConstraint(
                baseColumnNames: 'file_id', baseTableName: 'txn_bcq_data', constraintName: 'FK_BCQ_DATA',
                deferrable: false, initiallyDeferred: false,
                referencedColumnNames: 'file_id', referencedTableName: 'txn_bcq_upload_file')
    }

}