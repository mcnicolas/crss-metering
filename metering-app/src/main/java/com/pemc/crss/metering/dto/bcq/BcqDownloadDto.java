package com.pemc.crss.metering.dto.bcq;

import java.util.List;

/**
 * Created on 1/9/18.
 */
 public class BcqDownloadDto {
   private List<String> refMtns;
   private String refMtn;
    private String buyerBillingId;
    private String sellingMtn;
    private String date;
    private String bcq;
    private String genName;
    private String buyerMtn;

    public String getRefMtn() {
        return refMtn;
    }

    public void setRefMtn(String refMtn) {
        this.refMtn = refMtn;
    }

    public List<String> getRefMtns() {
        return refMtns;
    }

    public void setRefMtns(List<String> refMtns) {
        this.refMtns = refMtns;
    }

    public String getBuyerBillingId() {
        return buyerBillingId;
    }

    public void setBuyerBillingId(String buyerBillingId) {
        this.buyerBillingId = buyerBillingId;
    }

    public String getSellingMtn() {
        return sellingMtn;
    }

    public void setSellingMtn(String sellingMtn) {
        this.sellingMtn = sellingMtn;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBcq() {
        return bcq;
    }

    public void setBcq(String bcq) {
        this.bcq = bcq;
    }

    public String getGenName() {
        return genName;
    }

    public void setGenName(String genName) {
        this.genName = genName;
    }

    public String getBuyerMtn() {
        return buyerMtn;
    }

    public void setBuyerMtn(String buyerMtn) {
        this.buyerMtn = buyerMtn;
    }

    public BcqDownloadDto() {
    }

    public BcqDownloadDto(List<String> refMtns, String buyerBillingId, String sellingMtn, String date, String bcq, String genName, String buyerMtn) {
        this.refMtns = refMtns;
        this.buyerBillingId = buyerBillingId;
        this.sellingMtn = sellingMtn;
        this.date = date;
        this.bcq = bcq;
        this.genName = genName;
        this.buyerMtn = buyerMtn;
    }

    public BcqDownloadDto(String refMtn, String buyerBillingId, String sellingMtn, String date, String bcq, String buyerMtn) {
        this.refMtn = refMtn;
        this.buyerBillingId = buyerBillingId;
        this.sellingMtn = sellingMtn;
        this.date = date;
        this.bcq = bcq;
        this.buyerMtn = buyerMtn;
    }


}
