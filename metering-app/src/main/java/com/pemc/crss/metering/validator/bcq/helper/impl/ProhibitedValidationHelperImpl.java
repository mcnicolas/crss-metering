package com.pemc.crss.metering.validator.bcq.helper.impl;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqProhibitedPair;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.helper.ProhibitedValidationHelper;
import com.pemc.crss.metering.validator.bcq.validation.ProhibitedValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.pemc.crss.metering.constants.BcqValidationError.CONTAINS_PROHIBITED_PAIRS;
import static com.pemc.crss.metering.constants.BcqValidationError.INCOMPLETE_RESUBMISSION_ENTRIES;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.validator.bcq.helper.BcqValidationHelperUtils.getFormattedSellingMtnAndBillingIdPair;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProhibitedValidationHelperImpl implements ProhibitedValidationHelper {

    private final BcqService bcqService;

    @Override
    public ProhibitedValidation noProhibitedPairs() {
        ProhibitedValidation validation = new ProhibitedValidation();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            List<Pair<String, String>> enabledProhibitedPairs = bcqService.findAllEnabledProhibitedPairs().stream()
                    .map(prohibitedPair -> Pair.of(prohibitedPair.getSellingMtn(), prohibitedPair.getBillingId()))
                    .collect(Collectors.toList());
            List<BcqHeader> headersWithProhibitedPairs = new ArrayList<>();
            headerList.forEach(header -> {
                if (enabledProhibitedPairs.contains(Pair.of(header.getSellingMtn().toUpperCase(),
                        header.getBillingId().toUpperCase()))) {
                    headersWithProhibitedPairs.add(header);
                }
            });

            if (!headersWithProhibitedPairs.isEmpty()) {
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(CONTAINS_PROHIBITED_PAIRS,
                        singletonList(getFormattedSellingMtnAndBillingIdPair(headersWithProhibitedPairs)));
                validation.setErrorMessage(errorMessage);
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

}
