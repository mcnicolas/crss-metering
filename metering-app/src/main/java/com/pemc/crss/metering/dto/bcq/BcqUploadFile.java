package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.metering.constants.ValidationStatus;
import lombok.Data;

import java.util.Date;

@Data
public class BcqUploadFile {

    private long fileId;
    private String transactionId;
    private String fileName;
    private long fileSize;
    private Date submittedDate;
    private ValidationStatus validationStatus;

}
