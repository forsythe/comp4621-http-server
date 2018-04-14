package com.heng.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

public class HTTPResponse {
    private static final String CRLF = "\r\n";
    private static Logger log = LoggerFactory.getLogger(HTTPResponse.class);
    private static final String HTTP_VERSION = "HTTP/1.1";

    private byte[] body;
    private boolean useGzip;
    private boolean useChunkedEncoding;

    //Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
    private String statusCodeAndReasonPhrase = "";
    private String contentType = "";

    public HTTPResponse(HTTPRequest request) {
        useGzip = request.getHeaders().getOrDefault("Accept-Encoding", "").contains("gzip");
        useChunkedEncoding = request.getVersion().equals("HTTP/1.1");

        switch (request.getMethod()) {
            case HEAD:
                statusCodeAndReasonPhrase = Status._200.toString();
                break;
            case GET:
                try {
                    File f = new File("." + request.getUri());
                    if (f.exists()) {
                        if (f.isFile()) {
                            String fileExtension = f.getName().split("\\.")[1];
                            log.info("Requested content type: {}", fileExtension);
                            body = Files.readAllBytes(f.toPath());

                            statusCodeAndReasonPhrase = Status._200.toString();
                            log.info("Content found!");
                            contentType = getContentType(fileExtension);
                        } else if (f.isDirectory()) {
                            log.info("Requested directory: {}", f.getPath());

                            StringBuilder result = new StringBuilder("<html><head><title>Index of ");
                            result.append(f.getPath())
                                    .append("</title></head><body><h1>Index of ")
                                    .append(f.getPath())
                                    .append("</h1><hr><pre>");

                            File[] files = f.listFiles();
                            if (f.getParent() != null) {
                                result.append("<b><a href=\"/" + f.getParent() + "\">Parent Directory</a></b>\n");
                            }

                            if (files != null) {
                                for (File subfile : files) {
                                    result.append(" <a href=\"/" + subfile.getPath() + "\">" + subfile.getPath() + "</a>\n");
                                }
                            }
                            result.append("<hr></pre></body></html>");
                            body = result.toString().getBytes();

                            //contentType = "application/x-directory";
                        }
                    } else {
                        statusCodeAndReasonPhrase = Status._404.toString();
                        File error404Page = new File("error-404-page.html");
                        body = Files.readAllBytes(error404Page.toPath());
                        log.info("Content not found");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }
                break;

            case OPTIONS:
            case POST:
            case PUT:
            case DELETE:
            case TRACE:
            case CONNECT:
                statusCodeAndReasonPhrase = Status._501.toString();
                break;
        }
    }

    private String getContentType(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case "css":
                return "text/css";
            case "txt":
                return "text/plain";
            case "html":
            case "htm":
                return "text/html";
            case "pdf":
                return "application/pdf";
            case "jpg":
                return "image/jpeg";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            default:
                return "application/octet-stream"; //aka unknown
        }
    }


    public void write(DataOutputStream output) throws IOException {
        String statusLine = HTTP_VERSION + " " + statusCodeAndReasonPhrase;
        writeLine(output, statusLine);
        log.info(statusLine);

        /*
        HTTP servers often use compression to optimize transmission, for example with Content-Encoding: gzip or
        Content-Encoding: deflate. If both compression and chunked encoding are enabled, then the content stream is
        first compressed, then chunked; so the chunk encoding itself is not compressed, and the data in each chunk is
        not compressed individually. The remote endpoint then decodes the stream by concatenating the chunks and
        uncompressing the result.
         */
        log.info("{} gzip", useGzip ? "Using" : "Not using");
        log.info("{} chunked transfer encoding", useChunkedEncoding ? "Using" : "Not using");

        if (useGzip && useChunkedEncoding) {
            writeHeaderKeyPair(output, "Content-Encoding", "gzip");
            writeHeaderKeyPair(output, "Transfer-Encoding", "chunked");
        } else if (!useGzip && useChunkedEncoding) {
            writeHeaderKeyPair(output, "Content-Encoding", "identity");
            writeHeaderKeyPair(output, "Transfer-Encoding", "chunked");
        } else if (useGzip && !useChunkedEncoding) {
            writeHeaderKeyPair(output, "Content-Encoding", "gzip");
        } else { //neither gzip nor chunked encoding
            writeHeaderKeyPair(output, "Content-Encoding", "identity");
            writeHeaderKeyPair(output, "Content-Length", String.valueOf(body.length)); //don't use when using gzip
        }

        writeHeaderKeyPair(output, "Content-Type", contentType);
        writeHeaderKeyPair(output, "Connection", "close");

        if (body != null) {
            output.writeBytes(CRLF);
            if (useGzip && useChunkedEncoding) {

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream(body.length);
                GZIPOutputStream gzip = new GZIPOutputStream(byteStream);
                gzip.write(body);
                gzip.finish();

                ChunkedOutputStream cos = new ChunkedOutputStream(output);
                cos.write(byteStream.toByteArray());
                cos.finish();

            } else if (!useGzip && useChunkedEncoding) {
                ChunkedOutputStream cos = new ChunkedOutputStream(output);
                cos.write(body);
                cos.finish();
            } else if (useGzip && !useChunkedEncoding) {
                GZIPOutputStream gzip = new GZIPOutputStream(output);
                gzip.write(body);
                gzip.finish();
            } else { //neither gzip nor chunked encoding
                output.write(body);
            }
        }

        output.writeBytes(CRLF);
        output.flush();
    }

    private void writeHeaderKeyPair(DataOutputStream output, String key, String value) throws IOException {
        writeLine(output, key + ": " + value);
    }

    private void writeLine(DataOutputStream output, String value) throws IOException {
        output.writeBytes(value + CRLF);
    }

    private void writeLineBytes(OutputStream output, byte[] value) throws IOException {
        output.write(value);
        output.write(new byte[]{'\r', '\n'});
    }

    class ChunkedOutputStream {
        private static final int DEFAULT_BUFFER_SIZE = 2048;
        private final int bufferSize;
        OutputStream os;

        public ChunkedOutputStream(OutputStream os) {
            this.os = os;
            bufferSize = DEFAULT_BUFFER_SIZE;
        }

        public ChunkedOutputStream(OutputStream os, int bufferSize) {
            this.os = os;
            this.bufferSize = bufferSize;
        }


        public void write(byte[] bytes) throws IOException {
            InputStream is = new ByteArrayInputStream(bytes);
            byte[] tempBuffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = is.read(tempBuffer, 0, bufferSize)) != -1) {
                //System.out.println("bytesRead = " + bytesRead);
//                System.out.println(Integer.toHexString(bytesRead));
//                System.out.println(Arrays.toString(Arrays.copyOfRange(tempBuffer, 0, bytesRead)));
                writeLineBytes(os, Integer.toHexString(bytesRead).getBytes());
                writeLineBytes(os, Arrays.copyOfRange(tempBuffer, 0, bytesRead));
//                writeLine(os, Integer.toHexString(bytesRead));
//                writeLine(os, new String(tempBuffer, "UTF-8").substring(0, bytesRead));
            }
            //System.out.println("0");
        }

        public void finish() throws IOException {
            writeLineBytes(os, new byte[]{'0'});
        }
    }
}
