REL  = "2.0.0"
ITER = "ITER-10"
FILE = "20161212"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        addColumn(tableName: 'txn_bcq_header') {
            column(name: 'billing_id', type: 'VARCHAR(50)')
        }
    }

}
