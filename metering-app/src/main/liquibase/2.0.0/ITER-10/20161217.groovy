REL  = "2.0.0"
ITER = "ITER-10"
FILE = "20161217"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: true) {
        modifyDataType(tableName: 'TXN_MQ_MANIFEST_FILE',
            columnName: 'ERROR_DETAILS',
            newDataType: 'VARCHAR(500)')
    }

}
