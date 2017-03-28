package com.pemc.crss.metering.resource

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.pemc.crss.commons.cache.service.CacheConfigService
import com.pemc.crss.metering.dto.bcq.BcqDeclaration
import com.pemc.crss.metering.dto.bcq.BcqHeader
import com.pemc.crss.metering.dto.bcq.BcqHeaderDetails
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails
import com.pemc.crss.metering.parser.bcq.BcqReader
import com.pemc.crss.metering.service.BcqService
import com.pemc.crss.metering.service.reports.BcqReportService
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage
import com.pemc.crss.metering.validator.bcq.BcqValidationResult
import com.pemc.crss.metering.validator.bcq.handler.BcqValidationHandler
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import static com.pemc.crss.metering.constants.BcqValidationError.EMPTY
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload

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
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
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

        response.contentType == MediaType.APPLICATION_JSON_UTF8_VALUE
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
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
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

        response.contentType == MediaType.APPLICATION_JSON_UTF8_VALUE
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
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE))
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

        response.contentType == MediaType.APPLICATION_JSON_UTF8_VALUE
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
