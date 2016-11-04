package com.pemc.crss.metering.dto;

import com.pemc.crss.metering.parser.ChannelStatus;
import com.pemc.crss.metering.parser.IntervalStatus;
import lombok.Data;

import java.util.Date;

/**
 * TODO: Will be renamed to MeterData when fully implemented and previous DTO classes will be removed
 */
@Data
public class MeterData2 {

    private String sein;

    private Date readingDateTime;

    private Double kwd;
    private ChannelStatus kwdChannelStatus;
    private IntervalStatus kwdIntervalStatus;
    private Double kwhd;
    private ChannelStatus kwhdChannelStatus;
    private IntervalStatus kwhdIntervalStatus;
    private Double kvarhd;
    private ChannelStatus kvarhdChannelStatus;
    private IntervalStatus kvarhdIntervalStatus;
    private Double kwr;
    private ChannelStatus kwrChannelStatus;
    private IntervalStatus kwrIntervalStatus;
    private Double kwhr;
    private ChannelStatus kwhrChannelStatus;
    private IntervalStatus kwhrIntervalStatus;
    private Double kvarhr;
    private ChannelStatus kvarhrChannelStatus;
    private IntervalStatus kvarhrIntervalStatus;
    private Double van;
    private ChannelStatus vanChannelStatus;
    private IntervalStatus vanIntervalStatus;
    private Double vbn;
    private ChannelStatus vbnChannelStatus;
    private IntervalStatus vbnIntervalStatus;
    private Double vcn;
    private ChannelStatus vcnChannelStatus;
    private IntervalStatus vcnIntervalStatus;
    private Double ian;
    private ChannelStatus ianChannelStatus;
    private IntervalStatus ianIntervalStatus;
    private Double ibn;
    private ChannelStatus ibnChannelStatus;
    private IntervalStatus ibnIntervalStatus;
    private Double icn;
    private ChannelStatus icnChannelStatus;
    private IntervalStatus icnIntervalStatus;
    private Double pf;
    private ChannelStatus pfChannelStatus;
    private IntervalStatus pfIntervalStatus;
    private String channelStatus;
    private String intervalStatus;
    private String estimationFlag;
    private int version;

}
