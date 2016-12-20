REL  = '2.0.0'
ITER = 'ITER-10'
FILE = '20161220'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: true) {
        comment 'Additional sequence for bcq'

        createSequence(sequenceName: 'HIBERNATE_SEQUENCE',
                startValue: '1',
                incrementBy: '1',
                cycle: 'false')
    }

}
