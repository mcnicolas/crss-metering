package com.pemc.crss.metering.dto;

import com.google.common.base.MoreObjects;
import lombok.Data;

import java.util.Date;

@Data
public class BCQData {

    private long bcqDataId;
    private long fileId;
    private String sellingMTN;
    private String buyingParticipant;
    private String referenceMTN;
    private Date startTime;
    private Date endTime;
    private float bcq;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getBcqDataId())
                .add("fileId", getFileId())
                .add("sellingMTN", getSellingMTN())
                .add("buyingParticipant", getBuyingParticipant())
                .add("referenceMTN", getReferenceMTN())
                .add("startTime", getStartTime())
                .add("endTime", getEndTime())
                .add("bcq", getBcq())
                .toString();
    }

}
