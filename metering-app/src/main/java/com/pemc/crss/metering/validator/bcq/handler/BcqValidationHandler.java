package com.pemc.crss.metering.validator.bcq.handler;

import com.pemc.crss.commons.cache.service.CacheConfigService;
import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.resource.template.ResourceTemplate;
import com.pemc.crss.metering.validator.bcq.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.constants.BcqInterval.FIVE_MINUTES_PERIOD;
import static com.pemc.crss.metering.constants.BcqValidationError.CLOSED_TRADING_DATE;
import static com.pemc.crss.metering.constants.BcqValidationError.NO_SPECIAL_EVENT_FOUND;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.valueOf;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqValidationHandler {

    private static final String SELLER_URL = "/reg/bcq/seller";
    private static final String VALIDATE_URL = "/reg/bcq/validate";
    private static final String SETTLEMENT_VALIDATE_URL = "/reg/bcq/settlement/validate";
    private static final String BILLING_VALIDATE_URL =
            "http://app.reg.settlement.pemc.com/billing/%s/tradingParticipants?date=%s";

    private final CsvValidator csvValidator;
    private final HeaderListValidator headerListValidator;
    private final SpecialEventValidator specialEventValidator;
    private final BillingIdValidator billingIdValidator;
    private final CrssSideValidator crssSideValidator;
    private final ResubmissionValidator resubmissionValidator;
    private final OverrideValidator overrideValidator;
    private final ResourceTemplate resourceTemplate;
    private final CacheConfigService configService;

    public BcqDeclaration processAndValidate(List<List<String>> csv) {
        ParticipantSellerDetails sellerDetails = getSellerDetails();
        BcqDeclaration declaration = new BcqDeclaration(sellerDetails);

        BcqValidationResult<List<BcqHeader>> result = csvValidator.validate(csv)
                .then(csvResult -> headerListValidator.validate(csvResult.getProcessedObject()))
                .then(headerListResult -> billingIdValidator.validate(headerListResult.getProcessedObject()))
                .then(billingIdResult -> crssSideValidator.validate(billingIdResult.getProcessedObject(), sellerDetails))
                .then(crssSideResult -> resubmissionValidator.validate(crssSideResult.getProcessedObject(),
                        sellerDetails.getShortName()));

        List<BcqHeader> headerList = result.getProcessedObject();
        List<BcqHeaderDetails> headerDetailsList = new ArrayList<>();
        if (result.getStatus() == ACCEPTED) {
            headerList = divideDataOfHeaderList(headerList);
            headerList.forEach(header -> headerDetailsList.add(new BcqHeaderDetails(header)));
            return declaration.withHeaderDetailsList(headerDetailsList);
        } else {
            if (result.getErrorMessage().getValidationError() == CLOSED_TRADING_DATE) {
                return processAndValidateForSpecialEvent(headerList, sellerDetails, result);
            }
            return declaration.withValidationResult(result);
        }

    }

    private BcqDeclaration processAndValidateForSpecialEvent(List<BcqHeader> headerList,
                                                             ParticipantSellerDetails sellerDetails,
                                                             BcqValidationResult<List<BcqHeader>> previousResult) {

        BcqDeclaration declaration = new BcqDeclaration(sellerDetails);
        BcqValidationResult<List<BcqHeader>> result = billingIdValidator.validate(headerList)
                .then(billingIdResult -> crssSideValidator.validate(billingIdResult.getProcessedObject(), sellerDetails))
                .then(crssSideResult -> specialEventValidator.validate(crssSideResult.getProcessedObject(),
                        sellerDetails.getShortName()))
                .then(specialEventResult -> resubmissionValidator.validate(specialEventResult.getProcessedObject(),
                        sellerDetails.getShortName()));

        List<BcqHeaderDetails> headerDetailsList = new ArrayList<>();
        if (result.getStatus() == ACCEPTED) {
            headerList = divideDataOfHeaderList(headerList);
            headerList.forEach(header -> headerDetailsList.add(new BcqHeaderDetails(header)));
            return declaration.withHeaderDetailsList(headerDetailsList);
        } else {
            if (result.getErrorMessage().getValidationError() == NO_SPECIAL_EVENT_FOUND) {
                return declaration.withValidationResult(previousResult);
            } else {
                return declaration.withValidationResult(result);
            }
        }
    }

    public BcqDeclaration processAndValidateForSettlement(List<List<String>> csv,
                                                          ParticipantSellerDetails sellerDetails,
                                                          Date tradingDate) {

        BcqDeclaration declaration = new BcqDeclaration(sellerDetails);

        BcqValidationResult<List<BcqHeader>> result = csvValidator.validate(csv)
                .then(csvResult -> headerListValidator.validateForSettlement(csvResult.getProcessedObject(), tradingDate))
                .then(headerListResult -> billingIdValidator.validate(headerListResult.getProcessedObject()))
                .then(billingIdResult -> crssSideValidator.validateBySettlement(billingIdResult.getProcessedObject(),
                        sellerDetails))
                .then(resubmissionResult -> overrideValidator.validate(resubmissionResult.getProcessedObject(),
                        sellerDetails.getShortName()))
                .then(crssSideResult -> resubmissionValidator.validate(crssSideResult.getProcessedObject(),
                        sellerDetails.getShortName()));

        List<BcqHeader> headerList = result.getProcessedObject();
        List<BcqHeaderDetails> headerDetailsList = new ArrayList<>();
        if (result.getStatus() == ACCEPTED) {
            headerList = divideDataOfHeaderList(headerList);
            headerList.forEach(header -> headerDetailsList.add(new BcqHeaderDetails(header)));
            return declaration.withHeaderDetailsList(headerDetailsList);
        } else {
            return declaration.withValidationResult(result);
        }
    }

    private List<BcqHeader> divideDataOfHeaderList(List<BcqHeader> headerList) {
        BcqInterval interval = headerList.get(0).getInterval();
        if (interval != FIVE_MINUTES_PERIOD) {
            int intervalConfig = configService.getIntegerValueForKey("BCQ_INTERVAL", 15);
            headerList.forEach(header -> {
                List<BcqData> dividedDataList = new ArrayList<>();
                header.getDataList().forEach(data ->
                        dividedDataList.addAll(divideDataByInterval(data, interval, intervalConfig)));
                header.setDataList(dividedDataList);
            });
            return headerList;
        }
        return headerList;
    }

    private List<BcqData> divideDataByInterval(BcqData data, BcqInterval interval, int intervalConfig) {
        List<BcqData> dividedDataList = new ArrayList<>();
        int totalCount;
        switch (interval) {
            case QUARTERLY:
                totalCount = intervalConfig == 5 ? 3 : 1;
                break;
            case HOURLY:
                totalCount = intervalConfig == 5 ? 12 : 4;
                break;
            default:
                return singletonList(data);
        }

        Date currentStartTime = data.getStartTime();
        BigDecimal dividedBcq = data.getBcq().divide(valueOf(totalCount), 9, ROUND_HALF_UP);
        while (totalCount > 0) {
            BcqData partialData = new BcqData();
            partialData.setReferenceMtn(data.getReferenceMtn());
            partialData.setStartTime(currentStartTime);
            partialData.setEndTime(new Date(currentStartTime.getTime() + MINUTES.toMillis(intervalConfig)));
            partialData.setBcq(dividedBcq);
            currentStartTime = partialData.getEndTime();
            dividedDataList.add(partialData);
            totalCount --;
        }
        return dividedDataList;
    }

    private ParticipantSellerDetails getSellerDetails() {
        return resourceTemplate.get(SELLER_URL, ParticipantSellerDetails.class);
    }

}
