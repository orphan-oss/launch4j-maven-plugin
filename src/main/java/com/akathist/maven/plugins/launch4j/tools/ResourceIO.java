/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.akathist.maven.plugins.launch4j.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

final public class ResourceIO {

    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

    private ResourceIO() {
        // avoids creating an instance of this class
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = is.read(buf)) != -1) {
            baos.write(buf, 0, bytesRead);
        }

        return baos.toByteArray();
    }

    public static byte[] readResourceAsBytes(String resName) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream is = cl.getResourceAsStream(resName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resName);
            }
            return readAllBytes(is);
        }
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

    public static byte[] readBytes(File inFile) throws IOException {
        try (
                FileInputStream fis = new FileInputStream(inFile);
                BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            return readAllBytes(bis);
        }
    }

    public static void writeBytes(File outFile, byte[] outBytes) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(outFile, false);
                BufferedOutputStream bos = new BufferedOutputStream(fos)
        ) {
            bos.write(outBytes);
            bos.flush();
        }
    }
}
