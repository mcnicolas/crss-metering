package com.pemc.crss.metering.dto.mq;

import com.pemc.crss.metering.resource.validator.ValidCategory;
import com.pemc.crss.metering.resource.validator.ValidMSP;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ToString
@Data
public class HeaderParam {

    @NotNull
    @Min(value = 1, message = "fileCount should be a number between 1 to 10000.")
    @Max(value = 10000, message = "fileCount should be a number between 1 to 10000.")
    private Integer fileCount;

    @NotNull
    @ValidCategory
    private String category;

    @NotNull
    @ValidMSP
    private String mspShortName;

}
