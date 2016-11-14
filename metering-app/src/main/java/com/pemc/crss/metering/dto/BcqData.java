package com.pemc.crss.metering.dto;

import com.google.common.base.MoreObjects;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

@Data
public class BcqData {

    private long bcqDataId;
    private long bcqHeaderId;
    private String referenceMTN;
    private Date startTime;
    private Date endTime;
    private float bcq;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getBcqDataId())
                .add("fileId", getBcqHeaderId())
                .add("referenceMTN", getReferenceMTN())
                .add("startTime", getStartTime())
                .add("endTime", getEndTime())
                .add("bcq", getBcq())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BcqData that = (BcqData) o;
        return Objects.equals(bcqDataId, that.bcqDataId)
                && Objects.equals(bcqHeaderId, that.bcqHeaderId)
                && Objects.equals(referenceMTN, that.referenceMTN)
                && Objects.equals(startTime, that.startTime)
                && Objects.equals(endTime, that.endTime)
                && Objects.equals(bcq, that.bcq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bcqDataId, bcqHeaderId, referenceMTN, startTime, endTime, bcq);
    }
}
