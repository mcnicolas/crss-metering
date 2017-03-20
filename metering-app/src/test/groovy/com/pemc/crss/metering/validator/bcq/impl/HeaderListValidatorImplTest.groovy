package com.pemc.crss.metering.validator.bcq.impl

import com.pemc.crss.commons.cache.service.CacheConfigService
import com.pemc.crss.metering.validator.bcq.helper.BcqPopulator
import com.pemc.crss.metering.validator.bcq.helper.HeaderListValidationHelper
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.validator.bcq.helper.ValidationHelperTestUtils.readCsv

class HeaderListValidatorImplTest extends Specification {

    def configService = Mock(CacheConfigService);
    def validationHelper = Mock(HeaderListValidationHelper)
    def sut = new HeaderListValidatorImpl(configService, validationHelper)

    @Unroll
    def "validate with validation result: #status"() {
        given:
        def tradingDateConfig = 1;
        def predicate = { p1 -> predicateResult }
        def validation = new HeaderListValidation(predicate, null)
        def headerList = new BcqPopulator().populate(readCsv('bcq_file_valid'))

        when:
        def result = sut.validate(headerList)

        then:
        1 * configService.getIntegerValueForKey("BCQ_ALLOWABLE_TRADING_DATE", 1) >> tradingDateConfig
        1 * validationHelper.validHeaderList(tradingDateConfig) >> validation
        result.status == status

        where:
        predicateResult || status
        true            || ACCEPTED
        false           || REJECTED
    }

    @Unroll
    def "validate for settlement with validation result: #status"() {
        given:
        def tradingDate = new Date();
        def predicate = { p1 -> predicateResult }
        def validation = new HeaderListValidation(predicate, null)
        def headerList = new BcqPopulator().populate(readCsv('bcq_file_valid'))

        when:
        def result = sut.validateForSettlement(headerList, tradingDate)

        then:
        1 * validationHelper.validHeaderList(tradingDate) >> validation
        result.status == status

        where:
        predicateResult || status
        true            || ACCEPTED
        false           || REJECTED
    }

}
