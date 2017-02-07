package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.validation.OverrideValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqValidationError.INCOMPLETE_OVERRIDE_ENTRIES;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.validator.bcq.validation.OverrideValidation.emptyInst;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OverrideValidationHelper {

    private final BcqService bcqService;

    public Validation<List<BcqHeader>> validOverride(String sellingParticipant, Date tradingDate) {
        List<BcqHeader> currentHeaderList = getCurrentHeaderList(sellingParticipant, tradingDate);
        return noMissingHeaders(currentHeaderList);
    }

    private OverrideValidation noMissingHeaders(List<BcqHeader> currentHeaderList) {
        OverrideValidation overrideValidation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            List<BcqHeader> missingHeaderList = currentHeaderList.stream()
                    .filter(header -> !bcqService.isHeaderInList(header, headerList))
                    .collect(toList());

            if (missingHeaderList.size() > 0) {
                StringJoiner pairs = new StringJoiner("<br />");
                Set<String> pairsSet = new HashSet<>();
                missingHeaderList.forEach(missingHeader -> {
                    StringBuilder pair = new StringBuilder();
                    pair.append("<b>[")
                            .append(missingHeader.getSellingMtn())
                            .append(" - ")
                            .append(missingHeader.getBillingId())
                            .append("]</b>");
                    pairs.add(pair);
                    if (pairsSet.add(pair.toString())) {
                        pairs.add(pair);
                    }
                });
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(INCOMPLETE_OVERRIDE_ENTRIES,
                        asList(formatDate(currentHeaderList.get(0).getTradingDate()), pairs.toString()));
                overrideValidation.setErrorMessage(errorMessage);
                return false;
            }
            return true;
        };
        overrideValidation.setPredicate(predicate);
        return overrideValidation;
    }

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private List<BcqHeader> getCurrentHeaderList(String sellingParticipant, Date tradingDate) {
        return bcqService.findAllHeaders(of(
                "sellingParticipant", sellingParticipant,
                "tradingDate", formatDate(tradingDate)
        ));
    }

}
