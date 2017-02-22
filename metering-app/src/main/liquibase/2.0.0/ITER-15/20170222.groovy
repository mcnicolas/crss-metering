REL  = '2.0.0'
ITER = 'ITER-15'
FILE = '20170222'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        dropColumn(columnName: 'selling_participant_user_id', tableName: 'txn_bcq_header')
        dropColumn(columnName: 'buying_participant_user_id', tableName: 'txn_bcq_header')
    }

}

