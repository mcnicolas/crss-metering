REL = "2.0.0"
ITER = "ITER-12"
FILE = "20170118"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim') {
        modifyDataType(columnName: 'FILENAME', newDataType: 'VARCHAR(100)', tableName: 'TXN_MQ_MANIFEST_FILE')
    }

}
