package com.pemc.crss.meter.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.isSymbolicLink;
import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;

@Slf4j
public class SelectedFileUtils {

    public static List<FileBean> retrieveFileListing(File[] selectedFiles, String[] fileExtensions) {
        List<FileBean> retVal = new ArrayList<>();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            // TODO: Use Java 8 Stream Parallel instead
            // https://github.com/brettryan/io-recurse-tests
            for (File selectedFile : selectedFiles) {
                Files.walkFileTree(selectedFile.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                        if (isReadable(file) && !isSymbolicLink(file)
                                && isValidFileExtension(file.toString(), fileExtensions)) {

                            log.debug("Processing file:{}", file.toString());

                            try (InputStream source = new DigestInputStream(Files.newInputStream(file), md)) {

                                IOUtils.copy(source, NULL_OUTPUT_STREAM);

                                String hash = bytesToHex(md.digest());

                                FileBean fileBean = new FileBean();
                                fileBean.setPath(file);

                                BasicFileAttributes fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
                                fileBean.setLastModified(fileAttributes.lastModifiedTime());
                                fileBean.setSize(fileAttributes.size());
                                fileBean.setChecksum(hash);

                                retVal.add(fileBean);
                            }
                        }

                        return super.visitFile(file, attributes);
                    }
                });
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
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

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String byteValue = Integer.toHexString(0xFF & aByte);
            hexString.append(byteValue.length() == 2 ? byteValue : "0" + byteValue);
        }
        return hexString.toString();
    }

}
