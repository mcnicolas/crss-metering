package com.pemc.crss.metering.validator.bcq.helper

import com.pemc.crss.metering.dto.bcq.BcqHeader
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventParticipant
import com.pemc.crss.metering.service.BcqService
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.BcqUpdateType.MANUAL_OVERRIDE
import static com.pemc.crss.metering.constants.BcqValidationError.*
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.utils.DateTimeUtils.now
import static org.apache.commons.lang3.time.DateUtils.addDays

class SpecialEventValidationHelperTest extends Specification {

    def bcqService = Mock(BcqService)
    def validationHelper = new SpecialEventValidationHelper(bcqService)

    @Unroll
    def 'validate special event submission, validation status must be #status with an error: #validationError'() {
        when:
        def result = validationHelper.validSpecialEventUpload(_ as String).test(headerList)

        then:
        1 * bcqService.findEventParticipantsByTradingDate(_ as Date) >> participants
        (0..1) * bcqService.findAllHeaders(_ as Map) >> currentHeaderList
        (0..1) * bcqService.findEventDeadlineDateByTradingDateAndParticipant(_ as Date, _ as String) >> deadlineDate
        result.status == status
        if (result.errorMessage) {
            result.errorMessage.validationError == validationError
        }

        where:
        headerList << [
                [new BcqHeader(
                        tradingDate: now()
                )],
                [new BcqHeader(
                        buyingParticipantName: 'PDU',
                        buyingParticipantShortName: 'PDU',
                        tradingDate: now()
                )],
                [new BcqHeader(
                        buyingParticipantName: 'PDU',
                        buyingParticipantShortName: 'PDU',
                        tradingDate: now()
                )],
                [new BcqHeader(
                        buyingParticipantName: 'PDU',
                        buyingParticipantShortName: 'PDU',
                        tradingDate: now()
                )],
                [new BcqHeader(
                        buyingParticipantName: 'PDU',
                        buyingParticipantShortName: 'PDU',
                        tradingDate: now()
                )]
        ]

        currentHeaderList << [
                [],
                [],
                [new BcqHeader(
                    updatedVia: MANUAL_OVERRIDE
                )],
                [],
                []
        ]

        participants << [
                [],
                [new BcqSpecialEventParticipant(participantName: 'PBU', shortName: 'PBU')],
                [new BcqSpecialEventParticipant(participantName: 'PDU', shortName: 'PDU')],
                [new BcqSpecialEventParticipant(participantName: 'PDU', shortName: 'PDU')],
                [new BcqSpecialEventParticipant(participantName: 'PDU', shortName: 'PDU')]
        ]

        deadlineDate             || validationError                           | status
        now()                    || NO_SPECIAL_EVENT_FOUND                    | REJECTED
        now()                    || PARTICIPANTS_NOT_PRESENT_IN_SPECIAL_EVENT | REJECTED
        now()                    || OVERRIDDEN_ENTRIES                        | REJECTED
        now()                    || DEADLINE_DATE_PASSED                      | REJECTED
        addDays(now(), 1)        || null                                      | ACCEPTED
    }

}
