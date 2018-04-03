package com.heng.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HTTPResponse {
    private static Logger log = LoggerFactory.getLogger(HTTPResponse.class);
    private static final String VERSION = "HTTP/1.0";
    private byte[] body;

    private String statusLine = VERSION + " ";
    private String contentType;

    public HTTPResponse(HTTPRequest request) {
        switch (request.getMethod()) {
            case HEAD:
                statusLine += Status._200.toString();
                break;
            case GET:
                try {
                    String path = "." + request.getUri();
                    File f = new File(path);
                    if (f.exists()) {
                        contentType = f.getName().split("\\.")[1];
                        log.info("Requested content type: {}", contentType);
                        body = Files.readAllBytes(f.toPath());
                        statusLine += Status._200.toString();
                        log.info("Content found!");
                    } else {
                        statusLine += Status._404.toString();
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
                statusLine += Status._501.toString();
                break;
        }
    }

    public void write(OutputStream os) throws IOException {
        DataOutputStream output = new DataOutputStream(os);
        output.writeBytes(statusLine + "\r\n");

        if (body != null) {
            output.writeBytes("\r\n");
            output.write(body);
        }
        output.writeBytes("\r\n");
        output.flush();
    }
}
