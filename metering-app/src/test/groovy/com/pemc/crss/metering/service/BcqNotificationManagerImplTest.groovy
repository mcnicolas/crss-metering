package com.pemc.crss.metering.service

import com.pemc.crss.metering.dto.bcq.*
import com.pemc.crss.metering.resource.template.ResourceTemplate
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage
import com.pemc.crss.metering.validator.bcq.BcqValidationResult
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.BcqStatus.CANCELLED
import static com.pemc.crss.metering.constants.BcqStatus.CONFIRMED
import static com.pemc.crss.metering.constants.BcqStatus.FOR_APPROVAL_CANCEL
import static com.pemc.crss.metering.constants.BcqStatus.FOR_APPROVAL_NEW
import static com.pemc.crss.metering.constants.BcqStatus.FOR_APPROVAL_UPDATE
import static com.pemc.crss.metering.constants.BcqStatus.NOT_CONFIRMED
import static com.pemc.crss.metering.constants.BcqStatus.NULLIFIED
import static com.pemc.crss.metering.constants.BcqValidationError.EMPTY
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate

class BcqNotificationManagerImplTest extends Specification {

    def eventPublisher = Mock(ApplicationEventPublisher)
    def resourceTemplate = Mock(ResourceTemplate)
    def sut = new BcqNotificationManagerImpl(eventPublisher, resourceTemplate)
    def headerList = [
            new BcqHeader(
                    headerId: 1,
                    sellingParticipantName: 'Gen1',
                    sellingParticipantShortName: 'GEN1',
                    buyingParticipantName: 'Pdu1',
                    buyingParticipantShortName: 'PDU1',
                    tradingDate: parseDate('2017-03-21'),
                    uploadFile: new BcqUploadFile(submittedDate: new Date()),
                    dataList: [new BcqData()]
            ),
            new BcqHeader(
                    headerId: 2,
                    sellingParticipantName: 'Gen1',
                    sellingParticipantShortName: 'GEN1',
                    buyingParticipantName: 'Pbu1',
                    buyingParticipantShortName: 'PBU1',
                    tradingDate: parseDate('2017-03-21'),
                    uploadFile: new BcqUploadFile(submittedDate: new Date()),
                    dataList: [new BcqData(), new BcqData()],
                    exists: true
            )
    ]

    def setup() {
        Authentication authentication = Mock(Authentication) {
            getPrincipal() >> new LinkedHashMap<>(
                    userAuthentication: new LinkedHashMap<>(
                            principal: new LinkedHashMap<>(
                                    firstName: 'Billing',
                                    lastName: 'Settlement',
                                    userName: 'billing'
                            )
                    )
            )
        }

        SecurityContext securityContext = Mock(SecurityContext) {
            getAuthentication() >> authentication
        }
        SecurityContextHolder.setContext(securityContext);
    }

    def "send validation notification"() {
        given:
        def sellerDetails = new ParticipantSellerDetails('Gen1', 'GEN1')
        def errorMessage = new BcqValidationErrorMessage(EMPTY, [])
        def validationResult = new BcqValidationResult<>(status: REJECTED, errorMessage: errorMessage)
        def declaration = new BcqDeclaration(sellerDetails).withValidationResult(validationResult)
        def submittedDate = new Date()
        def userIds = [1, 2, 3].toSet()

        when:
        sut.sendValidationNotification(declaration, submittedDate)

        then:
        1 * resourceTemplate.get(_ as String, Set.class) >> userIds
        (1 + userIds.size()) * eventPublisher.publishEvent(_ as ApplicationEvent)
    }

    def "send upload notification"() {
        given:
        def userIds = [1, 2].toSet()

        when:
        sut.sendUploadNotification(headerList)

        then:
        (1 + headerList.size()) * resourceTemplate.get(_ as String, Set.class) >> userIds
        (userIds.size() + (headerList.size() * userIds.size())) * eventPublisher.publishEvent(_ as ApplicationEvent)
    }

    def "send settlement validation notification"() {
        given:
        def errorMessage = new BcqValidationErrorMessage(EMPTY, [])
        def validationResult = new BcqValidationResult<>(status: REJECTED, errorMessage: errorMessage)
        def declaration = new BcqDeclaration().withValidationResult(validationResult)
        def submittedDate = new Date()

        when:
        sut.sendSettlementValidationNotification(declaration, submittedDate)

        then:
        1 * eventPublisher.publishEvent(_ as ApplicationEvent)
    }

    def "send settlement upload notification"() {
        given:
        headerList[1].status = FOR_APPROVAL_UPDATE

        when:
        sut.sendSettlementUploadNotification(headerList)

        then:
        headerList.size() * eventPublisher.publishEvent(_ as ApplicationEvent)
    }

    @Unroll
    def "send update status notification with status: #status"() {
        given:
        def userIds = [1, 2].toSet()
        headerList[0].status = status

        when:
        sut.sendUpdateStatusNotification(headerList[0])

        then:
        1 * resourceTemplate.get(_ as String, Set.class) >> userIds
        userIds.size() * eventPublisher.publishEvent(_ as ApplicationEvent)

        where:
        status << [CANCELLED, CONFIRMED, NULLIFIED]
    }

    def "send settlement update status notification"() {
        when:
        sut.sendSettlementUpdateStatusNotification(headerList[0])

        then:
        1 * eventPublisher.publishEvent(_ as ApplicationEvent)
    }

    @Unroll
    def "send approval notification with status: #status"() {
        given:
        def userIds = [1, 2].toSet()
        headerList[0].status = status

        when:
        sut.sendApprovalNotification(headerList[0])

        then:
        2 * resourceTemplate.get(_ as String, Set.class) >> userIds
        (userIds.size() * 2) * eventPublisher.publishEvent(_ as ApplicationEvent)

        where:
        status << [FOR_APPROVAL_NEW, FOR_APPROVAL_UPDATE, FOR_APPROVAL_CANCEL]
    }

    @Unroll
    def "send unprocessed notification with status: #status"() {
        given:
        def userIds = [1, 2].toSet()
        headerList*.deadlineDate = new Date()

        when:
        sut.sendUnprocessedNotification(headerList, status)

        then:
        2 * resourceTemplate.get(_ as String, Set.class) >> userIds
        (userIds.size() * 2) * eventPublisher.publishEvent(_ as ApplicationEvent)

        where:
        status << [CONFIRMED, NOT_CONFIRMED]
    }

}
