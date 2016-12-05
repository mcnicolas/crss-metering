REL  = "2.0.0"
ITER = "ITER-10"
FILE = "20161205"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        renameColumn(tableName: 'txn_bcq_header',
                oldColumnName: 'buying_participant',
                newColumnName: 'buying_participant_short_name')

        addColumn(tableName: 'txn_bcq_header') {
            column(name: 'buying_participant_name', type: 'VARCHAR(50)')
        }
    }

}
