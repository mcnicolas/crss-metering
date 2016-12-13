REL  = "2.0.0"
ITER = "ITER-10"
FILE = "20161213"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        addColumn(tableName: 'txn_bcq_upload_file') {
            column(name: 'validation_status', type: 'VARCHAR(8)')
        }
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_02", author: 'jlapugot', failOnError: true) {
        addColumn(tableName: 'txn_bcq_header') {
            column(name: 'updated_via', type: 'VARCHAR(50)')
        }
    }

}
