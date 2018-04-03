REL  = '2.5.0'
FILE = '20180403'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/") {
    changeSet(id: "${REL}_${FILE}_01", author: 'rlevida') {
        addColumn(tableName: 'txn_bcq_header') {
            column(name: 'uploaded_by', type: 'VARCHAR(255)')
        }
    }
}