package com.pemc.crss.metering.service.reports

import com.pemc.crss.metering.dao.BcqDao
import com.pemc.crss.metering.service.reports.dto.BcqDataReportBean
import com.pemc.crss.metering.utils.DateTimeUtils
import spock.lang.Specification

import java.sql.Timestamp

import static org.apache.commons.lang3.time.DateUtils.addDays

class BcqReportServiceImplTest extends Specification {

    def bcqDao = Mock(BcqDao)
    def sut = new BcqReportServiceImpl(bcqDao)

    def "generate data report bean"() {
        given:
        def mapParams = new HashMap();
        def outputStream = new ByteArrayOutputStream()
        def date = DateTimeUtils.parseDate('03/30/2017')
        def reportBeans = [new BcqDataReportBean(
                sellingParticipant: 'GEN1',
                buyingParticipant: 'PDU1',
                sellingMtn: 'MTN1',
                billingId: 'BILL1',
                tradingDate: date,
                transactionId: '0000',
                submittedDate: new Timestamp(date.getTime()),
                deadlineDate: addDays(date, 1),
                status: 'CONFIRMED',
                updatedVia: 'RESUBMISSION',
                referenceMtn: 'MTN1',
                endTime: new Timestamp(addDays(date, 1).getTime()),
                bcq: new BigDecimal(1)
        )]

        when:
        sut.generateDataReport(mapParams, outputStream)

        then:
        1 * bcqDao.queryBcqDataReport(_ as Map) >> reportBeans
        def stringArrayContent = outputStream.toString().split('\n')
        stringArrayContent[0].trim() == 'Selling Participant,Buying Participant,Selling MTN,Buyer Billing ID,Trading Date,Transaction ID,Submitted Date / Time,Deadline Date,Status,Updated Via,Reference MTN,Time,BCQ'.trim()
        stringArrayContent[1].trim() == 'GEN1,PDU1,MTN1,BILL1,2017-03-30,0000,2017-03-30 00:00,2017-03-31,CONFIRMED,RESUBMISSION,MTN1,2017-03-31 00:00,1'.trim()
    }

    def "generate template"() {
        given:
        def outputStream = new ByteArrayOutputStream()

        when:
        sut.generateTemplate(outputStream)

        then:
        def stringArrayContent = outputStream.toString().split('\n')
        stringArrayContent[0].trim() == 'Interval,<Hourly/5mins/15mins>'.trim()
        stringArrayContent[1].trim() == 'Seller MTN (Resource ID),Buying Participant (Load Participant Name),Reference MTN (Resource ID),Date,BCQ'.trim()
        stringArrayContent[2].trim() == '<text>,<text>,<text>,<date with format yyyy-mm-dd hh:mm>,<numeric>'.trim()
        stringArrayContent[3].trim() == 'MTN_TRADE_1,ares,MTN_TRADE_1,2017-02-15 01:00,10'.trim()
    }

}
