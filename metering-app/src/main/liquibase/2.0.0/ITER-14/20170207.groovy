REL  = '2.0.0'
ITER = 'ITER-14'
FILE = '20170207'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: true) {
        addColumn(tableName: 'TXN_MQ_MANIFEST_HEADER') {
            column(name: 'MSP_SHORTNAME', type: 'VARCHAR(50)')
        }
    }

}
