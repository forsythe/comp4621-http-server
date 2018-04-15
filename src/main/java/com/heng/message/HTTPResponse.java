package com.heng.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;

public class HTTPResponse {
    private static final File ERROR_404_PAGE = new File("error-404-page.html");
    private static final String CRLF = "\r\n";
    private static final Logger log = LoggerFactory.getLogger(HTTPResponse.class);
    private static final String HTTP_VERSION = "HTTP/1.1";

    private final HTTPRequest request;
    private byte[] body;
    private final boolean useGzip;
    private final boolean useChunkedEncoding;

    //Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
    private String statusCodeAndReasonPhrase = "";
    private String contentType = "";

    public HTTPResponse(HTTPRequest request) {
        this.request = request;

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
                            contentType = ContentType.getContentType(fileExtension);
                        } else if (f.isDirectory()) {
                            log.info("Requested directory: {}", f.getPath());
                            body = generateDirectoryHtml(f).getBytes();
                        }
                    } else {
                        statusCodeAndReasonPhrase = Status._404.toString();
                        body = Files.readAllBytes(ERROR_404_PAGE.toPath());
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

    private String generateDirectoryHtml(File f) {
        StringBuilder result = new StringBuilder("<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"/main.css\"><title>Index of ");
        result.append(f.getPath())
                .append("</title></head><body=\"simple-container\"><h1>Index of ")
                .append(f.getPath())
                .append("</h1><hr><pre>");

        File[] files = f.listFiles();
        if (f.getParent() != null) {
            result.append("<b><a href=\"/").append(f.getParent()).append("\">Parent Directory</a></b>\n");
        }

        if (files != null) {
            for (File subfile : files) {
                result.append(" <a href=\"/").append(subfile.getPath()).append("\">").append(subfile.getPath()).append("</a>\n");
            }
        }
        result.append("<hr></pre></body></html>");
        return result.toString();
    }

    public void write(DataOutputStream output) throws IOException {
        String statusLine = HTTP_VERSION + " " + statusCodeAndReasonPhrase;
        log.info(statusLine);

        writeLine(output, statusLine);

        writeHeaderLines(output);
        writeBodyLines(output);

        output.writeBytes(CRLF);
        output.flush();
    }


    private void writeHeaderLines(DataOutputStream output) throws IOException {
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
        writeHeaderKeyPair(output, "Connection", request.isKeepAlive() ? "keep-alive" : "close");
    }

    private void writeBodyLines(DataOutputStream output) throws IOException {
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
    }

    private static void writeHeaderKeyPair(DataOutputStream output, String key, String value) throws IOException {
        writeLine(output, key + ": " + value);
    }

    private static void writeLine(DataOutputStream output, String value) throws IOException {
        output.writeBytes(value + CRLF);
    }
}
