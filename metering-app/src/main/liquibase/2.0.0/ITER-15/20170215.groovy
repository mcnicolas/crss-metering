REL  = '2.0.0'
ITER = 'ITER-15'
FILE = '20170215'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'dmendoza', failOnError: true) {
        addColumn(tableName: 'TXN_BCQ_HEADER') {
            column(name: 'CREATED_DATE', type: 'TIMESTAMP')
            column(name: 'MODIFIED_DATE', type: 'TIMESTAMP')
        }
    }

}
