package com.pemc.crss.metering.dto;

import java.util.ArrayList;
import java.util.List;

public class ChannelHeader {

    // RLEN 2 bytes
    private int recordLength;

    // RCODE 2 bytes
    private int recordCode;

    // DC_CUSTID 20 bytes
    private String customerID;

    // DC_RECID 14 bytes
    private String recorderID;

    // DC_METERID 12 bytes
    private String meterNo;

    // TA_START 12 bytes
    private String startTime;

    // TA_STOP 12 bytes
    private String stopTime;

    // DC_PYSCHAN 2 bytes
    private String meterChannelNo;

    // DC_LOGCHAN 2 bytes
    private int customerChannelNo;

    // DC_UMCODE 2 bytes
    private String uomCode;

    // CHANSTAT 1 byte
    private boolean channelStatusPresent;

    // INTSTAT 1 byte
    private boolean intervalStatusPresent;

    // STRTMTR 12 bytes
    private String startMeterReading;

    // STOPMTR 12 bytes
    private String stopMeterReading;

    // DC_MMULT 10 bytes
    private String meterMultiplier;

    // DC_SERVTYPE 1 byte
    private String serverType;

    // DR_INPHR 2 bytes
    private int intervalPerHour;

    // TD_STATUS 2 bytes
    private String validationResults;

    // DC_FLOW 1 byte
    private String powerFlowDirection;

    // DC_KVASET 2 bytes
    private int kvaSet;

    // TD_ORIGIN 1 byte
    private String dataOrigin;

    private List<IntervalData> intervals = new ArrayList<>();

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

    public String getRecorderID() {
        return recorderID;
    }

    public void setRecorderID(String recorderID) {
        this.recorderID = recorderID;
    }

    public String getMeterNo() {
        return meterNo;
    }

    public void setMeterNo(String meterNo) {
        this.meterNo = meterNo;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getMeterChannelNo() {
        return meterChannelNo;
    }

    public void setMeterChannelNo(String meterChannelNo) {
        this.meterChannelNo = meterChannelNo;
    }

    public int getCustomerChannelNo() {
        return customerChannelNo;
    }

    public void setCustomerChannelNo(int customerChannelNo) {
        this.customerChannelNo = customerChannelNo;
    }

    public String getUomCode() {
        return uomCode;
    }

    public void setUomCode(String uomCode) {
        this.uomCode = uomCode;
    }

    public boolean isChannelStatusPresent() {
        return channelStatusPresent;
    }

    public void setChannelStatusPresent(boolean channelStatusPresent) {
        this.channelStatusPresent = channelStatusPresent;
    }

    public boolean isIntervalStatusPresent() {
        return intervalStatusPresent;
    }

    public void setIntervalStatusPresent(boolean intervalStatusPresent) {
        this.intervalStatusPresent = intervalStatusPresent;
    }

    public String getStartMeterReading() {
        return startMeterReading;
    }

    public void setStartMeterReading(String startMeterReading) {
        this.startMeterReading = startMeterReading;
    }

    public String getStopMeterReading() {
        return stopMeterReading;
    }

    public void setStopMeterReading(String stopMeterReading) {
        this.stopMeterReading = stopMeterReading;
    }

    public String getMeterMultiplier() {
        return meterMultiplier;
    }

    public void setMeterMultiplier(String meterMultiplier) {
        this.meterMultiplier = meterMultiplier;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public int getIntervalPerHour() {
        return intervalPerHour;
    }

    public void setIntervalPerHour(int intervalPerHour) {
        this.intervalPerHour = intervalPerHour;
    }

    public String getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(String validationResults) {
        this.validationResults = validationResults;
    }

    public String getPowerFlowDirection() {
        return powerFlowDirection;
    }

    public void setPowerFlowDirection(String powerFlowDirection) {
        this.powerFlowDirection = powerFlowDirection;
    }

    public int getKvaSet() {
        return kvaSet;
    }

    public void setKvaSet(int kvaSet) {
        this.kvaSet = kvaSet;
    }

    public String getDataOrigin() {
        return dataOrigin;
    }

    public void setDataOrigin(String dataOrigin) {
        this.dataOrigin = dataOrigin;
    }

    public List<IntervalData> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<IntervalData> intervals) {
        this.intervals = intervals;
    }

    public void addInterval(IntervalData intervalData) {
        intervals.add(intervalData);
    }

}
