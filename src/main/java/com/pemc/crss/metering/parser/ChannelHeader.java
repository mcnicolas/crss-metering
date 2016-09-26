package com.pemc.crss.metering.parser;

import java.util.ArrayList;
import java.util.List;

public class ChannelHeader {

    private String recordLength;
    private String recordCode;

    private String customerID;
    private String recorderID;
    private String meterNo;
    private String startTime;
    private String stopTime;
    private String meterChannelNo;
    private String customerChannelNo;
    private String uomCode;
    private String channelStatusPresent;
    private String intervalStatusPresent;
    private String startMeterReading;
    private String stopMeterReading;
    private String meterMultiplier;
    private String serverType;
    private String intervalPerHour;
    private String validationResults;
    private String powerFlowDirection;
    private String kvaSet;
    private String dataOrigin;

    private List<IntervalData> intervals = new ArrayList<>();

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

    public String getCustomerChannelNo() {
        return customerChannelNo;
    }

    public void setCustomerChannelNo(String customerChannelNo) {
        this.customerChannelNo = customerChannelNo;
    }

    public String getUomCode() {
        return uomCode;
    }

    public void setUomCode(String uomCode) {
        this.uomCode = uomCode;
    }

    public String getChannelStatusPresent() {
        return channelStatusPresent;
    }

    public void setChannelStatusPresent(String channelStatusPresent) {
        this.channelStatusPresent = channelStatusPresent;
    }

    public String getIntervalStatusPresent() {
        return intervalStatusPresent;
    }

    public void setIntervalStatusPresent(String intervalStatusPresent) {
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

    public String getIntervalPerHour() {
        return intervalPerHour;
    }

    public void setIntervalPerHour(String intervalPerHour) {
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

    public String getKvaSet() {
        return kvaSet;
    }

    public void setKvaSet(String kvaSet) {
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

}
