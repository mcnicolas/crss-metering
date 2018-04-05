package com.pemc.crss.metering.resource.mq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

@Data
@JsonPropertyOrder({"meterdata_category", "sein", "msp_shortname", "trading_date", "columns", "meter_data"})
public class MqExtractionHeader {

    @JsonProperty("meterdata_category")
    private final String category;

    @JsonProperty("sein")
    private final String sein;

    @JsonProperty("msp_shortname")
    private final String mspShortName;

    @JsonProperty("trading_date")
    private final String tradingDate;

    @JsonProperty("columns")
    private final String columns = "reading_datetime,kwh_del,kvarh_del,kw_del,kwh_rec,kvarh_rec,kwr,estimation_flag";

    @JsonProperty("meter_data")
    private final List<MqExtractionMeterData> meterData;

    public MqExtractionHeader(String category, String sein, String mspShortName, String tradingDate, List<MqExtractionMeterData> meterData) {
        this.category = category;
        this.sein = sein;
        this.mspShortName = mspShortName;
        this.tradingDate = tradingDate;
        this.meterData = meterData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MqExtractionHeader that = (MqExtractionHeader) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(getCategory(), that.getCategory())
                .append(getSein(), that.getSein())
                .append(getMspShortName(), that.getMspShortName())
                .append(getTradingDate(), that.getTradingDate())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getCategory())
                .append(getSein())
                .append(getMspShortName())
                .append(getTradingDate())
                .toHashCode();
    }

}
