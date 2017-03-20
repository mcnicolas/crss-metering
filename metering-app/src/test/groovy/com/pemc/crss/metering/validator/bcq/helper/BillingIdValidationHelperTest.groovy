package com.pemc.crss.metering.validator.bcq.helper

import com.pemc.crss.metering.dto.bcq.BillingIdShortNamePair
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED

class BillingIdValidationHelperTest extends Specification {

    def validationHelper = new BillingIdValidationHelper()

    @Unroll
    def 'validate billing id, validation status must be #status'() {
        given:
        def validBillingIds = [
                new BillingIdShortNamePair(billingId: 'BILL1', tradingParticipantShortName: shortNames)
        ]

        when:
        def result = validationHelper.validBillingIds(new Date()).test(validBillingIds);

        then:
        result.status == status

        where:
        shortNames           || status
        ['BUYER1']           || ACCEPTED
        ['BUYER1', 'BUYER1'] || ACCEPTED
        ['BUYER1', 'BUYER2'] || REJECTED
    }

}
