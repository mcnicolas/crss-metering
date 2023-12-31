package com.pemc.crss.metering.dto.bcq.specialevent;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BcqSpecialEvent {

    private Long eventId;
    private List<Date> tradingDates;
    private Date deadlineDate;
    private String remarks;
    private List<BcqSpecialEventParticipant> tradingParticipants;

}
