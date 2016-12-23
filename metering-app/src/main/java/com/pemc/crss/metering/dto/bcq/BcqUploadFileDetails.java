package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.commons.web.dto.AbstractWebDto;
import com.pemc.crss.metering.dto.BcqUploadFile;

import java.util.Date;

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

    public Date getSubmittedDate() {
        return target().getSubmittedDate();
    }

    public void setSubmittedDate(Date submittedDate) {
        target().setSubmittedDate(submittedDate);
    }

}

