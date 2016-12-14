REL  = "2.0.0"
ITER = "ITER-10"
FILE = "20161216"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: true) {
        addColumn(tableName: 'TXN_METER_DATA_DAILY') {
            column(name: 'CREATED_DATE_TIME', type: 'TIMESTAMP')
        }

        addColumn(tableName: 'TXN_METER_DATA_MONTHLY') {
            column(name: 'CREATED_DATE_TIME', type: 'TIMESTAMP')
        }
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_02", author: 'clim', failOnError: true) {
        dropUniqueConstraint(constraintName: 'METER_DATA_DAILY_UNIQUE', tableName: 'TXN_METER_DATA_DAILY')
        dropUniqueConstraint(constraintName: 'METER_DATA_MONTHLY_UNIQUE', tableName: 'TXN_METER_DATA_MONTHLY')
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_03", author: 'clim', failOnError: true) {
        addUniqueConstraint(tableName: 'TXN_METER_DATA_DAILY',
                columnNames: 'SEIN, READING_DATETIME, CREATED_DATE_TIME',
                constraintName: 'METER_DATA_DAILY_UNIQUE')

        addUniqueConstraint(tableName: 'TXN_METER_DATA_MONTHLY',
                columnNames: 'SEIN, READING_DATETIME, CREATED_DATE_TIME',
                constraintName: 'METER_DATA_MONTHLY_UNIQUE')
    }

}
