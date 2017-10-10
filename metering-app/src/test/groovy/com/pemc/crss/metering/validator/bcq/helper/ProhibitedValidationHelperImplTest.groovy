package com.pemc.crss.metering.validator.bcq.helper

import com.pemc.crss.metering.dto.bcq.BcqHeader
import com.pemc.crss.metering.dto.bcq.BcqProhibitedPair
import com.pemc.crss.metering.service.BcqService
import com.pemc.crss.metering.validator.bcq.helper.impl.ProhibitedValidationHelperImpl
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED

class ProhibitedValidationHelperImplTest extends Specification {

    def bcqService = Mock(BcqService)
    def sut = new ProhibitedValidationHelperImpl(bcqService)

    @Unroll
    def "validate prohibited, validation status must be #status"() {
        given:
        def headerList = [new BcqHeader(sellingMtn: 'MTN1', billingId: 'BILL1')]

        when:
        def result = sut.noProhibitedPairs().test(headerList)

        then:
        1 * bcqService.findAllEnabledProhibitedPairs() >> enabledProhibitedPairs
        result.status == status

        where:
        enabledProhibitedPairs << [
                [new BcqProhibitedPair(sellingMtn: 'MTN1', billingId: 'BILL1')],
                []
        ]

        status << [REJECTED, ACCEPTED]
    }

}
