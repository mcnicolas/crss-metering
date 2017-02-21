package com.pemc.crss.metering.parser.bcq

import spock.lang.Specification
import spock.lang.Unroll

class BcqReaderTest extends Specification {

    BcqReader reader = new BcqReader()

    @Unroll
    def 'reader must read correctly the csv'() {
        given:
        def csvFile = new File(BcqReaderTest.class.getClassLoader().getResource('bcq/sample/' + csvFileName).getFile())

        when:
        def csv = reader.readCsv(new FileInputStream(csvFile))
        def firstLineList = csv.get(0)
        def thirdLineList = csv.get(2)
        def intervalString = firstLineList.get(1)
        def sellingMtnString = thirdLineList.get(0)
        def billingIdString = thirdLineList.get(1)
        def referenceMtnString = thirdLineList.get(2)
        def dateString = thirdLineList.get(3)
        def bcqString = thirdLineList.get(4)

        then:
        csv != null
        interval == intervalString
        sellingMtn == sellingMtnString
        billingId == billingIdString
        refMtn == referenceMtnString
        date == dateString
        bcq == bcqString

        where:
        csvFileName                  | interval | sellingMtn | billingId | refMtn | date               | bcq
        'sample_bcq_file_5mins.csv'  | '5mins'  | 'MTN1'     | 'BILL1'   | 'MTN1' | '02-20-2016 00:05' | "1"
        'sample_bcq_file_15mins.csv' | '15mins' | 'MTN2'     | 'BILL2'   | 'MTN2' | '02-21-2016 00:15' | "2"
        'sample_bcq_file_hourly.csv' | 'Hourly' | 'MTN3'     | 'BILL3'   | 'MTN3' | '02-22-2016 01:00' | "3"
    }

    def 'reader must return null if csv is invalid'() {
        given:
        def csvFileName = 'sample_bcq_file_invalid.csv'
        def csvFile = new File(BcqReaderTest.class.getClassLoader().getResource('bcq/sample/'
                + csvFileName).getFile())

        when:
        def csv = reader.readCsv(new FileInputStream(csvFile))

        then:
        csv == null
    }

}