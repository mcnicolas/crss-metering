package com.pemc.crss.metering.validator.bcq.impl

import com.pemc.crss.metering.validator.bcq.helper.BcqPopulator
import com.pemc.crss.metering.validator.bcq.helper.ProhibitedValidationHelper
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.validator.bcq.helper.ValidationHelperTestUtils.readCsv

class ProhibitedValidatorImplTest extends Specification {

    def validationHelper = Mock(ProhibitedValidationHelper)
    def sut = new ProhibitedValidatorImpl(validationHelper)

    @Unroll
    def "validate with validation result: #status"() {
        given:
        def predicate = { p1 -> predicateResult }
        def validation = new HeaderListValidation(predicate, null)
        def csv = readCsv('bcq_file_valid')
        def headerList = new BcqPopulator().populate(csv)

        when:
        def result = sut.validate(headerList)

        then:
        1 * validationHelper.noProhibitedPairs() >> validation
        result.status == status

        where:
        predicateResult || status
        true            || ACCEPTED
        false           || REJECTED
    }

}
