package com.pemc.crss.metering.validator.bcq.impl;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BillingIdShortNamePair;
import com.pemc.crss.metering.resource.template.ResourceTemplate;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.BillingIdValidator;
import com.pemc.crss.metering.validator.bcq.helper.BillingIdValidationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BillingIdValidatorImpl implements BillingIdValidator {

    private static final String BILLING_VALIDATE_URL =   "/reg/bcq/billing-id/%s/tradingParticipants?date=%s";
           // "/settlement/billing/%s/tradingParticipants?date=%s";

    private final BillingIdValidationHelper validationHelper;
    private final ResourceTemplate resourceTemplate;

    @Override
    public BcqValidationResult<List<BcqHeader>> validate(List<BcqHeader> headerList) {
        log.debug("Start validation of billing id");
        List<BillingIdShortNamePair> billingIdShortNamePairs = new ArrayList<>();
        List<String> uniqueBillingIds = headerList.stream().map(BcqHeader::getBillingId).distinct().collect(toList());
        Date tradingDate = headerList.get(0).getTradingDate();
        uniqueBillingIds.forEach(billingId -> {
            List<String> shortNames = getShortNameByBillingIdAndTradingDate(billingId, tradingDate);
            billingIdShortNamePairs.add(new BillingIdShortNamePair(billingId, shortNames));
        });

        BcqValidationResult<List<BcqHeader>> result = validationHelper.validBillingIds(tradingDate)
                .test(billingIdShortNamePairs);

        if (result.getStatus() == ACCEPTED) {
            headerList.forEach(header -> header.setBuyingParticipantShortName(
                    getShortNameByBillingId(header.getBillingId(), billingIdShortNamePairs))
            );
            result.setProcessedObject(headerList);
        }
        log.debug("Finish validation of billing id, Result: {}", result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> getShortNameByBillingIdAndTradingDate(String billingId, Date tradingDate) {
        return resourceTemplate.get(format(BILLING_VALIDATE_URL, billingId, formatDate(tradingDate)), List.class);
    }

    private String getShortNameByBillingId(String billingId, List<BillingIdShortNamePair> billingIdShortNamePairs) {
        return billingIdShortNamePairs.stream()
                .filter(billingIdShortNamePair -> billingIdShortNamePair.getBillingId().equalsIgnoreCase(billingId))
                .collect(toList()).get(0).getTradingParticipantShortName().get(0);
    }

}
