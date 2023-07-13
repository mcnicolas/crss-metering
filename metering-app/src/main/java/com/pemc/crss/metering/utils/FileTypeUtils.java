package com.pemc.crss.metering.utils;

import com.pemc.crss.metering.constants.FileType;
import org.apache.commons.io.FilenameUtils;

import static com.pemc.crss.metering.constants.FileType.CSV;
import static com.pemc.crss.metering.constants.FileType.MDEF;
import static com.pemc.crss.metering.constants.FileType.XLS;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public final class FileTypeUtils {

    private FileTypeUtils() {
    }

    public static FileType getFileType(String filename) {
        FileType retVal = null;

        String fileExt = FilenameUtils.getExtension(filename);

        if (equalsIgnoreCase(fileExt, "XLS") || equalsIgnoreCase(fileExt, "XLSX")) {
            retVal = XLS;
        } else if (equalsIgnoreCase(fileExt, "MDE") || equalsIgnoreCase(fileExt, "MDEF") || equalsIgnoreCase(fileExt, "MDF")) {
            retVal = MDEF;
        } else if (equalsIgnoreCase(fileExt, "CSV")) {
            retVal = CSV;
        }

        return retVal;
    }

}
