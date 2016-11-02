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

    private double kwd;
    private ChannelStatus kwdChannelStatus;
    private IntervalStatus kwdIntervalStatus;
    private double kwhd;
    private ChannelStatus kwhdChannelStatus;
    private IntervalStatus kwhdIntervalStatus;
    private double kvarhd;
    private ChannelStatus kvarhdChannelStatus;
    private IntervalStatus kvarhdIntervalStatus;
    private double kwr;
    private ChannelStatus kwrChannelStatus;
    private IntervalStatus kwrIntervalStatus;
    private double kwhr;
    private ChannelStatus kwhrChannelStatus;
    private IntervalStatus kwhrIntervalStatus;
    private double kvarhr;
    private ChannelStatus kvarhrChannelStatus;
    private IntervalStatus kvarhrIntervalStatus;
    private double van;
    private ChannelStatus vanChannelStatus;
    private IntervalStatus vanIntervalStatus;
    private double vbn;
    private ChannelStatus vbnChannelStatus;
    private IntervalStatus vbnIntervalStatus;
    private double vcn;
    private ChannelStatus vcnChannelStatus;
    private IntervalStatus vcnIntervalStatus;
    private double ian;
    private ChannelStatus ianChannelStatus;
    private IntervalStatus ianIntervalStatus;
    private double ibn;
    private ChannelStatus ibnChannelStatus;
    private IntervalStatus ibnIntervalStatus;
    private double icn;
    private ChannelStatus icnChannelStatus;
    private IntervalStatus icnIntervalStatus;
    private double pf;
    private ChannelStatus pfChannelStatus;
    private IntervalStatus pfIntervalStatus;
    private String channelStatus;
    private String intervalStatus;
    private String estimationFlag;
    private int version;

}
