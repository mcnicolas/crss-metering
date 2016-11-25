REL  = "2.0.0"
ITER = "ITER-09"
FILE = "20161125"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: true) {
        dropTable(tableName: 'txn_meter_data_xls', cascadeConstraints: true)
        dropTable(tableName: 'txn_interval', cascadeConstraints: true)
        dropTable(tableName: 'txn_channel_header', cascadeConstraints: true)
        dropTable(tableName: 'txn_meter_header', cascadeConstraints: true)
        dropTable(tableName: 'txn_meter_upload_file', cascadeConstraints: true)
        dropTable(tableName: 'txn_meter_upload_header', cascadeConstraints: true)
    }

    changeSet(id: "${REL}_${ITER}_${FILE}_02", author: 'clim', failOnError: true) {
        addColumn(tableName: 'txn_meter_data_daily') {
            column(name: 'msp_shortname', type: 'VARCHAR(50)')
        }

        addColumn(tableName: 'txn_meter_data_monthly') {
            column(name: 'msp_shortname', type: 'VARCHAR(50)')
        }

        dropColumn(tableName: 'txn_mq_manifest_header') {
            column(name: 'msp_id')
        }
    }

}
