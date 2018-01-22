package com.pemc.crss.metering.validator.bcq.impl

import com.pemc.crss.metering.dto.bcq.ParticipantBuyerDetails
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails
import com.pemc.crss.metering.dto.bcq.SellerWithItems
import com.pemc.crss.metering.resource.template.ResourceTemplate
import com.pemc.crss.metering.validator.bcq.BcqValidationResult
import com.pemc.crss.metering.validator.bcq.helper.BcqPopulator
import spock.lang.Specification
import spock.lang.Unroll

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.validator.bcq.helper.ValidationHelperTestUtils.readCsv

class CrssSideValidatorImplTest extends Specification {

    def resourceTemplate = Mock(ResourceTemplate)
    def sut = new CrssSideValidatorImpl(resourceTemplate)

    @Unroll
    def "validate with validation result: #status"() {
        given:
        def csv = readCsv('bcq_file_valid')
        def headerList = new BcqPopulator().populate(csv)
        def sellerDetails = new ParticipantSellerDetails('Gen1', 'GEN1','')
        def buyingParticipantShortName = 'PDU1'
        def crssSideResult = new BcqValidationResult(status: status, errorMessage: null,
                processedObject: [new ParticipantBuyerDetails(shortName: buyingParticipantShortName)])
        headerList*.buyingParticipantShortName = buyingParticipantShortName

        when:
        def result = sut.validate(headerList, sellerDetails)

        then:
        1 * resourceTemplate.post(_ as String, BcqValidationResult.class, _ as List) >> crssSideResult
        result.status == status

        where:
        predicateResult || status
        true            || ACCEPTED
        false           || REJECTED
    }

    @Unroll
    def "validate for settlement with validation result: #status"() {
        given:
        def csv = readCsv('bcq_file_valid')
        def headerList = new BcqPopulator().populate(csv)
        def sellerDetails = new ParticipantSellerDetails('Gen1', 'GEN1', '')
        def buyingParticipantShortName = 'PDU1'
        def crssSideResult = new BcqValidationResult(status: status, errorMessage: null,
                processedObject: [new ParticipantBuyerDetails(shortName: buyingParticipantShortName)])
        headerList*.buyingParticipantShortName = buyingParticipantShortName

        when:
        def result = sut.validateBySettlement(headerList, sellerDetails)

        then:
        1 * resourceTemplate.post(_ as String, BcqValidationResult.class, _ as SellerWithItems) >> crssSideResult
        result.status == status

        where:
        predicateResult || status
        true            || ACCEPTED
        false           || REJECTED
    }

}
