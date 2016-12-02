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
    private String sellingMtn;
    private String buyingParticipant;
    private String sellingParticipantName;
    private String sellingParticipantShortName;
    private BcqStatus status;
    private Date tradingDate;
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
                && Objects.equals(sellingMtn, that.sellingMtn)
                && Objects.equals(buyingParticipant, that.buyingParticipant)
                && Objects.equals(sellingParticipantName, that.sellingParticipantName)
                && Objects.equals(sellingParticipantShortName, that.sellingParticipantShortName)
                && Objects.equals(status, that.status)
                && Objects.equals(tradingDate, that.tradingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerId, fileId, sellingMtn, buyingParticipant, sellingParticipantName,
                sellingParticipantShortName, status, tradingDate);
    }
}
