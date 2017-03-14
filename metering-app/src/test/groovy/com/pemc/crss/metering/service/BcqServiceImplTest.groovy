package com.pemc.crss.metering.service

import com.google.common.collect.ImmutableMap
import com.pemc.crss.commons.cache.service.CacheConfigService
import com.pemc.crss.commons.web.dto.datatable.PageableRequest
import com.pemc.crss.metering.dao.BcqDao
import com.pemc.crss.metering.dao.query.ComparisonOperator
import com.pemc.crss.metering.dto.bcq.*
import com.pemc.crss.metering.service.exception.InvalidStateException
import com.pemc.crss.metering.service.exception.OldRecordException
import com.pemc.crss.metering.validator.bcq.BcqValidationResult
import org.springframework.data.domain.PageImpl
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.BcqStatus.CANCELLED
import static com.pemc.crss.metering.constants.BcqStatus.CONFIRMED
import static com.pemc.crss.metering.constants.BcqStatus.NULLIFIED
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.dao.query.ComparisonOperator.IN
import static com.pemc.crss.metering.utils.DateTimeUtils.now
import static com.pemc.crss.metering.utils.DateTimeUtils.parseDate

class BcqServiceImplTest extends Specification {

    def bcqDao = Mock(BcqDao);
    def bcqNotificationManager = Mock(BcqNotificationManager);
    def configService = Mock(CacheConfigService);
    def sut = new BcqServiceImpl(bcqDao, bcqNotificationManager, configService)

    @Unroll
    def 'save declaration by seller with status: #status'() {
        given:
        def uploadFile = new BcqUploadFile(
                fileName: 'file1',
                validationStatus: status
        )
        def header = new BcqHeader(
                sellingMtn: 'MTN1',
                billingId: 'BILL1',
                tradingDate: now()
        )
        def headerList = [header]
        def declaration = new BcqDeclaration(
                uploadFileDetails: new BcqUploadFileDetails(uploadFile),
                sellerDetails: new ParticipantSellerDetails(name: 'Gen', shortName: 'GEN1'),
                headerDetailsList: [new BcqHeaderDetails(header)],
                validationResult: new BcqValidationResult(status: status),
                isResubmission: isResubmission
        )

        when:
        sut.saveDeclaration(declaration, false)

        then:
        if (status == ACCEPTED) {
            1 * bcqDao.saveUploadFile(_ as BcqUploadFile)
            1 * bcqDao.saveHeaders(_ as List, _ as Boolean) >> []
            if (isResubmission) {
                1 * bcqDao.findAllHeaders(_ as Map) >> headerList
            }
            1 * bcqNotificationManager.sendUploadNotification(_ as List)
        } else {
            1 * bcqNotificationManager.sendValidationNotification(_ as BcqDeclaration, _ as Date)
        }

        where:
        isResubmission << [true, false, true, false]
        status << [ACCEPTED, ACCEPTED, REJECTED, REJECTED]
    }

    @Unroll
    def 'save declaration by settlement with status: #status'() {
        given:
        def uploadFile = new BcqUploadFile(
                fileName: 'file1',
                validationStatus: status
        )
        def header = new BcqHeader(
                sellingMtn: 'MTN1',
                billingId: 'BILL1',
                tradingDate: parseDate('12/12/2012')
        )
        def declaration = new BcqDeclaration(
                uploadFileDetails: new BcqUploadFileDetails(uploadFile),
                sellerDetails: new ParticipantSellerDetails(name: 'Gen', shortName: 'GEN1'),
                headerDetailsList: [new BcqHeaderDetails(header)],
                validationResult: new BcqValidationResult(status: status)
        )

        when:
        sut.saveDeclaration(declaration, true)

        then:
        if (status == ACCEPTED) {
            1 * bcqDao.saveUploadFile(_ as BcqUploadFile)
            1 * bcqDao.saveHeaders(_ as List, _ as Boolean) >> []
            1 * bcqDao.findAllHeaders(_ as Map) >> headerList
            1 * bcqNotificationManager.sendSettlementUploadNotification(_ as List)
        } else {
            1 * bcqNotificationManager.sendSettlementValidationNotification(_ as BcqDeclaration, _ as Date)
        }

        where:
        status << [ACCEPTED, ACCEPTED, REJECTED, REJECTED]
        headerList << [
                [new BcqHeader(
                        sellingMtn: 'MTN1',
                        billingId: 'BILL1',
                        tradingDate: parseDate('12/12/2012')
                ), new BcqHeader(
                        sellingMtn: 'MTN2',
                        billingId: 'BILL2',
                        tradingDate: parseDate('12/12/2012')
                )
                ],
                [],
                [],
                []
        ]
    }

