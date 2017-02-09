package com.pemc.crss.metering.validator.bcq.handler;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.constants.BcqValidationError;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.resource.template.ResourceTemplate;
import com.pemc.crss.metering.validator.bcq.*;
import com.pemc.crss.metering.validator.bcq.helper.BcqPopulator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.constants.BcqInterval.FIVE_MINUTES_PERIOD;
import static com.pemc.crss.metering.constants.BcqInterval.fromDescription;
import static com.pemc.crss.metering.constants.BcqStatus.FOR_CONFIRMATION;
import static com.pemc.crss.metering.constants.BcqStatus.FOR_NULLIFICATION;
import static com.pemc.crss.metering.constants.BcqValidationError.*;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static java.lang.Integer.parseInt;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.valueOf;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.ListUtils.union;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BcqValidationHandler {

    private static final String SELLER_URL = "/reg/bcq/seller";
    private static final String VALIDATE_URL = "/reg/bcq/validate";
    private static final String SETTLEMENT_VALIDATE_URL = "/reg/bcq/settlement/validate";
    private static final int INTERVAL_ROW_INDEX = 0;
    private static final int INTERVAL_COLUMN_INDEX = 1;

    private final CsvValidator csvValidator;
    private final HeaderListValidator headerListValidator;
    private final SpecialEventValidator specialEventValidator;
    private final ResubmissionValidator resubmissionValidator;
    private final OverrideValidator overrideValidator;
    private final ResourceTemplate resourceTemplate;
    private final CacheManager cacheManager;

    public BcqDeclaration processAndValidate(List<List<String>> csv) {
        ParticipantSellerDetails sellerDetails = getSellerDetails();
        BcqDeclaration declaration = new BcqDeclaration(sellerDetails);
        BcqValidationResult validationResult = csvValidator.validate(csv);
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        BcqInterval interval = fromDescription(csv.get(INTERVAL_ROW_INDEX).get(INTERVAL_COLUMN_INDEX));
        BcqPopulator populator = new BcqPopulator();
        List<BcqHeader> headerList = populator.populate(csv, interval);
        validationResult = headerListValidator.validate(headerList, interval);

        if (validationResult.getStatus() == REJECTED) {
            if (validationResult.getErrorMessage().getValidationError() == CLOSED_TRADING_DATE) {
                BcqDeclaration specialEventDeclaration = processAndValidateForSpecialEvent(headerList, declaration,
                        sellerDetails, interval);
                if (specialEventDeclaration.getValidationResult().getStatus() == REJECTED) {
                    BcqValidationError specialEventDeclarationError = specialEventDeclaration.getValidationResult()
                            .getErrorMessage().getValidationError();
                    if (!union(CRSS_SIDE_ERRORS, singletonList(NO_SPECIAL_EVENT_FOUND))
                            .contains(specialEventDeclarationError)) {
                        return specialEventDeclaration;
                    }
                } else {
                    return specialEventDeclaration;
                }
            }
            return declaration.withValidationResult(validationResult);
        }

        BcqParticipantDetails participantDetails = getAndValidate(getUniqueItems(headerList));
        if (participantDetails.getValidationResult().getStatus() == REJECTED) {
            return declaration.withValidationResult(participantDetails.getValidationResult());
        }

        headerList = addParticipantDetailsToHeaderList(headerList, sellerDetails, participantDetails);

        validationResult = resubmissionValidator.validate(headerList, sellerDetails.getShortName(),
                headerList.get(0).getTradingDate());
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        headerList = divideDataOfHeaderList(headerList, interval);

        List<BcqHeaderDetails> headerDetailsList = new ArrayList<>();
        headerList.forEach(header -> headerDetailsList.add(new BcqHeaderDetails(header)));
        return declaration.withHeaderDetailsList(headerDetailsList);
    }

    private BcqDeclaration processAndValidateForSpecialEvent(List<BcqHeader> headerList, BcqDeclaration declaration,
                                                             ParticipantSellerDetails sellerDetails,
                                                             BcqInterval interval) {

        BcqValidationResult validationResult;
        BcqParticipantDetails participantDetails = getAndValidate(getUniqueItems(headerList));
        if (participantDetails.getValidationResult().getStatus() == REJECTED) {
            return declaration.withValidationResult(participantDetails.getValidationResult());
        }

        headerList = addParticipantDetailsToHeaderList(headerList, sellerDetails, participantDetails);

        validationResult = specialEventValidator.validate(headerList, sellerDetails.getShortName());
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        validationResult = resubmissionValidator.validate(headerList, sellerDetails.getShortName(),
                headerList.get(0).getTradingDate());
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        headerList = divideDataOfHeaderList(headerList, interval);

        List<BcqHeaderDetails> headerDetailsList = new ArrayList<>();
        headerList.forEach(header -> headerDetailsList.add(new BcqHeaderDetails(header)));
        declaration.setSpecialEvent(true);
        return declaration.withHeaderDetailsList(headerDetailsList);
    }

    public BcqDeclaration processAndValidateForSettlement(List<List<String>> csv,
                                                          ParticipantSellerDetails sellerDetails,
                                                          Date tradingDate) {

        BcqDeclaration declaration = new BcqDeclaration(sellerDetails);
        BcqValidationResult validationResult = csvValidator.validate(csv);
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        BcqInterval interval = fromDescription(csv.get(INTERVAL_ROW_INDEX).get(INTERVAL_COLUMN_INDEX));
        BcqPopulator populator = new BcqPopulator();
        List<BcqHeader> headerList = populator.populate(csv, interval);
        validationResult = headerListValidator.validateForSettlement(headerList, interval, tradingDate);
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        SellerWithItems sellerWithItems = new SellerWithItems(sellerDetails, getUniqueItems(headerList));
        BcqParticipantDetails participantDetails = getAndValidate(sellerWithItems);
        if (participantDetails.getValidationResult().getStatus() == REJECTED) {
            return declaration.withValidationResult(participantDetails.getValidationResult());
        }

        headerList = addParticipantDetailsToHeaderList(headerList, sellerDetails, participantDetails);

        validationResult = overrideValidator.validate(headerList, sellerDetails.getShortName(),
                headerList.get(0).getTradingDate());
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        headerList = addParticipantDetailsToHeaderList(headerList, sellerDetails, participantDetails);

        validationResult = resubmissionValidator.validate(headerList,
                sellerDetails.getShortName(),
                headerList.get(0).getTradingDate());
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        headerList = divideDataOfHeaderList(headerList, interval);

        List<BcqHeaderDetails> headerDetailsList = new ArrayList<>();
        headerList.forEach(header -> headerDetailsList.add(new BcqHeaderDetails(header)));
        return declaration.withHeaderDetailsList(headerDetailsList);
    }

    private List<BcqItem> getUniqueItems(List<BcqHeader> headerList) {
        return headerList.stream().
                map(header -> {
                    List<String> referenceMtns = header.getDataList().stream()
                            .map(BcqData::getReferenceMtn)
                            .distinct()
                            .collect(toList());
                    BcqItem item = new BcqItem();
                    item.setSellingMtn(header.getSellingMtn());
                    item.setBillingId(header.getBillingId());
                    item.setReferenceMtns(referenceMtns);
                    return item;
                })
                .distinct()
                .collect(toList());
    }

    private List<BcqHeader> addParticipantDetailsToHeaderList(List<BcqHeader> headerList,
                                                              ParticipantSellerDetails sellerDetails,
                                                              BcqParticipantDetails participantDetails) {

        return headerList.stream().map(header -> {
            header.setSellingParticipantName(sellerDetails.getName());
            header.setBuyingParticipantShortName(sellerDetails.getShortName());
            ParticipantBuyerDetails buyerDetails =
                    participantDetails.getBuyerDetailsList().stream()
                            .filter(buyer -> buyer.getBillingId().equals(header.getBillingId()))
                            .collect(toList())
                            .get(0);
            header.setBuyingParticipantUserId(buyerDetails.getUserId());
            header.setBuyingParticipantName(buyerDetails.getName());
            header.setBuyingParticipantShortName(buyerDetails.getShortName());
            header.setStatus(buyerDetails.isBcqConfirmation() ? FOR_NULLIFICATION : FOR_CONFIRMATION);
            return header;
        }).collect(toList());
    }

    private int getIntervalConfig() {
        Cache configCache = cacheManager.getCache("config");
        ValueWrapper valueWrapper = configCache.get("BCQ_INTERVAL");
        return valueWrapper == null ? 15 : parseInt(valueWrapper.get().toString());
    }

    private List<BcqHeader> divideDataOfHeaderList(List<BcqHeader> headerList, BcqInterval interval) {
        if (interval != FIVE_MINUTES_PERIOD) {
            int intervalConfig = getIntervalConfig();
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

    private BcqParticipantDetails getAndValidate(List<BcqItem> itemList) {
        return resourceTemplate.post(VALIDATE_URL, BcqParticipantDetails.class, itemList);
    }

    private BcqParticipantDetails getAndValidate(SellerWithItems sellerWithItems) {
        return resourceTemplate.post(SETTLEMENT_VALIDATE_URL, BcqParticipantDetails.class, sellerWithItems);
    }

}
