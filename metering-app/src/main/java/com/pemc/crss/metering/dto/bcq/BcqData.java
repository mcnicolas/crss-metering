package com.pemc.crss.metering.dto.bcq;

import com.google.common.base.MoreObjects;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Data
public class BcqData {

    private long dataId;
    private long headerId;
    private String referenceMtn;
    private Date startTime;
    private Date endTime;
    private BigDecimal bcq;
    private String buyerMtn;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getDataId())
                .add("fileId", getHeaderId())
                .add("referenceMtn", getReferenceMtn())
                .add("startTime", getStartTime())
                .add("endTime", getEndTime())
                .add("bcq", getBcq())
                .add("buyerMtn", getBcq())
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
        return Objects.equals(dataId, that.dataId)
                && Objects.equals(headerId, that.headerId)
                && Objects.equals(referenceMtn, that.referenceMtn)
                && Objects.equals(startTime, that.startTime)
                && Objects.equals(endTime, that.endTime)
                && Objects.equals(buyerMtn, that.buyerMtn)
                && Objects.equals(bcq, that.bcq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataId, headerId, referenceMtn, startTime, endTime, bcq, buyerMtn);
    }
}
