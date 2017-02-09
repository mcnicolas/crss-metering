package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BcqHeader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class BcqValidationHelperUtils {

    public static String getFormattedSellingMtnAndBillingIdPair(List<BcqHeader> headerList) {
        StringJoiner pairs = new StringJoiner("<br />");
        Set<String> pairsSet = new HashSet<>();
        headerList.forEach(header -> {
            StringBuilder pair = new StringBuilder();
            pair.append("<b>[")
                    .append(header.getSellingMtn())
                    .append(" - ")
                    .append(header.getBillingId())
                    .append("]</b>");
            if (pairsSet.add(pair.toString())) {
                pairs.add(pair);
            }
        });
        return pairs.toString();
    }

}
