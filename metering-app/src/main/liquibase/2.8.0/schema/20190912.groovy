databaseChangeLog {
    changeSet(id: "20190911-01_01", author: "damasa") {
        addColumn(tableName: 'txn_mq_manifest_header') {
            column(name: 'CLOSURE_TIME', type: 'VARCHAR(100)')
            column(name: 'ALLOWABLE_DATE', type: 'VARCHAR(100)')
        }
    }
}