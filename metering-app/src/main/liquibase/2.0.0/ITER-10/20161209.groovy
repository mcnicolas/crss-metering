REL = "2.0.0"
ITER = "ITER-10"
FILE = "20161209"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: 'true') {
        comment 'Create metering sequences'

        createSequence(sequenceName: 'MQ_MANIFEST_HEADER_SEQ',
                startValue: '1',
                incrementBy: '1',
                cycle: 'false')

        createSequence(sequenceName: 'MQ_MANIFEST_FILE_SEQ',
                startValue: '1',
                incrementBy: '1',
                cycle: 'false')

        createSequence(sequenceName: 'MQ_DATA_DAILY_SEQ',
                startValue: '1',
                incrementBy: '1',
                cycle: 'false')

        createSequence(sequenceName: 'MQ_DATA_MONTHLY_SEQ',
                startValue: '1',
                incrementBy: '1',
                cycle: 'false')
    }

}
