package com.pemc.crss.metering.resource.validator;

import com.pemc.crss.metering.constants.FileType;
import com.pemc.crss.metering.dto.mq.FileParam;
import com.pemc.crss.metering.service.MeterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import static com.pemc.crss.metering.utils.FileTypeUtils.getFileType;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
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
        checkFileType(fileParam.getFileType(), fileParam.getFile(), errors);
        checkEmptyFileList(fileParam.getFile(), errors);
        checkEmptyFileContent(fileParam.getFile(), errors);
        checkFilenameLength(fileParam.getFile(), errors);
    }

    private void checkFilenameLength(MultipartFile[] files, Errors errors) {
        for (MultipartFile file : files) {
            if (file.getOriginalFilename().length() > 100) {
                errors.reject("", "Maximum filename length is 100 characters.");
                break;
            }
        }
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

    private void checkFileType(String selectedFileType, MultipartFile[] files, Errors errors) {
        if (!equalsIgnoreCase(selectedFileType, "XLS") &&
                !equalsIgnoreCase(selectedFileType, "MDEF") &&
                !equalsIgnoreCase(selectedFileType, "CSV")) {
            errors.rejectValue("selectedFileType", "", "Invalid file type. Accepted values are: MDEF, XLS, CSV");
        }

        for (MultipartFile file : files) {
            FileType fileType = getFileType(file.getOriginalFilename());

            if (fileType == null || fileType != FileType.valueOf(selectedFileType.toUpperCase())) {
                String fileTypeErrorMsg = "Invalid file. File Type:" + selectedFileType + " File Name:" + file.getOriginalFilename();

                errors.reject("", fileTypeErrorMsg);

                break;
            }
        }
    }

    private void checkEmptyFileList(MultipartFile[] files, Errors errors) {
        if (isEmpty(files)) {
            errors.reject("", "There must be at least 1 file for upload.");
        }
    }

    private void checkEmptyFileContent(MultipartFile[] files, Errors errors) {
        for (MultipartFile file : files) {
            if (file.getSize() == 0) {
                errors.reject("", "Empty file:" + file.getOriginalFilename());
                break;
            }
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FileParam.class.equals(clazz);
    }

}
