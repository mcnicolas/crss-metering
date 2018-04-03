package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.constants.BcqStatus;
import com.pemc.crss.metering.constants.BcqUpdateType;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@ToString
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
    private BcqUpdateType updatedVia;
    private BcqUploadFile uploadFile;
    private List<BcqData> dataList;
    private boolean exists;
    private BcqInterval interval;
    private String buyerMtn;
    private Long refMtnSize;
    private String uploadedBy;

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
                && Objects.equals(billingId.toUpperCase(), that.billingId.toUpperCase())
                && Objects.equals(sellingMtn.toUpperCase(), that.sellingMtn.toUpperCase())
                && Objects.equals(buyingParticipantName, that.buyingParticipantName)
                && Objects.equals(buyingParticipantShortName, that.buyingParticipantShortName)
                && Objects.equals(sellingParticipantName, that.sellingParticipantName)
                && Objects.equals(sellingParticipantShortName, that.sellingParticipantShortName)
                && Objects.equals(status, that.status)
                && Objects.equals(tradingDate, that.tradingDate)
                && Objects.equals(deadlineDate, that.deadlineDate)
                && Objects.equals(buyerMtn, that.buyerMtn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                headerId, fileId, billingId, sellingMtn, buyingParticipantName, buyingParticipantShortName,
                sellingParticipantName, sellingParticipantShortName, status, tradingDate, deadlineDate, buyerMtn);
    }

}
