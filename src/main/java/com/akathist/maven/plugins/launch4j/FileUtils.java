package com.akathist.maven.plugins.launch4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class FileUtils {

    public static final int DEF_BUF_SIZE = 4 * 1024;

    public static byte[] readResourceAsBytes(String resName) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4 * 1024);
        InputStream is = cl.getResourceAsStream(resName);

        return readAllBytes(is);
    }

    public static byte[] readBytes(File inFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(inFile);
        BufferedInputStream bis = new BufferedInputStream(fis);

        return readAllBytes(bis);
    }

    public static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte buf[] = new byte[DEF_BUF_SIZE];
        int bytesRead = 0;
        while ((bytesRead = is.read(buf)) != -1) {
            baos.write(buf, 0, bytesRead);
        }

        return baos.toByteArray();
    }

    public static void writeBytesIfDiff(File outFile, byte[] outBytes) throws IOException {
        if (outFile.exists()) {
            byte[] existingBytes = readBytes(outFile);
            if (Arrays.equals(outBytes, existingBytes)) {
                return;
            }
        }
        writeBytes(outFile, outBytes);
    }

    public static void writeBytes(File outFile, byte[] outBytes) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(outFile, false);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(outBytes);
            bos.flush();
        }
    }

}
