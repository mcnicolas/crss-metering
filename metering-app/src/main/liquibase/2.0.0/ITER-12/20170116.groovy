REL = "2.0.0"
ITER = "ITER-12"
FILE = "20170116"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot') {
        modifyDataType(columnName: 'BCQ', newDataType: 'NUMBER(28,9)', tableName: 'TXN_BCQ_DATA')
    }

}
