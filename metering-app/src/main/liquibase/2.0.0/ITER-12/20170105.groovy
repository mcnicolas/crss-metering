REL = "2.0.0"
ITER = "ITER-12"
FILE = "20170105"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim') {
        addNotNullConstraint(tableName: 'TXN_MQ_MANIFEST_HEADER', columnName: 'NOTIFICATION_SENT', defaultNullValue: 'N')
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_02", author: 'clim') {
        addColumn(tableName: 'TXN_MQ_MANIFEST_FILE') {
            column(name: 'PROCESS_DATETIME', type: 'TIMESTAMP')
        }
    }

}
