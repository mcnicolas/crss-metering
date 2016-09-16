package com.pemc.crss.metering.parser;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Trailer {

    String rLen;
    String rCode;
    String totRec;
    String reserved1;
    String  xsTstamp;

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

    public String getTotRec() {
        return totRec;
    }

    public void setTotRec(String totRec) {
        this.totRec = totRec;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getXsTstamp() {
        return xsTstamp;
    }

    public void setXsTstamp(String xsTstamp) {
        this.xsTstamp = xsTstamp;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
