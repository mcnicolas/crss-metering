REL  = "2.0.0"
ITER = "ITER-10"
FILE = "20161211"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: true) {
        addColumn(tableName: 'TXN_METER_DATA_DAILY') {
            column(name: 'category', type: 'VARCHAR(20)')
        }

        addColumn(tableName: 'TXN_METER_DATA_MONTHLY') {
            column(name: 'category', type: 'VARCHAR(20)')
        }
    }
}
