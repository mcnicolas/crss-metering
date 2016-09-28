package com.pemc.crss.metering.parser;

import java.util.List;

public class IntervalData {

    private int recordLength;
    private int recordCode;
    private String customerID;
    private List<Float> meterReading;
    private List<String> channelStatus;
    private List<String> intervalStatus;
    private List<String> readingDate;

    public int getRecordLength() {
        return recordLength;
    }

    public void setRecordLength(int recordLength) {
        this.recordLength = recordLength;
    }

    public int getRecordCode() {
        return recordCode;
    }

    public void setRecordCode(int recordCode) {
        this.recordCode = recordCode;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public List<Float> getMeterReading() {
        return meterReading;
    }

    public void setMeterReading(List<Float> meterReading) {
        this.meterReading = meterReading;
    }

    public List<String> getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(List<String> channelStatus) {
        this.channelStatus = channelStatus;
    }

    public List<String> getIntervalStatus() {
        return intervalStatus;
    }

    public void setIntervalStatus(List<String> intervalStatus) {
        this.intervalStatus = intervalStatus;
    }

    public List<String> getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(List<String> readingDate) {
        this.readingDate = readingDate;
    }

}
