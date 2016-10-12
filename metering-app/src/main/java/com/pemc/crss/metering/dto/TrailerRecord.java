package com.pemc.crss.metering.dto;

public class TrailerRecord {

    private int recordLength;
    private int recordCode;
    private String totalRecordCount;
    private String timestamp;

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

    public String getTotalRecordCount() {
        return totalRecordCount;
    }

    public void setTotalRecordCount(String totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
