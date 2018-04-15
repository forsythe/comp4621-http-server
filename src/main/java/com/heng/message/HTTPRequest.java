package com.heng.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
    private static final Logger log = LoggerFactory.getLogger(HTTPRequest.class);

    //request line: <method> <uri> <version>
    private RequestMethod method;
    private String uri;
    private String version;

    private final Map<String, String> headers = new HashMap<>();

    public HTTPRequest(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();

        String line = reader.readLine();
        sb.append("\n\n").append(line).append("\n");
        String[] requestLineTokens = line.split("\\s+"); //>=1 spaces

        try {
            method = RequestMethod.valueOf(requestLineTokens[0]);
        } catch (Exception e) {
            throw new RuntimeException("Unknown request method: " + requestLineTokens[0]);
        }
        uri = requestLineTokens[1];
        version = requestLineTokens[2];

        while (!(line = reader.readLine()).isEmpty()) {
            sb.append(line).append("\n");
            String[] keyAndValue = line.split(": ", 2);
            if (keyAndValue.length == 2)
                headers.put(keyAndValue[0], keyAndValue[1]);
        }
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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isKeepAlive() {
        //HTTP/1.1: Persistent by default, unless "Connection: close" specified
        //HTTP/1.0: Must specify "Connection: keep-alive" to make it persistent
        return (version.equals("HTTP/1.1") && !headers.getOrDefault("Connection", "").contains("close"))
                || headers.getOrDefault("Connection", "").contains("keep-alive");

    }
}
