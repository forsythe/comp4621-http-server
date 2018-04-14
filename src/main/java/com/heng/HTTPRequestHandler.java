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

            HTTPRequest request;
            HTTPResponse response;
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            DataOutputStream output = new DataOutputStream(client.getOutputStream());

            client.setSoTimeout(5000);
            do {
//                int available = client.getInputStream().available();
//                System.out.println("available = " + available);
                request = new HTTPRequest(reader);
                response = new HTTPResponse(request);
                response.write(output);
            } while (request.isKeepAlive());

            client.close();

        } catch (SocketTimeoutException e) {
            log.info("Socket timed out, closing");
            try {
                client.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
