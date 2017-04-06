package com.pemc.crss.metering.resource

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.pemc.crss.commons.cache.service.CacheConfigService
import com.pemc.crss.metering.constants.BcqStatus
import com.pemc.crss.metering.dto.bcq.*
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEvent
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventList
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventParticipant
import com.pemc.crss.metering.parser.bcq.BcqReader
import com.pemc.crss.metering.service.BcqService
import com.pemc.crss.metering.service.reports.BcqReportService
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage
import com.pemc.crss.metering.validator.bcq.BcqValidationResult
import com.pemc.crss.metering.validator.bcq.handler.BcqValidationHandler
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import static com.pemc.crss.metering.constants.BcqValidationError.EMPTY
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static java.util.Arrays.asList
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*

@WebMvcTest(controllers = BcqResource, secure = false)
class BcqResourceTest extends Specification {

    @Autowired
    BcqReader bcqReader;

    @Autowired
    BcqValidationHandler validationHandler;

    @Autowired
    BcqService bcqService

    @Autowired
    BcqReportService reportService;

    @Autowired
    CacheConfigService configService;

    @Autowired
    MockMvc mockMvc

    @Unroll
    def "upload bcq with validation status: #status"() {
        given:
        def file = new MockMultipartFile('file', 'file.csv', 'text/csv', 'some data'.bytes)
        def validationResult = new BcqValidationResult(status, null, null)
        def declaration = new BcqDeclaration(sellerDetails: new ParticipantSellerDetails('Gen 1', 'GEN1'),
                validationResult: validationResult, headerDetailsList: headerDetailsList)

        when:
        def response = mockMvc.perform(fileUpload('/bcq/upload')
                .file(file)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqReader.readCsv(_ as InputStream) >> []
        1 * validationHandler.processAndValidate(_ as List) >> declaration
        if (declaration.getHeaderDetailsList() != null) {
            1 * bcqService.findHeadersOfParticipantByTradingDate(_ as String, _ as Date) >> []
        }
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            1 * bcqService.saveDeclaration(_ as BcqDeclaration, false)
        }

        response.contentType == APPLICATION_JSON_UTF8_VALUE
        def content = new JsonSlurper().parseText(response.contentAsString)

        if (status == REJECTED) {
            response.status == 422
            content.status == REJECTED
        } else {
            response.status == 200
            content.uploadFileDetails.fileName == 'file.csv'
            content.uploadFileDetails.validationStatus == ACCEPTED
            content.headerDetailsList.size == 1
        }

        where:
        status   | headerDetailsList
        REJECTED | null
        ACCEPTED | [new BcqHeaderDetails(tradingDate: new Date(), sellingMtn: 'MTN1', billingId: 'BILL1', dataDetailsList: [])]
    }


