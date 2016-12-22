package com.pemc.crss.metering.dto.bcq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
public class BcqItem {

    private String sellingMtn;
    private String billingId;
    private List<String> referenceMtns;

}
