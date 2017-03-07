package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BillingIdShortNamePair;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.validation.BillingIdValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqValidationError.BILLING_ID_NOT_EXIST;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BillingIdValidationHelper {

    public Validation<List<String>> validBillingIds(List<BillingIdShortNamePair> billingIdShortNamePairs) {
        BillingIdValidation validation = new BillingIdValidation();
        Predicate<List<String>> predicate = billingIds -> {
            billingIds.removeAll(billingIdShortNamePairs.stream()
                    .map(BillingIdShortNamePair::getBillingId).collect(toList()));

            if (billingIds.size() > 0) {
                StringJoiner noRecordBillingIds = new StringJoiner(", ");
                billingIds.forEach(noRecordBillingIds::add);
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(BILLING_ID_NOT_EXIST,
                        singletonList("<b>" + noRecordBillingIds.toString() + "</b>"));
                validation.setErrorMessage(errorMessage);
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

}