    @Unroll
    def "upload bcq by web service with validation status: #status"() {
        given:
        def file = new MockMultipartFile('file', fileName, 'text/csv', 'some data'.bytes)
        def validationResult = new BcqValidationResult(status, new BcqValidationErrorMessage(EMPTY), null)
        def declaration = new BcqDeclaration(sellerDetails: new ParticipantSellerDetails('Gen 1', 'GEN1'),
                validationResult: validationResult, headerDetailsList: headerDetailsList)

        when:
        def response = mockMvc.perform(fileUpload('/bcq/webservice/upload')
                .file(file)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        if (fileName == 'file.csv') {
            1 * bcqReader.readCsv(_ as InputStream) >> []
            1 * validationHandler.processAndValidate(_ as List) >> declaration
            1 * bcqService.saveDeclaration(_ as BcqDeclaration, false)
        }
        if (declaration.getHeaderDetailsList() != null) {
            1 * bcqService.findHeadersOfParticipantByTradingDate(_ as String, _ as Date) >> currentHeaders
        }

        response.contentType == APPLICATION_JSON_UTF8_VALUE
        def content = response.contentAsString
        println content
        if (status == REJECTED) {
            response.status == 422
            content == 'No data found.'
        } else {
            response.status == 200
            if (declaration.isResubmission()) {
                content == 'Successfully saved resubmission.'
            } else {
                content == 'Successfully saved declaration.'
            }
        }

        where:
        fileName   | status   | currentHeaders
        'file.doc' | REJECTED | []
        'file.csv' | REJECTED | []
        'file.csv' | ACCEPTED | []
        'file.csv' | ACCEPTED | [new BcqHeader()]

        headerDetailsList << [
                null,
                null,
                [new BcqHeaderDetails(tradingDate: new Date(), sellingMtn: 'MTN1', billingId: 'BILL1', dataDetailsList: [])],
                [new BcqHeaderDetails(tradingDate: new Date(), sellingMtn: 'MTN1', billingId: 'BILL1', dataDetailsList: [])]
        ]
    }

    @Unroll
    def "upload bcq by settlement with validation status: #status"() {
        given:
        def file = new MockMultipartFile('file', 'file.csv', 'text/csv', 'some data'.bytes)
        def validationResult = new BcqValidationResult(status, null, null)
        def declaration = new BcqDeclaration(sellerDetails: new ParticipantSellerDetails('Gen 1', 'GEN1'),
                validationResult: validationResult, headerDetailsList: headerDetailsList)

        when:
        def response = mockMvc.perform(fileUpload('/bcq/settlement/upload')
                .file(file)
                .param('sellerDetailsString', 'Gen 1, GEN1')
                .param('tradingDateString', '2016-03-28')
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqReader.readCsv(_ as InputStream) >> []
        1 * validationHandler.processAndValidateForSettlement(_ as List, _ as ParticipantSellerDetails, _ as Date) >> declaration
        if (declaration.getHeaderDetailsList() != null) {
            1 * bcqService.findHeadersOfParticipantByTradingDate(_ as String, _ as Date) >> []
        }
        if (declaration.getValidationResult().getStatus() == REJECTED) {
            1 * bcqService.saveDeclaration(_ as BcqDeclaration, true)
        }

        response.contentType == APPLICATION_JSON_UTF8_VALUE
        def content = new JsonSlurper().parseText(response.contentAsString)

        if (status == REJECTED) {
            response.status == 422
            content.status == REJECTED
        } else {
            response.status == 200
            content.uploadFileDetails.fileName == 'file.csv'
            content.uploadFileDetails.validationStatus == ACCEPTED
            content.headerDetailsList.size == 1
        }

        where:
        status   | headerDetailsList
        REJECTED | null
        ACCEPTED | [new BcqHeaderDetails(tradingDate: new Date(), sellingMtn: 'MTN1', billingId: 'BILL1', dataDetailsList: [])]
    }

    @Unroll
    def "save declaration by #user"() {
        given:
        def builder = new JsonBuilder()
        builder {
            uploadFileDetails new BcqUploadFileDetails()
            sellerDetails new ParticipantSellerDetails()
            headerDetailsList asList(new BcqHeaderDetails())
            validationResult new BcqValidationResult<>()
            isResubmission false
            isSpecialEvent false
        }
        and:
        def requestBody = builder.toPrettyString()

        when:
        def response = mockMvc.perform(post(url)
                .content(requestBody)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        response.status == 200

        where:
        user         | url
        'seller'     | '/bcq/save'
        'settlement' | '/bcq/settlement/save'
    }

    @Unroll
    def "update header with status: #status"() {
        when:
        def response = mockMvc.perform(post('/bcq/declaration/update-status/1')
                .param('status', status)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.updateHeaderStatus(1, BcqStatus.fromString(status))
        response.status == 200

        where:
        status << ['CONFIRMED', 'NULLIFIED', 'CANCELLED']
    }

    @Unroll
    def "#action of header"() {
        when:
        def response = mockMvc.perform(post('/bcq/declaration/settlement/' + url + '/1')
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        if (action == 'approval') {
            1 * bcqService.approve(1)
        } else {
            1 * bcqService.requestForCancellation(1)
        }
        response.status == 200

        where:
        action                 | url
        'request cancellation' | 'request-cancel'
        'approval'             | 'approve'
    }

    def "save special event"() {
        given:
        def builder = new JsonBuilder()
        builder {
            tradingDates asList(new Date(), new Date())
            deadlineDate new Date()
            tradingParticipants asList(new BcqSpecialEventParticipant())
            remarks 'remarks'
        }
        and:
        def requestBody = builder.toPrettyString()

        when:
        def response = mockMvc.perform(post('/bcq/special-event/save')
                .content(requestBody)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.saveSpecialEvent(_ as BcqSpecialEvent)
        response.status == 200
    }

    def "get special events list"() {
        given:
        def specialEventList = [new BcqSpecialEventList(
                eventId: 1,
                tradingDates: [new Date()],
                deadlineDate: new Date(),
                remarks: 'remarks',
                tradingParticipants: [new BcqSpecialEventParticipant(participantName: 'Pdu 1', shortName: 'PDU1')],
                createdDate: new Date(),
                tradingParticipantsLabel: ['PDU1']
        )]

        when:
        def response = mockMvc.perform(get('/bcq/special-event/list')
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.findAllSpecialEvents() >> specialEventList
        response.status == 200
        def content = new JsonSlurper().parseText(response.contentAsString)
        content[0].eventId == specialEventList[0].eventId
        content[0].remarks == specialEventList[0].remarks
        content[0].tradingParticipants.participantName == specialEventList[0].tradingParticipants.participantName
        content[0].tradingParticipants.shortName == specialEventList[0].tradingParticipants.shortName
    }

    def "generate data report"() {
        given:
        def builder = new JsonBuilder()
        builder {
            tradingDate new Date()
        }
        and:
        def requestBody = builder.toPrettyString()

        when:
        def response = mockMvc.perform(post('/bcq/data/report')
                .content(requestBody)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * reportService.generateDataReport(_ as Map, _ as OutputStream)
        response.status == 200
        response.contentType == 'application/x-msdownload'
    }

    def "generate template"() {
        when:
        def response = mockMvc.perform(get('/bcq/template')
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * reportService.generateTemplate(_ as OutputStream)
        response.status == 200
        response.contentType == 'application/x-msdownload'
    }

    def "get allowable trading date config"() {
        when:
        def response = mockMvc.perform(get('/bcq/settlement/config')
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * configService.getIntegerValueForKey(_ as String, _ as Integer) >> 1
        response.status == 200
        response.contentAsString == '1'
    }

    def "add prohibited pair"() {
        given:
        def builder = new JsonBuilder()
        builder {
            sellingMtn 'MTN1'
            billingId 'BILL1'
            createdBy 'BILLING'
        }
        and:
        def requestBody = builder.toPrettyString()

        when:
        def response = mockMvc.perform(post('/bcq/prohibited/save')
                .content(requestBody)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.saveProhibitedPair(_ as BcqProhibitedPair)
        response.status == 200
    }

    def "disable prohibited pair"() {
        when:
        def response = mockMvc.perform(put('/bcq/prohibited/1/disable')
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.disableProhibitedPair(1)
        response.status == 200
    }

    @Configuration
    static class Config {
        def factory = new DetachedMockFactory()

        @Bean
        def bcqResource() {
            new BcqResource(bcqReader(), validationHandler(), bcqService(), bcqReportService(), configService())
        }

        @Bean
        def bcqReader() {
            factory.Mock(BcqReader)
        }

        @Bean
        def validationHandler() {
            factory.Mock(BcqValidationHandler)
        }

        @Bean
        def bcqService() {
            factory.Mock(BcqService)
        }

        @Bean
        def bcqReportService() {
            factory.Mock(BcqReportService)
        }

        @Bean
        def configService() {
            factory.Mock(CacheConfigService)
        }

        @Bean
        public MappingJackson2HttpMessageConverter jsonConverter() {
            def converter = new MappingJackson2HttpMessageConverter()
            converter.getObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            return converter
        }
    }

}
