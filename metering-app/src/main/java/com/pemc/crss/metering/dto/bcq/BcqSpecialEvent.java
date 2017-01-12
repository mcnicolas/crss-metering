package com.pemc.crss.metering.dto.bcq;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BcqSpecialEvent {

    private Long eventId;
    private List<Date> tradingDates;
    private Date deadlineDate;
    private List<String> tradingParticipants;
    private String remarks;

}
