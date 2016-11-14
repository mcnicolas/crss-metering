package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BcqHeaderDataPair {

    private BcqHeader header;
    private List<BcqData> dataList;

}
