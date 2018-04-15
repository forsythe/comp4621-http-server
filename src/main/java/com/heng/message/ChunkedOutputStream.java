package com.heng.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

class ChunkedOutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    public static final byte[] CRLF = {'\r', '\n'};
    private final int bufferSize;
    private final OutputStream os;

    ChunkedOutputStream(OutputStream os) {
        this.os = os;
        bufferSize = DEFAULT_BUFFER_SIZE;
    }

    void write(byte[] bytes) throws IOException {
        InputStream is = new ByteArrayInputStream(bytes);
        byte[] tempBuffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = is.read(tempBuffer, 0, bufferSize)) != -1) {
            byte[] size = Integer.toHexString(bytesRead).getBytes();
            writeLineBytes(os, size, 0, size.length);
            writeLineBytes(os, tempBuffer, 0, bytesRead);
            //writeLineBytes(os, Arrays.copyOfRange(tempBuffer, 0, bytesRead));
        }
    }

    void finish() throws IOException {
        writeLineBytes(os, new byte[]{'0'}, 0, 1);
    }

    private static void writeLineBytes(OutputStream output, byte[] value, int offset, int length) throws IOException {
        output.write(value, offset, length);
        output.write(CRLF);
    }
}