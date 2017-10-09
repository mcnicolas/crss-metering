REL  = '2.3.0'
FILE = '20171005'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/") {

    changeSet(id: "${REL}_${FILE}_01", author: 'rlevida', failOnError: true) {
        addColumn(tableName: 'TXN_BCQ_PROHIBITED') {
            column(name: 'EFFECTIVE_START_DATE', type: 'TIMESTAMP')
            column(name: 'EFFECTIVE_END_DATE', type: 'TIMESTAMP')
        }
    }

}

