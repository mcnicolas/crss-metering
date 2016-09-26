package com.pemc.crss.metering.parser;

public class TrailerRecord {

    private String recordLength;
    private String recordCode;
    private String totalRecordCount;
    private String reserved1;
    private String timestamp;

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

    public String getTotalRecordCount() {
        return totalRecordCount;
    }

    public void setTotalRecordCount(String totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
