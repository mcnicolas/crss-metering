REL  = '2.6.0'
FILE = '20180625'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/") {
    changeSet(id: "${REL}_${FILE}_01", author: 'jdimayuga') {
        addColumn(tableName: 'txn_meter_data_daily') {
            column(name: 'ORPHAN', type: 'BOOLEAN', defaultValue: 'FALSE') {
                constraints(nullable: false)
            }
        }

        addColumn(tableName: 'txn_meter_data_monthly') {
            column(name: 'ORPHAN', type: 'BOOLEAN', defaultValue: 'FALSE') {
                constraints(nullable: false)
            }
        }
    }
}