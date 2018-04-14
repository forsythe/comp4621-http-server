package com.heng;

import com.heng.message.HTTPRequest;
import com.heng.message.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class HTTPRequestHandler implements Runnable {
    private static Logger log = LoggerFactory.getLogger(HTTPRequestHandler.class);
    private final Socket client;

    public HTTPRequestHandler(Socket client) {
        log.info("New client {}", client.getRemoteSocketAddress().toString());
        this.client = client;
    }

    @Override
    public void run() {
        try {
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            HTTPRequest request = new HTTPRequest(reader);
            HTTPResponse response = new HTTPResponse(request);

            response.write(output);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
