package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.validation.ResubmissionValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqValidationRules.INCOMPLETE_RESUBMISSION_ENTRIES;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.validator.bcq.validation.ResubmissionValidation.emptyInst;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResubmissionValidationHelper {

    private final BcqService bcqService;

    public ResubmissionValidation validResubmission(String sellingParticipant, Date tradingDate) {
        ResubmissionValidation resubmissionValidation = emptyInst();
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
                resubmissionValidation.setErrorMessage(format(INCOMPLETE_RESUBMISSION_ENTRIES.getErrorMessage(),
                        formatDate(tradingDate), pairs.toString()));
                return false;
            }
            return true;
        };
        resubmissionValidation.setPredicate(predicate);
        return resubmissionValidation;
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
