package com.pemc.crss.meter.upload;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.isSymbolicLink;
import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;

// TODO: rename class
public class FileUtils {

    public static List<FileBean> retrieveFileListing(File[] selectedFiles) {
        List<FileBean> retVal = new ArrayList<>();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            Files.walkFileTree(selectedFiles[0].toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    if (isReadable(file) && !isSymbolicLink(file)) {
                        // TODO: Apply file filter

                        System.out.println("Processing file:" + file.toString());
                        try (InputStream source = new DigestInputStream(Files.newInputStream(file), md)) {

                            IOUtils.copy(source, NULL_OUTPUT_STREAM);

                            String hash = bytesToHex(md.digest());

                            System.out.println("File: " + file.getFileName().toString() + " MD5:" + hash);

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
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
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
