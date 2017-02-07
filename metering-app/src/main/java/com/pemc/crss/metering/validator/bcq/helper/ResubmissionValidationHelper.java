package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqValidationError.INCOMPLETE_RESUBMISSION_ENTRIES;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation.emptyInst;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResubmissionValidationHelper {

    private final BcqService bcqService;

    public HeaderListValidation validResubmission(String sellingParticipant, Date tradingDate) {
        HeaderListValidation validation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            List<BcqHeader> missingHeaderList = getCurrentHeaderList(sellingParticipant, tradingDate).stream()
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
                    if (pairsSet.add(pair.toString())) {
                        pairs.add(pair);
                    }
                });
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(INCOMPLETE_RESUBMISSION_ENTRIES,
                        asList(formatDate(tradingDate), pairs.toString()));
                validation.setErrorMessage(errorMessage);
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
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
