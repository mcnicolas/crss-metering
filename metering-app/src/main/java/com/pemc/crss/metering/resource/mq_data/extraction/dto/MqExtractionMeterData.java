package com.pemc.crss.metering.resource.mq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonPropertyOrder({"transaction_id", "upload_datetime", "record_count", "meter_readings"})
public class MqExtractionMeterData {

    @JsonProperty("transaction_id")
    private final String transactionId;

    @JsonProperty("upload_datetime")
    private final String uploadDateTime;

    @JsonProperty("meter_readings")
    private List<MqExtractionMeterReading> meterReadings = new ArrayList<>();

    public MqExtractionMeterData(String uploadDateTime, String transactionId) {
        this.uploadDateTime = uploadDateTime;
        this.transactionId = transactionId;
    }

    @JsonProperty("record_count")
    public int getRecordCount() {
        return CollectionUtils.isNotEmpty(meterReadings) ? meterReadings.size() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MqExtractionMeterData that = (MqExtractionMeterData) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getUploadDateTime(), that.getUploadDateTime())
                .append(getTransactionId(), that.getTransactionId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getUploadDateTime())
                .append(getTransactionId())
                .toHashCode();
    }


}
