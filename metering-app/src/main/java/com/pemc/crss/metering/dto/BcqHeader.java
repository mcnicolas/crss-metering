package com.pemc.crss.metering.dto;

import com.pemc.crss.metering.constants.BcqStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
public class BcqHeader {

    private long headerId;
    private long fileId;
    private String billingId;
    private String sellingMtn;
    private String buyingParticipantName;
    private String buyingParticipantShortName;
    private String sellingParticipantName;
    private String sellingParticipantShortName;
    private BcqStatus status;
    private Date tradingDate;
    private Date deadlineDate;
    private String updatedVia;
    private BcqUploadFile uploadFile;
    private List<BcqData> dataList;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BcqHeader that = (BcqHeader) o;
        return Objects.equals(headerId, that.headerId)
                && Objects.equals(fileId, that.fileId)
                && Objects.equals(billingId, that.billingId)
                && Objects.equals(sellingMtn, that.sellingMtn)
                && Objects.equals(buyingParticipantName, that.buyingParticipantName)
                && Objects.equals(buyingParticipantShortName, that.buyingParticipantShortName)
                && Objects.equals(sellingParticipantName, that.sellingParticipantName)
                && Objects.equals(sellingParticipantShortName, that.sellingParticipantShortName)
                && Objects.equals(status, that.status)
                && Objects.equals(tradingDate, that.tradingDate)
                && Objects.equals(deadlineDate, that.deadlineDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerId, fileId, billingId, sellingMtn, buyingParticipantName, buyingParticipantShortName,
                sellingParticipantName, sellingParticipantShortName, status, tradingDate, deadlineDate);
    }
}
