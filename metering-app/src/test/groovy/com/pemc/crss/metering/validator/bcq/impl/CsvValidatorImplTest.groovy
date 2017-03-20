package com.pemc.crss.metering.validator.bcq.impl

import com.pemc.crss.commons.cache.service.CacheConfigService
import com.pemc.crss.metering.validator.bcq.helper.CsvValidationHelper
import com.pemc.crss.metering.validator.bcq.validation.CsvValidation
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.validator.bcq.helper.ValidationHelperTestUtils.readCsv

class CsvValidatorImplTest extends Specification {

    def configService = Mock(CacheConfigService);
    def validationHelper = Mock(CsvValidationHelper);
    def sut = new CsvValidatorImpl(configService, validationHelper)

    @Unroll
    def "validate with validation result: #status"() {
        given:
        def intervalConfig = 15
        def predicate = { p1 -> predicateResult }
        def validation = new CsvValidation(predicate, null)
        def csv = readCsv('bcq_file_valid')

        when:
        def result = sut.validate(csv)

        then:
        1 * configService.getIntegerValueForKey("BCQ_INTERVAL", 15) >> 15;
        1 * validationHelper.validCsv(intervalConfig) >> validation
        result.status == status

        where:
        predicateResult || status
        true            || ACCEPTED
        false           || REJECTED
    }

}
