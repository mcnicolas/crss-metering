package com.pemc.crss.metering.resource.validator;

import com.pemc.crss.metering.dto.mq.FileParam;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import static com.pemc.crss.metering.utils.FileTypeUtils.getFileType;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Slf4j
@Component
public class FileUploadValidator implements Validator {

    private final MeterService meterService;

    @Autowired
    public FileUploadValidator(MeterService meterService) {
        this.meterService = meterService;
    }

    @Override
    public void validate(Object target, Errors errors) {
        FileParam fileParam = (FileParam) target;

        checkHeaderID(fileParam.getHeaderID(), errors);
        checkMSPShortName(fileParam.getMspShortName(), errors);
        checkFileType(fileParam.getFileType(), errors);
        checkFile(fileParam.getFileType(), fileParam.getFile(), errors);
    }

    private void checkHeaderID(Long headerID, Errors errors) {
        boolean valid = meterService.isHeaderValid(headerID);

        if (!valid) {
            errors.rejectValue("headerID", "", "HeaderID is not valid.");
        }
    }

    private void checkMSPShortName(String mspShortName, Errors errors) {
        // TODO: How to access MSP Listing
    }

    private void checkFileType(String fileType, Errors errors) {
        if (!equalsIgnoreCase(fileType, "XLS") &&
                !equalsIgnoreCase(fileType, "MDEF") &&
                !equalsIgnoreCase(fileType, "CSV")) {
            errors.rejectValue("fileType", "", "Invalid file type. Accepted values are: MDEF, XLS, CSV");
        }
    }

    private void checkFile(String fileType, MultipartFile[] files, Errors errors) {
        if (ArrayUtils.isEmpty(files)) {
            errors.reject("", "There must be at least 1 file for upload.");
        }

        for (MultipartFile file: files) {
            if (file.getSize() == 0) {
                errors.reject("", "Empty file:" + file.getOriginalFilename());
            }

            // TODO: Ugly code. Refactor!
            String fileTypeErrorMsg = "Invalid file. File Type:" + fileType + " File Name:" + file.getOriginalFilename();
            switch (getFileType(file.getOriginalFilename())) {
                case MDEF:
                    if (!equalsIgnoreCase(fileType, "MDEF")) {
                        errors.reject("", fileTypeErrorMsg);
                    }
                    break;
                case XLS:
                    if (!equalsIgnoreCase(fileType, "XLS")) {
                        errors.reject("", fileTypeErrorMsg);
                    }
                    break;
                case CSV:
                    if (!equalsIgnoreCase(fileType, "CSV")) {
                        errors.reject("", fileTypeErrorMsg);
                    }
            }

            if (file.getOriginalFilename().length() > 100) {
                errors.reject("", "Maximum filename length is 100 characters.");
            }
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FileParam.class.equals(clazz);
    }

}
