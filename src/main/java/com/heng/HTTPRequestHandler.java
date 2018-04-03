package com.heng;

import com.heng.message.HTTPRequest;
import com.heng.message.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class HTTPRequestHandler implements Runnable {
    private static Logger log = LoggerFactory.getLogger(HTTPRequestHandler.class);
    private final Socket client;

    public HTTPRequestHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            //DataInputStream inputFromClient = new DataInputStream(client.getInputStream());
            //DataOutputStream outputToClient = new DataOutputStream(client.getOutputStream());
            HTTPRequest request = new HTTPRequest(client.getInputStream());
            HTTPResponse response = new HTTPResponse(request);
            response.write(client.getOutputStream());

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
