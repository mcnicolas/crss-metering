package com.pemc.crss.metering.validator.bcq.impl

import com.pemc.crss.metering.resource.template.ResourceTemplate
import com.pemc.crss.metering.validator.bcq.helper.BcqPopulator
import com.pemc.crss.metering.validator.bcq.helper.BillingIdValidationHelper
import com.pemc.crss.metering.validator.bcq.validation.BillingIdValidation
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.validator.bcq.helper.ValidationHelperTestUtils.readCsv

class BillingIdValidatorImplTest extends Specification {

    def validationHelper = Mock(BillingIdValidationHelper)
    def resourceTemplate = Mock(ResourceTemplate)
    def sut = new BillingIdValidatorImpl(validationHelper, resourceTemplate)

    @Unroll
    def "validate with validation result: #status"() {
        given:
        def predicate = { p1 -> predicateResult }
        def validation = new BillingIdValidation(predicate, null)
        def csv = readCsv('bcq_file_valid')
        def headerList = new BcqPopulator().populate(csv)

        when:
        def result = sut.validate(headerList)

        then:
        1 * validationHelper.validBillingIds(headerList.get(0).getTradingDate()) >> validation
        1 * resourceTemplate.get(_ as String, List.class) >> ['SHORTNAME1']
        result.status == status

        where:
        predicateResult || status
        true            || ACCEPTED
        false           || REJECTED
    }

}
