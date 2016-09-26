package com.pemc.crss.metering.parser;

import java.util.ArrayList;
import java.util.List;

public class MeterHeader {

    private long mdefMeterHeaderId;
    private Long uploadGenInfoId;

    private String recordLength;
    private String recordCode;

    private String customerID;
    private String customerName;
    private String customerAddressLine1;
    private String customerAddressLine2;
    private String customerAccountNumber;
    private String reserved1;
    private String customerTotalChannels;
    private String reserved2;
    private String startTime;
    private String stopTime;
    private String dstFlag;
    private String reserved3;

    List<ChannelHeader> channels = new ArrayList<>();

    public long getMdefMeterHeaderId() {
        return mdefMeterHeaderId;
    }

    public void setMdefMeterHeaderId(long mdefMeterHeaderId) {
        this.mdefMeterHeaderId = mdefMeterHeaderId;
    }

    public Long getUploadGenInfoId() {
        return uploadGenInfoId;
    }

    public void setUploadGenInfoId(Long uploadGenInfoId) {
        this.uploadGenInfoId = uploadGenInfoId;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddressLine1() {
        return customerAddressLine1;
    }

    public void setCustomerAddressLine1(String customerAddressLine1) {
        this.customerAddressLine1 = customerAddressLine1;
    }

    public String getCustomerAddressLine2() {
        return customerAddressLine2;
    }

    public void setCustomerAddressLine2(String customerAddressLine2) {
        this.customerAddressLine2 = customerAddressLine2;
    }

    public String getCustomerAccountNumber() {
        return customerAccountNumber;
    }

    public void setCustomerAccountNumber(String customerAccountNumber) {
        this.customerAccountNumber = customerAccountNumber;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getCustomerTotalChannels() {
        return customerTotalChannels;
    }

    public void setCustomerTotalChannels(String customerTotalChannels) {
        this.customerTotalChannels = customerTotalChannels;
    }

    public String getReserved2() {
        return reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
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

    public String getDstFlag() {
        return dstFlag;
    }

    public void setDstFlag(String dstFlag) {
        this.dstFlag = dstFlag;
    }

    public String getReserved3() {
        return reserved3;
    }

    public void setReserved3(String reserved3) {
        this.reserved3 = reserved3;
    }

    public List<ChannelHeader> getChannels() {
        return channels;
    }

    public void setChannels(List<ChannelHeader> channels) {
        this.channels = channels;
    }

}
