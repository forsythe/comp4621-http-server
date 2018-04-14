package com.heng.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class HTTPResponse {
    public static final String CRLF = "\r\n";
    private static Logger log = LoggerFactory.getLogger(HTTPResponse.class);
    private static final String HTTP_VERSION = "HTTP/1.1";
    private byte[] body;

    //Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
    private String statusCodeAndReasonPhrase = "";
    private String contentType = "";

    public HTTPResponse(HTTPRequest request) {
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
        output.writeBytes(statusLine + CRLF);
        System.out.println(statusLine);

        if (!contentType.isEmpty()) {
            output.writeBytes("Content-Type: " + contentType + CRLF);
        }
        output.writeBytes("Content-Length: " + body.length + CRLF); //dont use when using gzip
        output.writeBytes("Connection: keep-alive" + CRLF);
        //output.writeBytes("Keep-Alive: timeout=15, max=100" + CRLF);

        if (body != null) {
            output.writeBytes(CRLF);
            output.write(body);
        }
        output.writeBytes(CRLF);
        output.flush();
    }
}
