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
                new BillingIdShortNamePair(billingId: 'BILL1', tradingParticipantShortName: 'BUYER1'),
                new BillingIdShortNamePair(billingId: 'BILL2', tradingParticipantShortName: 'BUYER2')
        ]

        when:
        def result = validationHelper.validBillingIds(validBillingIds).test(billingIds);

        then:
        result.status == status

        where:
        billingIds                  || status
        ['BILL1']                   || ACCEPTED
        ['BILL1', 'BILL2']          || ACCEPTED
        ['BILL1', 'BILL2', 'BILL3'] || REJECTED
    }

}
