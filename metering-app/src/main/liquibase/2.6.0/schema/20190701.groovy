REL  = '2.6.0'
FILE = '20190701'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/") {
    changeSet(id: "${REL}_${FILE}_01", author: 'jmengote') {
        createIndex(tableName: 'txn_mq_manifest_file', indexName: 'idx_tx_id', unique: false) {
            column(name: 'transaction_id')
        }
        createIndex(tableName: 'txn_meter_data_daily', indexName: 'idx_txn_meter_data_daily_file_id', unique: false) {
            column(name: 'file_id')
        }
        createIndex(tableName: 'txn_meter_data_monthly', indexName: 'idx_txn_meter_data_monthly_file_id', unique: false) {
            column(name: 'file_id')
        }
    }
}