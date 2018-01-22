REL  = '2.4.0'
FILE = '20180122'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/") {
    changeSet(id: "${REL}_${FILE}_01", author: 'rlevida (generated)') {
        dropUniqueConstraint(constraintName: 'uk_bcq_data', tableName: 'txn_bcq_data')
        addUniqueConstraint(columnNames: 'header_id, end_time, reference_mtn', constraintName: 'uk_bcq_data', tableName: 'txn_bcq_data')
    }
    changeSet(id: "${REL}_${FILE}_02", author: 'rlevida') {
        addColumn(tableName: 'txn_bcq_data') {
            column(name: 'buyer_mtn', type: 'VARCHAR(255)')
        }
    }
}