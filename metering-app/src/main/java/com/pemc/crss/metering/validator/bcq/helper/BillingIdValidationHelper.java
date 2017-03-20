package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BillingIdShortNamePair;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.validation.BillingIdValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqValidationError.BILLING_ID_NOT_EXIST;
import static com.pemc.crss.metering.constants.BcqValidationError.MULTIPLE_PARTICIPANT_BILLING_ID;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatLongDate;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BillingIdValidationHelper {

    public BillingIdValidation validBillingIds(Date tradingDate) {
        BillingIdValidation validation = new BillingIdValidation();
        Predicate<List<BillingIdShortNamePair>> predicate = billingIdShortNamePairs -> {
            List<String> notExistingBillingIds = new ArrayList<>();
            for (BillingIdShortNamePair billingIdShortNamePair : billingIdShortNamePairs) {
                billingIdShortNamePair.setTradingParticipantShortName(billingIdShortNamePair
                        .getTradingParticipantShortName().stream().distinct().collect(toList()));
                if (billingIdShortNamePair.getTradingParticipantShortName().size() > 1) {
                    BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(MULTIPLE_PARTICIPANT_BILLING_ID,
                            asList(billingIdShortNamePair.getBillingId(), formatLongDate(tradingDate)));
                    validation.setErrorMessage(errorMessage);
                    return false;
                } else if (billingIdShortNamePair.getTradingParticipantShortName().size() < 1) {
                    notExistingBillingIds.add(billingIdShortNamePair.getBillingId());
                }
            }

            if (notExistingBillingIds.size() > 0) {
                StringJoiner billingIds = new StringJoiner(", ");
                notExistingBillingIds.forEach(billingIds::add);
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(BILLING_ID_NOT_EXIST,
                        singletonList("<b>" + billingIds.toString() + "</b>"));
                validation.setErrorMessage(errorMessage);
                return false;
            }

            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

}
