package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.commons.web.dto.AbstractWebDto;
import com.pemc.crss.metering.constants.ValidationStatus;

public class BcqUploadFileDetails extends AbstractWebDto<BcqUploadFile> {

    public BcqUploadFileDetails() {
        super(new BcqUploadFile());
    }

    public BcqUploadFileDetails(BcqUploadFile target) {
        super(target);
    }

    public String getFileName() {
        return target().getFileName();
    }

    public void setFileName(String fileName) {
        target().setFileName(fileName);
    }

    public long getFileSize() {
        return target().getFileSize();
    }

    public void setFileSize(long fileSize) {
        target().setFileSize(fileSize);
    }

    public ValidationStatus getValidationStatus() {
        return target().getValidationStatus();
    }

    public void setValidationStatus(ValidationStatus validationStatus) {
        target().setValidationStatus(validationStatus);
    }

}

