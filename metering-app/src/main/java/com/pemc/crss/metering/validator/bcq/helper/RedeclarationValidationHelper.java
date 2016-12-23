package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.validation.RedeclarationValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqValidationRules.INCOMPLETE_REDECLARATION_ENTRIES;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.validator.bcq.validation.RedeclarationValidation.emptyInst;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Component
public class RedeclarationValidationHelper {

    private final BcqService bcqService;

    @Autowired
    public RedeclarationValidationHelper(BcqService bcqService) {
        this.bcqService = bcqService;
    }

    public RedeclarationValidation validRedeclaration(String sellingParticipant, Date tradingDate) {
        RedeclarationValidation redeclarationValidation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            List<BcqHeader> missingHeaderList = getCurrentHeaderList(sellingParticipant, tradingDate)
                    .stream()
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
                redeclarationValidation.setErrorMessage(format(INCOMPLETE_REDECLARATION_ENTRIES.getErrorMessage(),
                        formatDate(tradingDate), pairs.toString()));
                return false;
            }
            return true;
        };
        redeclarationValidation.setPredicate(predicate);
        return redeclarationValidation;
    }

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private List<BcqHeader> getCurrentHeaderList(String sellingParticipant, Date tradingDate) {
        return bcqService.findAllHeadersBySellerAndTradingDate(sellingParticipant, tradingDate);
    }

}
