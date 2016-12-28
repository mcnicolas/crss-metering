REL  = '2.0.0'
ITER = 'ITER-10'
FILE = '20161228'

databaseChangeLog(logicalFilePath: "/liquibase/${REL}/${ITER}") {

    changeSet(id: "${REL}_${ITER}_${FILE}_01", author: 'clim', failOnError: true) {
        comment 'Modify meter quantity numeric scaling'

        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'KWD', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'KWHD', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'KVARHD', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'KWR', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'KWHR', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'KVARHR', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'VAN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'VBN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'VCN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'IAN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'IBN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'ICN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_DAILY', columnName: 'PF', newDataType: 'NUMBER(25,17)')

        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'KWD', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'KWHD', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'KVARHD', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'KWR', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'KWHR', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'KVARHR', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'VAN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'VBN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'VCN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'IAN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'IBN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'ICN', newDataType: 'NUMBER(25,17)')
        modifyDataType(tableName: 'TXN_METER_DATA_MONTHLY', columnName: 'PF', newDataType: 'NUMBER(25,17)')
    }

}
