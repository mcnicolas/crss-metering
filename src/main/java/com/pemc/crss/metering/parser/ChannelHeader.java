package com.pemc.crss.metering.parser;


import java.util.ArrayList;
import java.util.List;

public class ChannelHeader {

    private long mdefChannelHeaderId;

    private Long mdefMeterHeaderId;

    private String rLen;

    private String rCode;

    private String dcCustid;
    private String dcRecid;
    private String reserved1;
    private String dcMeterid;
    private String taStart;
    private String taStop;
    private String reserved2;
    private String reserved3;
    private String dcPyschan;
    private String dcLogchan;
    private String dcUmcode;
    private String chanStat;
    private String intstat;
    private String strtmtr;
    private String stopmtr;
    private String reserved4;
    private String dcMmult;
    private String reserved5;
    private String dcServerType;
    private String reserved6;
    private String drInphr;
    private String reserved7;
    private String tdStatus;
    private String reserved8;
    private String dcFlow;
    private String dcKvaset;
    private String tdOrigin;
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

    public String getDcCustid() {
        return dcCustid;
    }

    public void setDcCustid(String dcCustid) {
        this.dcCustid = dcCustid;
    }

    public String getDcRecid() {
        return dcRecid;
    }

    public void setDcRecid(String dcRecid) {
        this.dcRecid = dcRecid;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getDcMeterid() {
        return dcMeterid;
    }

    public void setDcMeterid(String dcMeterid) {
        this.dcMeterid = dcMeterid;
    }

    public String getTaStart() {
        return taStart;
    }

    public void setTaStart(String taStart) {
        this.taStart = taStart;
    }

    public String getTaStop() {
        return taStop;
    }

    public void setTaStop(String taStop) {
        this.taStop = taStop;
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

    public String getDcPyschan() {
        return dcPyschan;
    }

    public void setDcPyschan(String dcPyschan) {
        this.dcPyschan = dcPyschan;
    }

    public String getDcLogchan() {
        return dcLogchan;
    }

    public void setDcLogchan(String dcLogchan) {
        this.dcLogchan = dcLogchan;
    }

    public String getDcUmcode() {
        return dcUmcode;
    }

    public void setDcUmcode(String dcUmcode) {
        this.dcUmcode = dcUmcode;
    }

    public String getChanStat() {
        return chanStat;
    }

    public void setChanStat(String chanStat) {
        this.chanStat = chanStat;
    }

    public String getIntstat() {
        return intstat;
    }

    public void setIntstat(String intstat) {
        this.intstat = intstat;
    }

    public String getStrtmtr() {
        return strtmtr;
    }

    public void setStrtmtr(String strtmtr) {
        this.strtmtr = strtmtr;
    }

    public String getStopmtr() {
        return stopmtr;
    }

    public void setStopmtr(String stopmtr) {
        this.stopmtr = stopmtr;
    }

    public String getReserved4() {
        return reserved4;
    }

    public void setReserved4(String reserved4) {
        this.reserved4 = reserved4;
    }

    public String getDcMmult() {
        return dcMmult;
    }

    public void setDcMmult(String dcMmult) {
        this.dcMmult = dcMmult;
    }

    public String getReserved5() {
        return reserved5;
    }

    public void setReserved5(String reserved5) {
        this.reserved5 = reserved5;
    }

    public String getDcServerType() {
        return dcServerType;
    }

    public void setDcServerType(String dcServerType) {
        this.dcServerType = dcServerType;
    }

    public String getReserved6() {
        return reserved6;
    }

    public void setReserved6(String reserved6) {
        this.reserved6 = reserved6;
    }

    public String getDrInphr() {
        return drInphr;
    }

    public void setDrInphr(String drInphr) {
        this.drInphr = drInphr;
    }

    public String getReserved7() {
        return reserved7;
    }

    public void setReserved7(String reserved7) {
        this.reserved7 = reserved7;
    }

    public String getTdStatus() {
        return tdStatus;
    }

    public void setTdStatus(String tdStatus) {
        this.tdStatus = tdStatus;
    }

    public String getReserved8() {
        return reserved8;
    }

    public void setReserved8(String reserved8) {
        this.reserved8 = reserved8;
    }

    public String getDcFlow() {
        return dcFlow;
    }

    public void setDcFlow(String dcFlow) {
        this.dcFlow = dcFlow;
    }

    public String getDcKvaset() {
        return dcKvaset;
    }

    public void setDcKvaset(String dcKvaset) {
        this.dcKvaset = dcKvaset;
    }

    public String getTdOrigin() {
        return tdOrigin;
    }

    public void setTdOrigin(String tdOrigin) {
        this.tdOrigin = tdOrigin;
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
