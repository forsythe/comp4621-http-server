package com.heng;

import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HTTPRequestHandler implements Runnable {
    private static Logger log = Logger.getLogger(HTTPRequestHandler.class);
    private final Socket client;

    public HTTPRequestHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            DataInputStream inputFromClient = new DataInputStream(client.getInputStream());
            DataOutputStream outputToClient = new DataOutputStream(client.getOutputStream());

            outputToClient.writeBytes("Sup lol this is a test");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
