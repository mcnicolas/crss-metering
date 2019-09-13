databaseChangeLog {
    changeSet(id: "20190911-01_01", author: "damasa") {
        addColumn(tableName: 'TXN_MQ_MANIFEST_HEADER') {
            column(name: 'CLOSURE_TIME_SETTING', type: 'VARCHAR(100)')
            column(name: 'ALLOWABLE_DATE_SETTING', type: 'VARCHAR(100)')
        }
    }
}