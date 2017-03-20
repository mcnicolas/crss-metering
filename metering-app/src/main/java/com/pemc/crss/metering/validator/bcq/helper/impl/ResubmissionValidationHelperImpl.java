package com.pemc.crss.metering.validator.bcq.helper.impl;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
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
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResubmissionValidationHelperImpl implements ResubmissionValidationHelper {

    private final BcqService bcqService;

    public HeaderListValidation validResubmission(String sellingParticipant, Date tradingDate) {
        HeaderListValidation validation = new HeaderListValidation();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            List<BcqHeader> missingHeaderList = bcqService.findHeadersOfParticipantByTradingDate(sellingParticipant,
                    tradingDate).stream()
                    .filter(header -> !bcqService.isHeaderInList(header, headerList))
                    .collect(toList());

            if (missingHeaderList.size() > 0) {
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(INCOMPLETE_RESUBMISSION_ENTRIES,
                        asList(formatDate(tradingDate), getFormattedSellingMtnAndBillingIdPair(missingHeaderList)));
                validation.setErrorMessage(errorMessage);
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

}
