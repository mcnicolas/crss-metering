package com.pemc.crss.metering.validator.bcq.impl

import com.pemc.crss.metering.validator.bcq.helper.BcqPopulator
import com.pemc.crss.metering.validator.bcq.helper.SpecialEventValidationHelper
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.validator.bcq.helper.ValidationHelperTestUtils.readCsv

class SpecialEventValidatorImplTest extends Specification {

    def validationHelper = Mock(SpecialEventValidationHelper)
    def sut = new SpecialEventValidatorImpl(validationHelper)

    @Unroll
    def "validate with validation result: #status"() {
        given:
        def predicate = { p1 -> predicateResult }
        def validation = new HeaderListValidation(predicate, null)
        def csv = readCsv('bcq_file_valid')
        def headerList = new BcqPopulator().populate(csv)
        def sellingParticipant = 'SELLER1'

        when:
        def result = sut.validate(headerList, sellingParticipant)

        then:
        1 * validationHelper.validSpecialEventUpload(sellingParticipant) >> validation
        result.status == status

        where:
        predicateResult || status
        true            || ACCEPTED
        false           || REJECTED
    }
}
