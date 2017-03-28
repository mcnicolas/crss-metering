package com.pemc.crss.metering.resource

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.pemc.crss.commons.web.dto.datatable.PageableRequest
import com.pemc.crss.metering.dto.bcq.*
import com.pemc.crss.metering.service.BcqService
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageImpl
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import static com.pemc.crss.metering.constants.BcqStatus.VOID
import static com.pemc.crss.metering.dao.query.ComparisonOperator.NOT_IN
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest(controllers = BcqDisplayResource, secure = false)
class BcqDisplayResourceTest extends Specification {

    @Autowired
    BcqService bcqService

    @Autowired
    MockMvc mockMvc

    def "get header page"() {
        given:
        def headerList = [
                new BcqHeaderPageDisplay(headerId: 1, sellingMtn: 'MTN1', billingId: 'BILL1'),
                new BcqHeaderPageDisplay(headerId: 2, sellingMtn: 'MTN2', billingId: 'BILL2')
        ]
        def page = new PageImpl(headerList)
        def builder = new JsonBuilder()
        builder {
            pageNo 0
            pageSize 10
        }
        and:
        def requestBody = builder.toPrettyString()

        when:
        def response = mockMvc.perform(post('/bcq/declaration/list')
                .content(requestBody)
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.findAllHeaders(_ as PageableRequest) >> page
        response.status == 200
        def content = new JsonSlurper().parseText(response.contentAsString)
        content.data[0].headerId == headerList[0].headerId
        content.data[0].sellingMtn == headerList[0].sellingMtn
        content.data[0].billingId == headerList[0].billingId
        content.data[1].headerId == headerList[1].headerId
        content.data[1].sellingMtn == headerList[1].sellingMtn
        content.data[1].billingId == headerList[1].billingId
    }

    @Unroll
    def "get latest header id with #status header"() {
        when:
        def response = mockMvc.perform(get('/bcq/declaration/1/latest')
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.findHeader(1) >> header
        if (header != null) {
            1 * bcqService.findSameHeaders(_ as BcqHeader, _ as List, NOT_IN) >> sameHeaders
        }
        if (sameHeaders.empty) {
            response.status == 500
        } else {
            response.status == 200
        }
        response.contentAsString == contentAsString

        where:
        status   | header          | sameHeaders                  || contentAsString
        'null'   | null            | []                           || 'No declaration found with an ID of 1'
        'voided' | new BcqHeader() | []                           || 'Declaration was already voided'
        'valid'  | new BcqHeader() | [new BcqHeader(headerId: 2)] || '2'
    }

    @Unroll
    def "get header by header id with #status header"() {
        given:
        def validHeader = new BcqHeader(headerId: 1, uploadFile: new BcqUploadFile(transactionId: '000000',
                submittedDate: new Date()))
        def ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        def json = ow.writeValueAsString(validHeader);

        when:
        def response = mockMvc.perform(get('/bcq/declaration/1')
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.findHeader(1) >> header
        if (status == 'valid') {
            response.status == 200
            def content = new JsonSlurper().parseText(response.contentAsString)
            content == json

        } else {
            response.status == 500
            response.contentAsString == contentAsString
        }


        where:
        status   || contentAsString
        'null'   || 'No declaration found with an ID of 1'
        'voided' || 'Declaration was already voided'
        'valid'  || ''

        header << [
                null,
                new BcqHeader(status: VOID),
                new BcqHeader(headerId: 1, uploadFile: new BcqUploadFile(transactionId: '000000', submittedDate: new Date()))
        ]
    }

    @Unroll
    def "get same headers with #status header"() {
        given:
        def ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        def json = ow.writeValueAsString(sameHeaders);

        when:
        def response = mockMvc.perform(get('/bcq/declaration/1/same')
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.findHeader(1) >> header
        if (header != null) {
            1 * bcqService.findSameHeaders(_ as BcqHeader, _ as List, NOT_IN) >> sameHeaders
        }
        if (sameHeaders.empty) {
            response.status == 500
            response.contentAsString == contentAsString
        } else {
            response.status == 200
            println response.contentAsString
            def content = new JsonSlurper().parseText(response.contentAsString)
            content == json
        }

        where:
        status   | header          || contentAsString
        'null'   | null            || 'No declaration found with an ID of 1'
        'voided' | new BcqHeader() || 'Declaration was already voided'
        'valid'  | new BcqHeader() || ''

        sameHeaders << [
                [],
                [],
                [new BcqHeader(headerId: 2, uploadFile: new BcqUploadFile(transactionId: '000000',
                        submittedDate: new Date()))]
        ]
    }

    def "find data by header id"() {
        given:
        def dataDisplayList = [
                new BcqData(referenceMtn: 'MTN1', bcq: new BigDecimal(1)),
                new BcqData(referenceMtn: 'MTN2', bcq: new BigDecimal(2))
        ]

        when:
        def response = mockMvc.perform(get('/bcq/declaration/1/data')
                .contentType(APPLICATION_JSON_UTF8_VALUE)
                .header("Accept", APPLICATION_JSON_UTF8_VALUE))
                .andReturn().response

        then:
        1 * bcqService.findDataByHeaderId(1) >> dataDisplayList
        response.status == 200
        def content = new JsonSlurper().parseText(response.contentAsString)
        content[0].referenceMtn == dataDisplayList[0].referenceMtn
        new BigDecimal(content[0].bcq) == dataDisplayList[0].bcq
        content[1].referenceMtn == dataDisplayList[1].referenceMtn
        new BigDecimal(content[1].bcq) == dataDisplayList[1].bcq
    }

    @Configuration
    static class Config {
        def factory = new DetachedMockFactory()

        @Bean
        def bcqDisplayResource() {
            new BcqDisplayResource(bcqService())
        }

        @Bean
        def bcqService() {
            factory.Mock(BcqService)
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
