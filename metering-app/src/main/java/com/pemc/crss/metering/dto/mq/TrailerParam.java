package com.pemc.crss.metering.dto.mq;

import com.pemc.crss.metering.resource.validator.ExistingHeaderID;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TrailerParam {

    @NotNull
    @ExistingHeaderID
    private Long headerID;

}
