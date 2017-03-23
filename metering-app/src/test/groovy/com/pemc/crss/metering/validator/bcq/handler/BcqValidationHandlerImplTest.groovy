package com.pemc.crss.metering.validator.bcq.handler

import com.pemc.crss.commons.cache.service.CacheConfigService
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails
import com.pemc.crss.metering.resource.template.ResourceTemplate
import com.pemc.crss.metering.validator.bcq.*
import com.pemc.crss.metering.validator.bcq.handler.impl.BcqValidationHandlerImpl
import com.pemc.crss.metering.validator.bcq.helper.BcqPopulator
import spock.lang.Shared
import spock.lang.Specification

import static com.pemc.crss.metering.constants.BcqValidationError.*
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDate
import static com.pemc.crss.metering.validator.bcq.helper.ValidationHelperTestUtils.readCsv

class BcqValidationHandlerImplTest extends Specification {

    def csvValidator = Mock(CsvValidator);
    def headerListValidator = Mock(HeaderListValidator);
    def specialEventValidator = Mock(SpecialEventValidator);
    def billingIdValidator = Mock(BillingIdValidator);
    def crssSideValidator = Mock(CrssSideValidator);
    def resubmissionValidator = Mock(ResubmissionValidator);
    def overrideValidator = Mock(OverrideValidator);
    def resourceTemplate = Mock(ResourceTemplate);
    def configService = Mock(CacheConfigService );
    def sut
    List csv

    @Shared
    List headerList
    @Shared
    def acceptedValidationResult

    def setup() {
        sut = new BcqValidationHandlerImpl(
                csvValidator,
                headerListValidator,
                specialEventValidator,
                billingIdValidator,
                crssSideValidator,
                resubmissionValidator,
                overrideValidator,
                resourceTemplate,
                configService
        )
        csv = readCsv('bcq_file_valid')
        headerList = new BcqPopulator().populate(csv)
        acceptedValidationResult = new BcqValidationResult<>(ACCEPTED, null, headerList)
    }

    def "process and validate with failed validation in csv validator"() {
        given:
        def sellerDetails = new ParticipantSellerDetails('Gen1', 'GEN1')
        def errorMessage = new BcqValidationErrorMessage(EMPTY, [])
        def validationResult = new BcqValidationResult<>(status: REJECTED, errorMessage: errorMessage)

        when:
        def declaration = sut.processAndValidate(csv)

        then:
        1 * resourceTemplate.get(_ as String, ParticipantSellerDetails.class) >> sellerDetails
        1 * csvValidator.validate(csv) >> validationResult
        0 * headerListValidator.validate(_ as List) >> validationResult
        declaration.validationResult == validationResult
    }

    def "process and validate with failed validation in header list validator"() {
        given:
        def csv = readCsv('bcq_file_valid')
        def sellerDetails = new ParticipantSellerDetails('Gen1', 'GEN1')
        def errorMessage = new BcqValidationErrorMessage(INCOMPLETE_ENTRIES, [])
        def validationResult = new BcqValidationResult<>(status: REJECTED, errorMessage: errorMessage)

        when:
        def declaration = sut.processAndValidate(csv)

        then:
        1 * resourceTemplate.get(_ as String, ParticipantSellerDetails.class) >> sellerDetails
        1 * csvValidator.validate(csv) >> acceptedValidationResult
        1 * headerListValidator.validate(_ as List) >> validationResult
        0 * billingIdValidator.validate(_ as List)
        declaration.validationResult == validationResult
    }

