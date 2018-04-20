package com.pemc.crss.metering.resource.mq_data.extraction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;

@Data
//@JsonPropertyOrder({"reading_datetime", "kwh_del", "kvarh_del", "kw_del", "kwh_rec", "kvarh_rec", "kwr", "estimation_flag"})
public class MqExtractionMeterReading {
//    @JsonProperty("reading_datetime")
//    private String readingDateTime;
//
//    @JsonProperty("kwh_del")
//    private Object kwhd;
//
//    @JsonProperty("kvarh_del")
//    private Object kvarhd;
//
//    @JsonProperty("kw_del")
//    private Object kwd;
//
//    @JsonProperty("kwh_rec")
//    private Object kwhr;
//
//    @JsonProperty("kvarh_rec")
//    private Object kvarhr;
//
//    @JsonProperty("kwr")
//    private Object kwr;
//
//    @JsonProperty("estimation_flag")
//    private String estimationFlag;

    @JsonProperty("value")
    private final String value;


    public MqExtractionMeterReading(String readingDateTime,
                                    Object okwhd,
                                    Object okvarhd,
                                    Object okwd,
                                    Object okwhr,
                                    Object okvarhr,
                                    Object okwr,
                                    String estimationFlag) {
        String kwhd = okwhd != null ? formatNumber(okwhd) : null;
        String kvarhd = okvarhd != null ? formatNumber(okvarhd) : null;
        String kwd = okwd != null ? formatNumber(okwd) : null;
        String kwhr = okwhr != null ? formatNumber(okwhr) : null;
        String kvarhr = okvarhr != null ? formatNumber(okvarhr) : null;
        String kwr = okwr != null ? formatNumber(okwr) : null;
        this.value = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                readingDateTime,
                kwhd,
                kvarhd,
                kwd,
                kwhr,
                kvarhr,
                kwr,
                estimationFlag);
    }

    private String formatNumber(Object o) {

        if (o instanceof BigDecimal) {
            BigDecimal bigD = (BigDecimal) o;
            return bigD.toPlainString();
        }

        return "";
    }

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
                .append(getValue(), that.getValue())
//                .append(getReadingDateTime(), that.getReadingDateTime())
//                .append(getKwhd(), that.getKwhd())
//                .append(getKvarhd(), that.getKvarhd())
//                .append(getKwd(), that.getKwd())
//                .append(getKwhr(), that.getKwhr())
//                .append(getKvarhr(), that.getKvarhr())
//                .append(getKwr(), that.getKwr())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(getValue())
//                .append(getReadingDateTime())
//                .append(getKwhd())
//                .append(getKvarhd())
//                .append(getKwd())
//                .append(getKwhr())
//                .append(getKvarhr())
//                .append(getKwr())
                .toHashCode();
    }
}
