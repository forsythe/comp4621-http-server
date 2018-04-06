package com.heng.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class HTTPResponse {
    private static Logger log = LoggerFactory.getLogger(HTTPResponse.class);
    private static final String HTTP_VERSION = "HTTP/1.0";
    private byte[] body;

    //Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
    private String statusCodeAndReasonPhrase;
    private String contentType;

    public HTTPResponse(HTTPRequest request) {
        switch (request.getMethod()) {
            case HEAD:
                statusCodeAndReasonPhrase = Status._200.toString();
                break;
            case GET:
                try {
                    File f = new File("." + request.getUri());
                    if (f.exists() && f.isFile()) {
                        String fileExtension = f.getName().split("\\.")[1].toLowerCase();
                        log.info("Requested content type: {}", fileExtension);
                        body = Files.readAllBytes(f.toPath());
                        statusCodeAndReasonPhrase = Status._200.toString();
                        log.info("Content found!");
                        contentType = getContentType(fileExtension);
                    } else {
                        statusCodeAndReasonPhrase = Status._404.toString();
                        body = Status._404.toString().getBytes();
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
        switch (fileExtension) {
            case "css":
                return "Content-Type: text/css";
            case "html":
            case "htm":
                return "Content-Type: text/html";
        }
    }

    public void write(OutputStream os) throws IOException {
        DataOutputStream output = new DataOutputStream(os);
        output.writeBytes(HTTP_VERSION + " " + statusCodeAndReasonPhrase + "\r\n");

        if (body != null) {
            output.writeBytes("\r\n");
            output.write(body);
        }
        output.writeBytes("\r\n");
        output.flush();
    }
}
