REL  = '2.0.0'
ITER = 'ITER-15'
FILE = '20170221'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        createTable(tableName: 'MAP_BILLING_ID_TAX_DATA', remarks: 'Mock table for billing id validation') {
            column(name: 'ID',                             type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'EFFECTIVE_START_DATE',           type: 'TIMESTAMP')
            column(name: 'EFFECTIVE_END_DATE',             type: 'TIMESTAMP')
            column(name: 'BILLING_ID',                     type: 'VARCHAR(255)')
            column(name: 'TRADING_PARTICIPANT_SHORT_NAME', type: 'VARCHAR(255)')
        }
    }

}

