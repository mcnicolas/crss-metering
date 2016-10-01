package com.pemc.crss.metering.parser;

import java.util.ArrayList;
import java.util.List;

public class MeterData {

    private Header header;
    private TrailerRecord trailerRecord;
    private String error;
    private List<ChannelHeader> channels = new ArrayList<>();

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public TrailerRecord getTrailerRecord() {
        return trailerRecord;
    }

    public void setTrailerRecord(TrailerRecord trailerRecord) {
        this.trailerRecord = trailerRecord;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<ChannelHeader> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelHeader> channels) {
        this.channels = channels;
    }

    public void addChannel(ChannelHeader channelHeader) {
        channels.add(channelHeader);
    }

}
