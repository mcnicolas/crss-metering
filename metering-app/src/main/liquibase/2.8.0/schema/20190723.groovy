REL  = '2.8.0'
FILE = '20190723'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/") {
    changeSet(id: "${REL}_${FILE}_01", author: 'jlapugot') {
        dropUniqueConstraint(tableName: 'TXN_METER_DATA_DAILY', constraintName: 'UK_METER_DATA_DAILY')
    }

    changeSet(id: "${REL}_${FILE}_02", author: 'jlapugot') {
        dropUniqueConstraint(tableName: 'TXN_METER_DATA_MONTHLY', constraintName: 'UK_METER_DATA_MONTHLY')
    }
}
