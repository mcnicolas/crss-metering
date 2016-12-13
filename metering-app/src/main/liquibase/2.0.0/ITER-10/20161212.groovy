REL  = "2.0.0"
ITER = "ITER-10"
FILE = "20161212"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        addColumn(tableName: 'txn_bcq_header') {
            column(name: 'billing_id', type: 'VARCHAR(50)')
        }
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_02", author: 'clim', failOnError: true) {
        addUniqueConstraint(tableName: 'TXN_METER_DATA_DAILY',
                columnNames: 'SEIN, READING_DATETIME, VERSION',
                constraintName: 'METER_DATA_DAILY_UNIQUE')

        addUniqueConstraint(tableName: 'TXN_METER_DATA_MONTHLY',
                columnNames: 'SEIN, READING_DATETIME, VERSION',
                constraintName: 'METER_DATA_MONTHLY_UNIQUE')
    }

}
