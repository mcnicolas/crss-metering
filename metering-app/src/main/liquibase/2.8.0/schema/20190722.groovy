REL  = '2.8.0'
FILE = '20190722'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/") {
    changeSet(id: "${REL}_${FILE}_01", author: 'jlapugot') {
        addUniqueConstraint(
                columnNames: 'CATEGORY, SEIN, READING_DATETIME',
                constraintName: 'UK_METER_DATA_DAILY',
                tableName: 'TXN_METER_DATA_DAILY'
        )
    }

    changeSet(id: "${REL}_${FILE}_02", author: 'jlapugot') {
        addUniqueConstraint(
                columnNames: 'CATEGORY, SEIN, READING_DATETIME',
                constraintName: 'UK_METER_DATA_MONTHLY',
                tableName: 'TXN_METER_DATA_MONTHLY'
        )
    }
}
