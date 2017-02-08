package com.pemc.crss.metering.dto.bcq.specialevent;

import com.pemc.crss.metering.utils.BcqDateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class BcqSpecialEventList extends BcqSpecialEvent {

    private List<String> tradingParticipantsLabel;
    private Date createdDate;

    public String getTradingDatesStr() {
        return getTradingDates().stream().map(BcqDateUtils::formatDate).collect(Collectors.joining(", "));
    }

    public String getCreatedDateTime() {
        return BcqDateUtils.formatDateTime12Hr(getCreatedDate());
    }

    public String getTradingParticipantsStr() {
        return getTradingParticipantsLabel().stream().collect(Collectors.joining(", "));
    }
}
