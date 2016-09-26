package com.pemc.crss.metering.parser;

import java.util.List;

public class IntervalData {

    private String recordLength;
    private String recordCode;
    private String customerID;
    private List<String> uomFlow;
    private List<String> reading;
    private List<String> channelStatus;
    private List<String> channelStatusDesc;
    private List<String> intervalStatus;
    private List<String> intervalStatusDesc;
    private List<String> readingDate;

    public String getRecordLength() {
        return recordLength;
    }

    public void setRecordLength(String recordLength) {
        this.recordLength = recordLength;
    }

    public String getRecordCode() {
        return recordCode;
    }

    public void setRecordCode(String recordCode) {
        this.recordCode = recordCode;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public List<String> getUomFlow() {
        return uomFlow;
    }

    public void setUomFlow(List<String> uomFlow) {
        this.uomFlow = uomFlow;
    }

    public List<String> getReading() {
        return reading;
    }

    public void setReading(List<String> reading) {
        this.reading = reading;
    }

    public List<String> getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(List<String> channelStatus) {
        this.channelStatus = channelStatus;
    }

    public List<String> getChannelStatusDesc() {
        return channelStatusDesc;
    }

    public void setChannelStatusDesc(List<String> channelStatusDesc) {
        this.channelStatusDesc = channelStatusDesc;
    }

    public List<String> getIntervalStatus() {
        return intervalStatus;
    }

    public void setIntervalStatus(List<String> intervalStatus) {
        this.intervalStatus = intervalStatus;
    }

    public List<String> getIntervalStatusDesc() {
        return intervalStatusDesc;
    }

    public void setIntervalStatusDesc(List<String> intervalStatusDesc) {
        this.intervalStatusDesc = intervalStatusDesc;
    }

    public List<String> getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(List<String> readingDate) {
        this.readingDate = readingDate;
    }

}
