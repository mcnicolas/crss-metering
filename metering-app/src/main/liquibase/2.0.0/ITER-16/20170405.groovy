REL  = '2.0.0'
ITER = 'ITER-16'
FILE = '20170405'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        createTable(tableName: 'TXN_BCQ_PROHIBITED') {
            column(name: 'ID',           type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'CREATED_BY',   type: 'VARCHAR(255)')
            column(name: 'CREATED_DATE', type: 'TIMESTAMP')
            column(name: 'SELLING_MTN',  type: 'VARCHAR(255)')
            column(name: 'BILLING_ID',   type: 'VARCHAR(255)')
            column(name: 'ENABLED', type: 'BOOLEAN')
        }
    }

}

