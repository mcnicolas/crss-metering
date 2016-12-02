package com.pemc.crss.metering.parser.meterquantity;

import java.util.ArrayList;
import java.util.List;

class MDEFMeterData {

    private MDEFHeader MDEFHeader;
    private MDEFTrailerRecord MDEFTrailerRecord;
    private String error;
    private List<MDEFChannelHeader> channels = new ArrayList<>();

    public MDEFHeader getMDEFHeader() {
        return MDEFHeader;
    }

    public void setMDEFHeader(MDEFHeader MDEFHeader) {
        this.MDEFHeader = MDEFHeader;
    }

    public MDEFTrailerRecord getMDEFTrailerRecord() {
        return MDEFTrailerRecord;
    }

    public void setMDEFTrailerRecord(MDEFTrailerRecord MDEFTrailerRecord) {
        this.MDEFTrailerRecord = MDEFTrailerRecord;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<MDEFChannelHeader> getChannels() {
        return channels;
    }

    public void setChannels(List<MDEFChannelHeader> channels) {
        this.channels = channels;
    }

    public void addChannel(MDEFChannelHeader MDEFChannelHeader) {
        channels.add(MDEFChannelHeader);
    }

}
