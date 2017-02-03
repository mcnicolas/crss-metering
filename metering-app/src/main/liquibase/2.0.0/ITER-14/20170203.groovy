REL = "2.0.0"
ITER = "ITER-14"
FILE = "20170203"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'dmendoza') {
        addColumn(tableName: 'TXN_BCQ_EVENT_PARTICIPANT') {
            column(name: 'PARTICIPANT_NAME', type: 'VARCHAR(255)')
        }
    }

}
