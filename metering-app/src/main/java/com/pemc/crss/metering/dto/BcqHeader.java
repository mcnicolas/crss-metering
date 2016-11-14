package com.pemc.crss.metering.dto;

import com.pemc.crss.metering.constants.BcqStatus;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

@Data
public class BcqHeader {

    private long bcqHeaderId;
    private long fileId;
    private String sellingMTN;
    private String buyingParticipant;
    private String sellingParticipantName;
    private String sellingParticipantShortName;
    private BcqStatus status;
    private Date declarationDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BcqHeader that = (BcqHeader) o;
        return Objects.equals(bcqHeaderId, that.bcqHeaderId)
                && Objects.equals(fileId, that.fileId)
                && Objects.equals(sellingMTN, that.sellingMTN)
                && Objects.equals(buyingParticipant, that.buyingParticipant)
                && Objects.equals(sellingParticipantName, that.sellingParticipantName)
                && Objects.equals(sellingParticipantShortName, that.sellingParticipantShortName)
                && Objects.equals(status, that.status)
                && Objects.equals(declarationDate, that.declarationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bcqHeaderId, fileId, sellingMTN, buyingParticipant, sellingParticipantName,
                sellingParticipantShortName, status, declarationDate);
    }
}
