REL  = '2.8.0'
FILE = '20190710'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/") {
    changeSet(id: "${REL}_${FILE}_01", author: 'jlapugot') {
        addColumn(tableName: 'txn_mq_manifest_header') {
            column(name: 'converted_to_five_min', type: 'CHAR(1)', defaultValue: 'N') {
                constraints(nullable: false)
            }
        }
    }
}