package com.pemc.crss.meter.upload;

import lombok.Data;

import java.util.Date;

@Data
public class StaleStatusMonitor {

    private int checksum = -1;
    private Date updateTimestamp = new Date();

}
