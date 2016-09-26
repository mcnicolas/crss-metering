package com.pemc.crss.metering.parser;


import java.util.ArrayList;
import java.util.List;

public class ChannelHeader {

    private long mdefChannelHeaderId;

    private Long mdefMeterHeaderId;

    private String recordLength;
    private String recordCode;

    private String customerID;
    private String recorderID;
    private String reserved1;
    private String meterNo;
    private String startTime;
    private String stopTime;
    private String reserved2;
    private String reserved3;
    private String meterChannelNo;
    private String customerChannelNo;
    private String uomCode;
    private String channelStatusPresent;
    private String intervalStatusPresent;
    private String startMeterReading;
    private String stopMeterReading;
    private String reserved4;
    private String meterDialMultiplier;
    private String reserved5;
    private String serverType;
    private String reserved6;
    private String intervalPerHour;
    private String reserved7;
    private String validationResults;
    private String reserved8;
    private String powerFlowDirection;
    private String kvaSet;
    private String dataOrigin;
    private String reserved9;

    List<IntervalData> intervals = new ArrayList<IntervalData>();

    public long getMdefChannelHeaderId() {
        return mdefChannelHeaderId;
    }

    public void setMdefChannelHeaderId(long mdefChannelHeaderId) {
        this.mdefChannelHeaderId = mdefChannelHeaderId;
    }

    public Long getMdefMeterHeaderId() {
        return mdefMeterHeaderId;
    }

    public void setMdefMeterHeaderId(Long mdefMeterHeaderId) {
        this.mdefMeterHeaderId = mdefMeterHeaderId;
    }

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

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
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

    public String getReserved2() {
        return reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
    }

    public String getReserved3() {
        return reserved3;
    }

    public void setReserved3(String reserved3) {
        this.reserved3 = reserved3;
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

    public String getReserved4() {
        return reserved4;
    }

    public void setReserved4(String reserved4) {
        this.reserved4 = reserved4;
    }

    public String getMeterDialMultiplier() {
        return meterDialMultiplier;
    }

    public void setMeterDialMultiplier(String meterDialMultiplier) {
        this.meterDialMultiplier = meterDialMultiplier;
    }

    public String getReserved5() {
        return reserved5;
    }

    public void setReserved5(String reserved5) {
        this.reserved5 = reserved5;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getReserved6() {
        return reserved6;
    }

    public void setReserved6(String reserved6) {
        this.reserved6 = reserved6;
    }

    public String getIntervalPerHour() {
        return intervalPerHour;
    }

    public void setIntervalPerHour(String intervalPerHour) {
        this.intervalPerHour = intervalPerHour;
    }

    public String getReserved7() {
        return reserved7;
    }

    public void setReserved7(String reserved7) {
        this.reserved7 = reserved7;
    }

    public String getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(String validationResults) {
        this.validationResults = validationResults;
    }

    public String getReserved8() {
        return reserved8;
    }

    public void setReserved8(String reserved8) {
        this.reserved8 = reserved8;
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

    public String getReserved9() {
        return reserved9;
    }

    public void setReserved9(String reserved9) {
        this.reserved9 = reserved9;
    }

    public List<IntervalData> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<IntervalData> intervals) {
        this.intervals = intervals;
    }

}
