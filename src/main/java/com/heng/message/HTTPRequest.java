package com.heng.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class HTTPRequest {
    private static Logger log = LoggerFactory.getLogger(HTTPRequest.class);

    //request line: <method> <uri> <version>
    private RequestMethod method;
    private String uri;
    private String version;
    private boolean keepAlive = true;

    public HTTPRequest(BufferedReader reader) throws IOException {
        StringBuffer sb = new StringBuffer();
//
//        String line;
//        while ((line = reader.readLine()) == null) {
//            System.out.println(line);
//        }

        String line = reader.readLine();
        sb.append("\n").append(line).append("\n");
        System.out.println("Request line: " + line);
        String[] requestLineTokens = line.split("\\s+"); //>=1 spaces

        try {
            method = RequestMethod.valueOf(requestLineTokens[0]);
        } catch (Exception e) {
            throw new RuntimeException("Unknown request method: " + requestLineTokens[0]);
        }
        uri = requestLineTokens[1];
        version = requestLineTokens[2];

        //keepAlive = "HTTP/1.1".equals(version) && (connection == null || !connection.matches("(?i).*close.*"));

        while (!(line = reader.readLine()).isEmpty()) {
            sb.append(line).append("\n");
        }
        //System.out.println(sb.toString());
        log.info(sb.toString());

    }

    public RequestMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getVersion() {
        return version;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }
}
