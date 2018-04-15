package com.heng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class HTTPServer {
    private static final Logger log = LoggerFactory.getLogger(HTTPServer.class);
    private static final int PORT = 80;

    public void start() throws IOException {
        ServerSocket svSocket = new ServerSocket(PORT);
        log.info("HTTP server running on port {}. Go to http://localhost/index.html to get started!", PORT);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        while (true) {
            Socket client = svSocket.accept();
            threadPool.execute(new HTTPSessionHandler(client, client.getInputStream(), client.getOutputStream()));
        }
    }
}
