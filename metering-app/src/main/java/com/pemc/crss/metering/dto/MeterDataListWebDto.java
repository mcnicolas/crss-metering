package com.pemc.crss.metering.dto;

import lombok.Data;

@Data
public class MeterDataListWebDto {

    private Long id;
    private String sein;
    private String readingDate;
    private String kwD;
    private String kwhD;
    private String kwR;
    private String kwhR;
    private String kvarhD;
    private String kvarhR;
    private String van;
    private String vbn;
    private String vcn;
    private String ia;
    private String ib;
    private String ic;
    private String pf;
}
