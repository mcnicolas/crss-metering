package com.pemc.crss.metering.dto;

import com.pemc.crss.commons.web.dto.AbstractWebDto;

import java.util.Date;

public class BcqUploadFileInfo extends AbstractWebDto<BcqUploadFile> {

    public BcqUploadFileInfo() {
        super(new BcqUploadFile());
    }

    public BcqUploadFileInfo(BcqUploadFile target) {
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