    def "process and validate with failed validation in billing id validator"() {
        given:
        def csv = readCsv('bcq_file_valid')
        def sellerDetails = new ParticipantSellerDetails('Gen1', 'GEN1')
        def errorMessage = new BcqValidationErrorMessage(BILLING_ID_NOT_EXIST, [])
        def validationResult = new BcqValidationResult<>(status: REJECTED, errorMessage: errorMessage)

        when:
        def declaration = sut.processAndValidate(csv)

        then:
        1 * resourceTemplate.get(_ as String, ParticipantSellerDetails.class) >> sellerDetails
        1 * csvValidator.validate(csv) >> acceptedValidationResult
        1 * headerListValidator.validate(_ as List) >> acceptedValidationResult
        1 * billingIdValidator.validate(_ as List) >> validationResult
        0 * crssSideValidator.validate(_ as List, sellerDetails)
        declaration.validationResult == validationResult
    }

    def "process and validate with failed validation in crss side validator"() {
        given:
        def csv = readCsv('bcq_file_valid')
        def sellerDetails = new ParticipantSellerDetails('Gen1', 'GEN1')
        def errorMessage = new BcqValidationErrorMessage(SELLING_MTN_NOT_OWNED, [])
        def validationResult = new BcqValidationResult<>(status: REJECTED, errorMessage: errorMessage)

        when:
        def declaration = sut.processAndValidate(csv)

        then:
        1 * resourceTemplate.get(_ as String, ParticipantSellerDetails.class) >> sellerDetails
        1 * csvValidator.validate(csv) >> acceptedValidationResult
        1 * headerListValidator.validate(_ as List) >> acceptedValidationResult
        1 * billingIdValidator.validate(_ as List) >> acceptedValidationResult
        1 * crssSideValidator.validate(_ as List, sellerDetails) >> validationResult
        0 * resubmissionValidator.validate(_ as List, sellerDetails.shortName)
        declaration.validationResult == validationResult
    }

    def "process and validate with failed validation in resubmission validator"() {
        given:
        def csv = readCsv('bcq_file_valid')
        def sellerDetails = new ParticipantSellerDetails('Gen1', 'GEN1')
        def errorMessage = new BcqValidationErrorMessage(INCOMPLETE_RESUBMISSION_ENTRIES, [])
        def validationResult = new BcqValidationResult<>(status: REJECTED, errorMessage: errorMessage)

        when:
        def declaration = sut.processAndValidate(csv)

        then:
        1 * resourceTemplate.get(_ as String, ParticipantSellerDetails.class) >> sellerDetails
        1 * csvValidator.validate(csv) >> acceptedValidationResult
        1 * headerListValidator.validate(_ as List) >> acceptedValidationResult
        1 * billingIdValidator.validate(_ as List) >> acceptedValidationResult
        1 * crssSideValidator.validate(_ as List, sellerDetails) >> acceptedValidationResult
        1 * resubmissionValidator.validate(_ as List, sellerDetails.shortName) >> validationResult
        declaration.validationResult == validationResult
    }

    def "process and validate with accepted validation"() {
        given:
        def csv = readCsv('bcq_file_valid')
        def sellerDetails = new ParticipantSellerDetails('Gen1', 'GEN1')

        when:
        def declaration = sut.processAndValidate(csv)

        then:
        1 * resourceTemplate.get(_ as String, ParticipantSellerDetails.class) >> sellerDetails
        1 * csvValidator.validate(csv) >> acceptedValidationResult
        1 * headerListValidator.validate(_ as List) >> acceptedValidationResult
        1 * billingIdValidator.validate(_ as List) >> acceptedValidationResult
        1 * crssSideValidator.validate(_ as List, sellerDetails) >> acceptedValidationResult
        1 * resubmissionValidator.validate(_ as List, sellerDetails.shortName) >> acceptedValidationResult
        configService.getIntegerValueForKey('BCQ_INTERVAL', 15) >> 15
        declaration.validationResult.withProcessedObject(headerList) == acceptedValidationResult
        declaration.headerDetailsList.size == 1
        declaration.headerDetailsList[0].sellingMtn == 'MTN1'
        declaration.headerDetailsList[0].billingId == 'BILL1'
        declaration.headerDetailsList[0].tradingDate == parseDate('2017-02-25')
    }

}
