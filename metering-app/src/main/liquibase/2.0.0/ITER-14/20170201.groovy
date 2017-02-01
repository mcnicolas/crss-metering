REL = "2.0.0"
ITER = "ITER-14"
FILE = "20170201"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim') {
        addColumn(tableName: 'TXN_METER_DATA_DAILY') {
            column(name: 'CONSUMED', type: 'BOOLEAN', defaultValue: 'FALSE') {
                constraints(nullable: false)
            }
        }

        addColumn(tableName: 'TXN_METER_DATA_MONTHLY') {
            column(name: 'CONSUMED', type: 'BOOLEAN', defaultValue: 'FALSE') {
                constraints(nullable: false)
            }
        }
    }

}
