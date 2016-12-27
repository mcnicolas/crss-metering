REL = "2.0.0"
ITER = "ITER-06"
FILE = "20161227"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot') {
        addColumn(tableName:'txn_bcq_header'){
            column(name:'buying_participant_user_id', type:'INT')
            column(name:'selling_participant_user_id', type:'INT')
        }
    }

}