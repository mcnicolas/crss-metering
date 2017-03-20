package com.pemc.crss.metering.validator.bcq.helper

import com.pemc.crss.metering.dto.bcq.BcqHeader
import com.pemc.crss.metering.service.BcqService
import com.pemc.crss.metering.validator.bcq.helper.impl.ResubmissionValidationHelperImpl
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.BcqValidationError.INCOMPLETE_RESUBMISSION_ENTRIES
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.utils.DateTimeUtils.now

class ResubmissionValidationHelperImplTest extends Specification {

    def bcqService = Mock(BcqService)
    def validationHelper = new ResubmissionValidationHelperImpl(bcqService)

    @Unroll
    def 'validate resubmission, validation status must be #status'() {
        when:
        def result = validationHelper.validResubmission(_ as String, now()).test([])

        then:
        1 * bcqService.findHeadersOfParticipantByTradingDate(_ as String, _ as Date) >> currentHeaderList
        currentHeaderList.size() * bcqService.isHeaderInList(_ as BcqHeader, _ as List) >> isHeaderInList
        result.status == status
        if (result.errorMessage) {
            result.errorMessage.validationError == INCOMPLETE_RESUBMISSION_ENTRIES
        }

        where:

        currentHeaderList << [
                [new BcqHeader(headerId: 1)],
                [new BcqHeader(headerId: 1), new BcqHeader(headerId: 2)]
        ]

        isHeaderInList || status
        true           || ACCEPTED
        false          || REJECTED
    }

}
