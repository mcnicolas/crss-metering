REL = "2.0.0"
ITER = "ITER-12"
FILE = "20170111"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot') {
        modifyDataType(columnName: 'STATUS', newDataType: 'VARCHAR(30)', tableName: 'TXN_BCQ_HEADER')
    }

}
