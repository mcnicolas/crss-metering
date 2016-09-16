package com.pemc.crss.metering.parser;

import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class IntervalData {

    String rLen;
    String rCode;
    String cmCustId;
    List<String> uomFlow;
    List<String> reading;
    List<String> channelStatus;
    List<String> channelStatusDesc;
    List<String> intervalStatus;
    List<String> intervalStatusDesc;
    List<String> readingDate;

    public String getrLen() {
        return rLen;
    }

    public void setrLen(String rLen) {
        this.rLen = rLen;
    }

    public String getrCode() {
        return rCode;
    }

    public void setrCode(String rCode) {
        this.rCode = rCode;
    }

    public String getCmCustId() {
        return cmCustId;
    }

    public void setCmCustId(String cmCustId) {
        this.cmCustId = cmCustId;
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