    def "find header"() {
        when:
        def result = sut.findHeader(1L)

        then:
        1 * bcqDao.findHeader(_ as Long) >> new BcqHeader(headerId: 1L)
        result.headerId == 1L
    }

    def "find all headers by map"() {
        when:
        def result = sut.findAllHeaders(ImmutableMap.of())

        then:
        1 * bcqDao.findAllHeaders(_ as Map) >> [new BcqHeader(headerId: 1L)]
        result.size() == 1
        result[0].headerId == 1L
    }

    def "find all headers by pageable request"() {
        when:
        def result = sut.findAllHeaders(PageableRequest.newInstance())

        then:
        1 * bcqDao.findAllHeaders(_ as PageableRequest) >>
                new PageImpl<>([new BcqHeader(headerId: 1L)])
        result.content.size() == 1
        result.content[0].headerId == 1L
    }

    def "find same headers"() {
        when:
        def result = sut.findSameHeaders(BcqHeader.newInstance(), [], IN)

        then:
        1 * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >>
                [new BcqHeader(headerId: 1L)]
        result[0].headerId == 1L
    }

    def "find headers of participant by tradingDate"() {
        when:
        def result = sut.findHeadersOfParticipantByTradingDate('GEN1', now())

        then:
        1 * bcqDao.findAllHeaders(_ as Map) >> [new BcqHeader(headerId: 1L)]
        result[0].headerId == 1L
    }

    def "find data by header id"() {
        when:
        def result = sut.findDataByHeaderId(1L)

        then:
        1 * bcqDao.findDataByHeaderId(_ as Long) >> [new BcqData(dataId: 1L), new BcqData(dataId: 2L)]
        result.size() == 2
        result[0].dataId == 1L
        result[1].dataId == 2L
    }

    @Unroll
    def "update header status to #status"() {
        when:
        sut.updateHeaderStatus(1L, status)

        then:
        2 * bcqDao.findHeader(_ as Long) >> header
        if (status == CONFIRMED) {
            2 * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >> sameHeaders
        } else {
            1 * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >> sameHeaders
        }
        1 * bcqNotificationManager.sendUpdateStatusNotification(_ as BcqHeader)

        where:
        header << [
                new BcqHeader(headerId: 1L),
                new BcqHeader(headerId: 1L),
                new BcqHeader(headerId: 1L, status: CONFIRMED)
        ]
        status << [CONFIRMED, NULLIFIED, CANCELLED]
        sameHeaders << [
                [new BcqHeader(headerId: 1L)],
                [new BcqHeader(headerId: 1L)],
                [new BcqHeader(headerId: 1L)]
        ]
    }

    @Unroll
    def "update header status, throw exception #exception"() {
        when:
        sut.updateHeaderStatus(1L, status)

        then:
        1 * bcqDao.findHeader(_ as Long) >> header
        1 * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >> sameHeaders
        thrown(exception)

        where:
        header << [
                new BcqHeader(headerId: 1L),
                new BcqHeader(headerId: 1L),
                new BcqHeader(headerId: 1L, status: CANCELLED)
        ]
        status << [CONFIRMED, NULLIFIED, CANCELLED]
        sameHeaders << [
                [new BcqHeader(headerId: 2L)],
                [],
                [new BcqHeader(headerId: 1L)]
        ]
        exception << [OldRecordException, InvalidStateException, InvalidStateException]
    }

}
