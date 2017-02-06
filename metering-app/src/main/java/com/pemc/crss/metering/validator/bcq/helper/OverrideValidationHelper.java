package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.validation.OverrideValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqValidationRules.EXCESS_OVERRIDE_ENTRIES;
import static com.pemc.crss.metering.constants.BcqValidationRules.INCOMPLETE_OVERRIDE_ENTRIES;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.validator.bcq.validation.OverrideValidation.emptyInst;
import static java.lang.String.format;
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
            if (headerList.size() > currentHeaderList.size()) {
                return true;
            }
            List<BcqHeader> missingHeaderList = currentHeaderList.stream()
                    .filter(header -> !bcqService.isHeaderInList(header, headerList))
                    .collect(toList());

            if (missingHeaderList.size() > 0) {
                StringJoiner pairs = new StringJoiner("<br />");
                missingHeaderList.forEach(missingHeader -> {
                    StringBuilder pair = new StringBuilder();
                    pair.append("<b>[")
                            .append(missingHeader.getSellingMtn())
                            .append(" - ")
                            .append(missingHeader.getBillingId())
                            .append("]</b>");
                    pairs.add(pair);
                });
                overrideValidation.setErrorMessage(format(INCOMPLETE_OVERRIDE_ENTRIES.getErrorMessage(),
                        formatDate(currentHeaderList.get(0).getTradingDate()), pairs.toString()));
                return false;
            }
            return true;
        };
        overrideValidation.setPredicate(predicate);
        return overrideValidation;
    }

    private OverrideValidation noExcessHeaders(List<BcqHeader> currentHeaderList) {
        OverrideValidation overrideValidation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            if (headerList.size() < currentHeaderList.size()) {
                return true;
            }
            List<BcqHeader> excessHeaderList = headerList.stream()
                    .filter(header -> !bcqService.isHeaderInList(header, currentHeaderList))
                    .collect(toList());

            if (excessHeaderList.size() > 0) {
                StringJoiner pairs = new StringJoiner("<br />");
                excessHeaderList.forEach(excessHeader -> {
                    StringBuilder pair = new StringBuilder();
                    pair.append("<b>[")
                            .append(excessHeader.getSellingMtn())
                            .append(" - ")
                            .append(excessHeader.getBillingId())
                            .append("]</b>");
                    pairs.add(pair);
                });
                overrideValidation.setErrorMessage(format(EXCESS_OVERRIDE_ENTRIES.getErrorMessage(),
                        formatDate(currentHeaderList.get(0).getTradingDate()), pairs.toString()));
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
