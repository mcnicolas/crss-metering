package com.pemc.crss.metering.validator.bcq.helper.impl;

import com.google.common.collect.Lists;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqItem;
import com.pemc.crss.metering.resource.template.ResourceTemplate;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.helper.ResubmissionValidationHelper;
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqValidationError.INCOMPLETE_RESUBMISSION_ENTRIES;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.validator.bcq.helper.BcqValidationHelperUtils.getFormattedSellingMtnAndBillingIdPair;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResubmissionValidationHelperImpl implements ResubmissionValidationHelper {

    private final BcqService bcqService;
    private static final String VALIDATE_URL = "/reg/bcq/validate/header";

    private final ResourceTemplate resourceTemplate;

    @SuppressWarnings("unchecked")
    @Override
    public HeaderListValidation validResubmission(String sellingParticipant, Date tradingDate) {
        HeaderListValidation validation = new HeaderListValidation();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            List<BcqHeader> missingHeaderList = bcqService.findHeadersOfParticipantByTradingDate(sellingParticipant,
                    tradingDate).stream()
                    .filter(header -> !bcqService.isHeaderInList(header, headerList) && !BcqStatus.VOID.equals(header.getStatus()))
                    .collect(toList());
            List<BcqHeader> notValidHeader = Lists.newArrayList();
            if (missingHeaderList.size() > 0) {
                for (BcqHeader header : missingHeaderList) {

                    Boolean hasActive = processMissingHeader(new BcqItem(header.getBuyingParticipantShortName(), tradingDate));
                    if (hasActive) {
                        notValidHeader.add(header);
                    }

                }
                if (notValidHeader.size() > 0) {
                    BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(INCOMPLETE_RESUBMISSION_ENTRIES,
                            asList(formatDate(tradingDate), getFormattedSellingMtnAndBillingIdPair(notValidHeader)));
                    validation.setErrorMessage(errorMessage);
                    return false;
                }

            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private boolean processMissingHeader(BcqItem item) {
        return resourceTemplate.post(VALIDATE_URL, Boolean.class, item);
    }

}
