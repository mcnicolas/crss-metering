REL = "2.0.0"
ITER = "ITER-12"
FILE = "20170112"

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'jlapugot', failOnError: true) {
        createTable(tableName: 'TXN_BCQ_SPECIAL_EVENT') {
            column(name: 'EVENT_ID',      type: 'BIGINT')  { constraints(primaryKey: true, nullable: false) }
            column(name: 'DEADLINE_DATE', type: 'TIMESTAMP')
            column(name: 'REMARKS',       type: 'VARCHAR(500)')
        }

        createTable(tableName: 'TXN_BCQ_EVENT_TRADING_DATE') {
            column(name: 'EVENT_ID',     type: 'BIGINT')
            column(name: 'TRADING_DATE', type: 'TIMESTAMP')
        }

        createTable(tableName: 'TXN_BCQ_EVENT_PARTICIPANT') {
            column(name: 'EVENT_ID',            type: 'BIGINT')
            column(name: 'TRADING_PARTICIPANT', type: 'VARCHAR(100)')
        }

        addForeignKeyConstraint(
                baseColumnNames:       'EVENT_ID',
                baseTableName:         'TXN_BCQ_EVENT_TRADING_DATE',
                constraintName:        'FK_BCQ_EVENT_DATE',
                deferrable:             false,
                initiallyDeferred:      false,
                referencedColumnNames: 'EVENT_ID',
                referencedTableName:   'TXN_BCQ_SPECIAL_EVENT'
        )

        addForeignKeyConstraint(
                baseColumnNames:       'EVENT_ID',
                baseTableName:         'TXN_BCQ_EVENT_PARTICIPANT',
                constraintName:        'FK_BCQ_EVENT_PARTICIPANT',
                deferrable:             false,
                initiallyDeferred:      false,
                referencedColumnNames: 'EVENT_ID',
                referencedTableName:   'TXN_BCQ_SPECIAL_EVENT'
        )
    }

}
