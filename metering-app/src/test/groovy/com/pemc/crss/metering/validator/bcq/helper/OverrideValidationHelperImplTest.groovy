package com.pemc.crss.metering.validator.bcq.helper

import com.pemc.crss.metering.dto.bcq.BcqHeader
import com.pemc.crss.metering.service.BcqService
import com.pemc.crss.metering.validator.bcq.helper.impl.OverrideValidationHelperImpl
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.BcqValidationError.INCOMPLETE_OVERRIDE_ENTRIES
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.utils.DateTimeUtils.now

class OverrideValidationHelperImplTest extends Specification {

    def bcqService = Mock(BcqService)
    def validationHelper = new OverrideValidationHelperImpl(bcqService)

    @Unroll
    def 'validate resubmission, validation status must be #status'() {
        when:
        def result = validationHelper.validOverride(_ as String, now()).test([])

        then:
        1 * bcqService.findHeadersOfParticipantByTradingDate(_ as String, _ as Date) >> currentHeaderList
        currentHeaderList.size() * bcqService.isHeaderInList(_ as BcqHeader, _ as List) >> isHeaderInList
        result.status == status
        if (result.errorMessage) {
            result.errorMessage.validationError == INCOMPLETE_OVERRIDE_ENTRIES
        }

        where:

        currentHeaderList << [
                [new BcqHeader(headerId: 1, tradingDate: now())],
                [new BcqHeader(headerId: 1, tradingDate: now()), new BcqHeader(headerId: 2)]
        ]

        isHeaderInList || status
        true           || ACCEPTED
        false          || REJECTED
    }

}
