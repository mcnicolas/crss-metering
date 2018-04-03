package com.pemc.crss.metering.service

import com.google.common.collect.ImmutableMap
import com.pemc.crss.commons.cache.service.CacheConfigService
import com.pemc.crss.commons.web.dto.datatable.PageableRequest
import com.pemc.crss.metering.constants.BcqStatus
import com.pemc.crss.metering.dao.BcqDao
import com.pemc.crss.metering.dao.query.ComparisonOperator
import com.pemc.crss.metering.dto.bcq.*
import com.pemc.crss.metering.dto.bcq.specialevent.BcqEventValidationData
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEvent
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventParticipant
import com.pemc.crss.metering.resource.template.ResourceTemplate
import com.pemc.crss.metering.service.exception.InvalidStateException
import com.pemc.crss.metering.service.exception.OldRecordException
import com.pemc.crss.metering.service.exception.PairExistsException
import com.pemc.crss.metering.validator.bcq.BcqValidationResult
import org.springframework.data.domain.PageImpl
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.BcqStatus.*
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.dao.query.ComparisonOperator.IN
import static com.pemc.crss.metering.utils.DateTimeUtils.now
import static com.pemc.crss.metering.utils.DateTimeUtils.parseDate

class BcqServiceImplTest extends Specification {

    def bcqDao = Mock(BcqDao);
    def bcqNotificationManager = Mock(BcqNotificationManager);
    def configService = Mock(CacheConfigService);
    def resourceTemplate = Mock(ResourceTemplate);
    def sut = new BcqServiceImpl(bcqDao, bcqNotificationManager, configService, resourceTemplate)

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
        isResubmission || status
        true           || ACCEPTED
        false          || ACCEPTED
        true           || REJECTED
        false          || REJECTED
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
                )],
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

        sameHeaders << [
                [new BcqHeader(headerId: 2L)],
                [],
                [new BcqHeader(headerId: 1L)]
        ]

        status    || exception
        CONFIRMED || OldRecordException
        NULLIFIED || InvalidStateException
        CANCELLED || InvalidStateException
    }

    @Unroll
    def "request for cancellation, with status #status"() {
        when:
        sut.requestForCancellation(1L)

        then:
        1 * bcqDao.findHeader(_ as Long) >> new BcqHeader(headerId: 1L, status: status)
        1 * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >> sameHeaders
        1 * bcqDao.updateHeaderStatusBySettlement(_ as Long, FOR_APPROVAL_CANCEL)
        1 * bcqNotificationManager.sendSettlementUpdateStatusNotification(_ as BcqHeader)

        where:
        status << [CONFIRMED, SETTLEMENT_READY]
        sameHeaders << [
                [new BcqHeader(headerId: 1L)],
                [new BcqHeader(headerId: 1L)]
        ]
    }

    @Unroll
    def "request for cancellation, with status #status, then throw exception"() {
        when:
        sut.requestForCancellation(1L)

        then:
        1 * bcqDao.findHeader(_ as Long) >> new BcqHeader(headerId: 1L, status: status)
        1 * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >> sameHeaders
        thrown(InvalidStateException)

        where:
        status << [FOR_APPROVAL_CANCEL, FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATE]
        sameHeaders << [
                [new BcqHeader(headerId: 1L)],
                [new BcqHeader(headerId: 1L)],
                [new BcqHeader(headerId: 1L)]
        ]
    }

    @Unroll
    def "approve declaration, with status #status"() {
        when:
        sut.approve(1L)

        then:
        1 * bcqDao.findHeader(_ as Long) >> new BcqHeader(headerId: 1L, status: status)
        if (status == FOR_APPROVAL_CANCEL) {
            1 * bcqDao.updateHeaderStatus(_ as Long, _ as BcqStatus)
            1 * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >> sameHeaders
        } else {
            2 * bcqDao.updateHeaderStatus(_ as Long, _ as BcqStatus)
            2 * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >> sameHeaders
        }

        1 * bcqNotificationManager.sendApprovalNotification(_ as BcqHeader)

        where:
        status << [FOR_APPROVAL_CANCEL, FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATE]
        sameHeaders << [
                [new BcqHeader(headerId: 1L)],
                [new BcqHeader(headerId: 1L)],
                [new BcqHeader(headerId: 1L)]
        ]
    }

    @Unroll
    def "approve declaration, with status #status, then throw exception"() {
        when:
        sut.approve(1L)

        then:
        1 * bcqDao.findHeader(_ as Long) >> new BcqHeader(headerId: 1L, status: status)
        1 * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >> sameHeaders
        thrown(exception)

        where:
        sameHeaders << [
                [new BcqHeader(headerId: 1L)],
                [new BcqHeader(headerId: 1L)],
                [new BcqHeader(headerId: 2L)]
        ]

        status              || exception
        CANCELLED           || InvalidStateException
        SETTLEMENT_READY    || InvalidStateException
        FOR_APPROVAL_CANCEL || OldRecordException
    }

    def "process unconfirmed headers"() {
        given:
        def tradingDate = now()
        def unconfirmedHeaders = [
                new BcqHeader(headerId: 1L, status: NOT_CONFIRMED, tradingDate: tradingDate,
                        sellingParticipantShortName: 'GEN1', buyingParticipantShortName: 'PDU1'),
                new BcqHeader(headerId: 2L, status: NOT_CONFIRMED, tradingDate: tradingDate,
                        sellingParticipantShortName: 'GEN1', buyingParticipantShortName: 'PDU1'),
        ]
        when:
        sut.processUnconfirmedHeaders()

        then:
        1 * bcqDao.findAllHeaders(_ as Map) >> unconfirmedHeaders
        unconfirmedHeaders.size() * bcqDao.updateHeaderStatus(_ as Long, NOT_CONFIRMED)
        1 * bcqNotificationManager.sendUnprocessedNotification(_ as List, NOT_CONFIRMED)
    }

    def "process unnullified headers"() {
        given:
        def tradingDate = now()
        def unnullifiedHeaders = [
                new BcqHeader(headerId: 1L, status: FOR_NULLIFICATION, tradingDate: tradingDate,
                        sellingParticipantShortName: 'GEN1', buyingParticipantShortName: 'PDU1'),
                new BcqHeader(headerId: 2L, status: FOR_NULLIFICATION, tradingDate: tradingDate,
                        sellingParticipantShortName: 'GEN1', buyingParticipantShortName: 'PDU2'),
        ]
        when:
        sut.processUnnullifiedHeaders()

        then:
        1 * bcqDao.findAllHeaders(_ as Map) >> unnullifiedHeaders
        unnullifiedHeaders.size() * bcqDao.findSameHeaders(_ as BcqHeader, _ as List, _ as ComparisonOperator) >> []
        unnullifiedHeaders.size() * bcqDao.updateHeaderStatus(_ as Long, CONFIRMED)
        2 * bcqNotificationManager.sendUnprocessedNotification(_ as List, CONFIRMED)
    }

    def "process headers to settlement ready"() {
        given:
        def headerIdsToUpdate = [1L, 2L]

        when:
        sut.processHeadersToSettlementReady()

        then:
        1 * configService.getIntegerValueForKey(_ as String, _ as Integer) >> 2
        1 * bcqDao.selectByStatusAndDeadlineDatePlusDays(_ as BcqStatus, _ as Integer) >> headerIdsToUpdate
        headerIdsToUpdate.size() * bcqDao.updateHeaderStatus(_ as Long, SETTLEMENT_READY)
    }

    def "find all special events"() {
        given:
        def specialEvents = []
        for (int i = 0; i < new Random().nextInt(100); i ++) {
            specialEvents.add(new BcqSpecialEvent(eventId: i))
        }

        when:
        def result = sut.findAllSpecialEvents()

        then:
        1 * bcqDao.findAllSpecialEvents() >> specialEvents
        result.size() == specialEvents.size()
    }

    def "save special event"() {
        given:
        def specialEvent = new BcqSpecialEvent(
                tradingParticipants: [
                        new BcqSpecialEventParticipant(shortName: 'PDU1'),
                        new BcqSpecialEventParticipant(shortName: 'PDU2')
                ],
                tradingDates: [
                        parseDate('12/12/2012'),
                        parseDate('12/13/2012')
                ]
        )

        when:
        def result = sut.saveSpecialEvent(specialEvent)

        then:
        1 * bcqDao.checkDuplicateParticipantTradingDates(['PDU1', 'PDU2'], specialEvent.getTradingDates()) >> []
        1 * bcqDao.saveSpecialEvent(_ as BcqSpecialEvent) >> 1L
        result == 1L
    }

    def "save special event, throw error"() {
        given:
        def specialEvent = new BcqSpecialEvent(
                tradingParticipants: [
                        new BcqSpecialEventParticipant(shortName: 'PDU1')
                ],
                tradingDates: [
                        parseDate('12/12/2012'),
                        parseDate('12/13/2012')
                ]
        )

        when:
        sut.saveSpecialEvent(specialEvent)

        then:
        1 * bcqDao.checkDuplicateParticipantTradingDates(['PDU1'], specialEvent.getTradingDates()) >> [BcqEventValidationData.newInstance()]
        thrown(RuntimeException)
    }

    def "find event participants by trading date"() {
        given:
        def tradingDate = now()

        when:
        def result = sut.findEventParticipantsByTradingDate(tradingDate)

        then:
        1 * bcqDao.findEventParticipantsByTradingDate(tradingDate) >> [new BcqSpecialEventParticipant(shortName: 'PDU1')]
        result.size() == 1
        result[0].shortName == 'PDU1'
    }

    def "find event deadline date by trading date and participant"() {
        given:
        def shortName = 'PDU1'
        def tradingDate = now()

        when:
        def result = sut.findEventDeadlineDateByTradingDateAndParticipant(tradingDate, shortName)

        then:
        1 * bcqDao.findEventDeadlineDateByTradingDateAndParticipant(tradingDate, shortName) >> parseDate('12/12/2012')
        result == parseDate('12/12/2012')
    }

    def "find all prohibited pairs by pageable request"() {
        when:
        def result = sut.findAllProhibitedPairs(PageableRequest.newInstance())

        then:
        1 * bcqDao.findAllProhibitedPairs(_ as PageableRequest) >>
                new PageImpl<>([new BcqProhibitedPairPageDisplay(id: 1L)])
        result.content.size() == 1
        result.content[0].id == 1L
    }

    def "save prohibited pair"() {
        given:
        def prohibitedPair = new BcqProhibitedPair(sellingMtn: 'MTN1', billingId: 'BILL1')

        when:
        def id = sut.saveProhibitedPair(prohibitedPair)

        then:
        1 * bcqDao.findAllEnabledProhibitedPairs() >> []
        1 * bcqDao.saveProhibitedPair(_ as BcqProhibitedPair) >> 1L
        id == 1L
    }

    def "save prohibited pair with exception"() {
        given:
        def prohibitedPair = new BcqProhibitedPair(sellingMtn: 'MTN1', billingId: 'BILL1')

        when:
        sut.saveProhibitedPair(prohibitedPair)

        then:
        1 * bcqDao.findAllEnabledProhibitedPairs() >> [prohibitedPair]
        0 * bcqDao.saveProhibitedPair(_ as BcqProhibitedPair)
        thrown(PairExistsException)
    }

    def "disable prohibited pair"() {
        when:
        sut.disableProhibitedPair(1L)

        then:
        1 * bcqDao.disableProhibitedPair(1L)
    }

}
