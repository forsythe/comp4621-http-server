package com.heng.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

class ChunkedOutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private int bufferSize;
    private OutputStream os;

    ChunkedOutputStream(OutputStream os) {
        this.os = os;
        bufferSize = DEFAULT_BUFFER_SIZE;
    }

    void write(byte[] bytes) throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        byte[] tempBuffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = is.read(tempBuffer, 0, bufferSize)) != -1) {
            writeLineBytes(os, Integer.toHexString(bytesRead).getBytes());
            writeLineBytes(os, Arrays.copyOfRange(tempBuffer, 0, bytesRead));
        }
    }

    void finish() throws IOException {
        writeLineBytes(os, new byte[]{'0'});
    }

    private static void writeLineBytes(OutputStream output, byte[] value) throws IOException {
        output.write(value);
        output.write(new byte[]{'\r', '\n'});
    }
}