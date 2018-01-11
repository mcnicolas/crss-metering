package com.pemc.crss.metering.dto.bcq;

/**
 * Created on 1/9/18.
 */
 public class BcqDownloadDto {
   private String refMtn;
    private String buyerBillingId;
    private String sellingMtn;
    private String date;
    private String bcq;
    private String genName;

    public String getRefMtn() {
        return refMtn;
    }

    public void setRefMtn(String refMtn) {
        this.refMtn = refMtn;
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

    public BcqDownloadDto() {
    }

    public BcqDownloadDto(String refMtn, String buyerBillingId, String sellingMtn, String date, String bcq) {
        this.refMtn = refMtn;
        this.buyerBillingId = buyerBillingId;
        this.sellingMtn = sellingMtn;
        this.date = date;
        this.bcq = bcq;
    }

    @Override
    public String toString() {
        return "BcqDownloadDto{" +
                "refMtn='" + refMtn + '\'' +
                ", buyerBillingId='" + buyerBillingId + '\'' +
                ", sellingMtn='" + sellingMtn + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
