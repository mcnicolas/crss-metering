package com.pemc.crss.metering.parser;

import java.util.ArrayList;
import java.util.List;

public class Header {

    private long mdefMeterHeaderId;
    private Long uploadGenInfoId;

    private String rLen;
    private String rCode;

    private String cmCustid;
    private String cmName;
    private String cmAddr1;
    private String cmAddr2;
    private String cmAccount;
    private String reserved1;
    private String cmLogchans;
    private String reserved2;
    private String taStart;
    private String taStop;
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

    public String getCmCustid() {
        return cmCustid;
    }

    public void setCmCustid(String cmCustid) {
        this.cmCustid = cmCustid;
    }

    public String getCmName() {
        return cmName;
    }

    public void setCmName(String cmName) {
        this.cmName = cmName;
    }

    public String getCmAddr1() {
        return cmAddr1;
    }

    public void setCmAddr1(String cmAddr1) {
        this.cmAddr1 = cmAddr1;
    }

    public String getCmAddr2() {
        return cmAddr2;
    }

    public void setCmAddr2(String cmAddr2) {
        this.cmAddr2 = cmAddr2;
    }

    public String getCmAccount() {
        return cmAccount;
    }

    public void setCmAccount(String cmAccount) {
        this.cmAccount = cmAccount;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getCmLogchans() {
        return cmLogchans;
    }

    public void setCmLogchans(String cmLogchans) {
        this.cmLogchans = cmLogchans;
    }

    public String getReserved2() {
        return reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
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
