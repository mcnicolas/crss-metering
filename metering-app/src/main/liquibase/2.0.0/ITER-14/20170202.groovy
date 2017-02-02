REL = "2.0.0"
ITER = "ITER-14"
FILE = "20170202"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'dmendoza') {
        addColumn(tableName: 'TXN_BCQ_SPECIAL_EVENT') {
            column(name: 'CREATED_DATE', type: 'TIMESTAMP')
        }
    }

}
