package com.pemc.crss.meter.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.isSymbolicLink;

@Slf4j
public class SelectedFileUtils {

    public static List<FileBean> retrieveFileListing(File[] selectedFiles, String[] fileExtensions) {
        List<FileBean> retVal = new ArrayList<>();

        try {
            // TODO: Use Java 8 Stream Parallel instead
            // https://github.com/brettryan/io-recurse-tests
            for (File selectedFile : selectedFiles) {
                Files.walkFileTree(selectedFile.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                        if (isReadable(file) && !isSymbolicLink(file)
                                && isValidFileExtension(file.toString(), fileExtensions)) {

                            FileBean fileBean = new FileBean();
                            fileBean.setPath(file);

                            BasicFileAttributes fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
                            fileBean.setSize(fileAttributes.size());
                            fileBean.setStatus("");
                            fileBean.setElapsedTime("");

                            retVal.add(fileBean);
                        }

                        return super.visitFile(file, attributes);
                    }
                });
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

    private static boolean isValidFileExtension(String filename, String[] allowedExtensions) {
        boolean retVal = false;

        String fileExtension = FilenameUtils.getExtension(filename);
        for (String allowedExtension : allowedExtensions) {
            if (StringUtils.equalsIgnoreCase(fileExtension, allowedExtension)) {
                retVal = true;
                break;
            }
        }

        return retVal;
    }

}
