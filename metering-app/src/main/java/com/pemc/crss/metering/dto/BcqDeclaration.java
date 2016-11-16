package com.pemc.crss.metering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BcqDeclaration {

    private BcqHeader header;
    private List<BcqData> dataList;

}
