package com.pemc.crss.meter.upload.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class FileNameFilter extends FileFilter {

    private final String description;
    private final String[] extensions;
    private final String[] lowerCaseExtensions;

    public FileNameFilter(String description, String... extensions) {
        if (extensions == null || extensions.length == 0) {
            throw new IllegalArgumentException(
                    "Extensions must be non-null and not empty");
        }
        this.description = description;
        this.extensions = new String[extensions.length];
        this.lowerCaseExtensions = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            if (extensions[i] == null || extensions[i].length() == 0) {
                throw new IllegalArgumentException(
                        "Each extension must be non-null and not empty");
            }
            this.extensions[i] = extensions[i];
            lowerCaseExtensions[i] = extensions[i].toLowerCase(Locale.ENGLISH);
        }
    }

    public boolean accept(File f) {
        boolean retVal = false;

        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            String fileName = f.getName();

            if (!equalsIgnoreCase(fileName.substring(0, 1), "~")) {
                int i = fileName.lastIndexOf('.');
                if (i > 0 && i < fileName.length() - 1) {
                    String desiredExtension = fileName.substring(i+1).
                            toLowerCase(Locale.ENGLISH);
                    for (String extension : lowerCaseExtensions) {
                        if (desiredExtension.equals(extension)) {
                            retVal = true;
                        }
                    }
                }
            }
        }

        return retVal;
    }

    public String getDescription() {
        return description;
    }

    public String[] getExtensions() {
        String[] result = new String[extensions.length];
        System.arraycopy(extensions, 0, result, 0, extensions.length);
        return result;
    }

    public String toString() {
        return super.toString() + "[description=" + getDescription() +
                " extensions=" + java.util.Arrays.asList(getExtensions()) + "]";
    }

}
