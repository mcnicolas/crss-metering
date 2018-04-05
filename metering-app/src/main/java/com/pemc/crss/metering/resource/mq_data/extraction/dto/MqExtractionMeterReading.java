package com.pemc.crss.metering.resource.mq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Data
public class MqExtractionMeterReading {
    @JsonProperty("reading_datetime")
    private String readingDateTime;

    @JsonProperty("kwh_del")
    private Object kwhd;

    @JsonProperty("kvarh_del")
    private Object kvarhd;

    @JsonProperty("kw_del")
    private Object kwd;

    @JsonProperty("kwh_rec")
    private Object kwhr;

    @JsonProperty("kvarh_rec")
    private Object kvarhr;

    @JsonProperty("kwr")
    private Object kwr;

    @JsonProperty("estimation_flag")
    private String estimationFlag;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MqExtractionMeterReading that = (MqExtractionMeterReading) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getReadingDateTime(), that.getReadingDateTime())
                .append(getKwhd(), that.getKwhd())
                .append(getKvarhd(), that.getKvarhd())
                .append(getKwd(), that.getKwd())
                .append(getKwhr(), that.getKwhr())
                .append(getKvarhr(), that.getKvarhr())
                .append(getKwr(), that.getKwr())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getReadingDateTime())
                .append(getKwhd())
                .append(getKvarhd())
                .append(getKwd())
                .append(getKwhr())
                .append(getKvarhr())
                .append(getKwr())
                .toHashCode();
    }
}
