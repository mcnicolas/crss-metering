package com.pemc.crss.metering.parser.meterquantity;

import java.util.List;

class MDEFIntervalData {

    private int recordLength;
    private int recordCode;
    private String customerID;

    // TODO: Refactor. Each bean will contain a single record
    private List<Float> meterReading;
    private List<Integer> channelStatus;
    private List<Integer> intervalStatus;
    private List<String> readingDate;

    // TODO: Add version field
    // Version to increment when there is an existing customerID and readingDate

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

    public List<Integer> getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(List<Integer> channelStatus) {
        this.channelStatus = channelStatus;
    }

    public List<Integer> getIntervalStatus() {
        return intervalStatus;
    }

    public void setIntervalStatus(List<Integer> intervalStatus) {
        this.intervalStatus = intervalStatus;
    }

    public List<String> getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(List<String> readingDate) {
        this.readingDate = readingDate;
    }

}
