package com.pemc.crss.metering.resource.mq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

@Data
public abstract class MqExtractionHeader {

    @JsonProperty("meterdata_category")
    private final String category;

    @JsonProperty("sein")
    private String sein;

    @JsonProperty("msp_shortname")
    private String mspShortName;

    @JsonProperty("meter_data")
    private List<MqExtractionMeterData> meterData;

    protected MqExtractionHeader(String category, String mspShortName, String sein) {
        this.category = category;
        this.mspShortName = mspShortName;
        this.sein = sein;
    }

}
