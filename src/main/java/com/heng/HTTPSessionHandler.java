package com.heng;

import com.heng.message.HTTPRequest;
import com.heng.message.HTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

class HTTPSessionHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(HTTPSessionHandler.class);
    private static final int SO_KEEP_ALIVE_TIMEOUT_MS = 5000;

    private final Socket client;
    private final BufferedReader reader;
    private final DataOutputStream output;

    HTTPSessionHandler(Socket client, InputStream is, OutputStream os) {
        log.info("New client {}", client.getRemoteSocketAddress().toString());
        this.client = client;
        this.reader = new BufferedReader(new InputStreamReader(is));
        this.output = new DataOutputStream(os);
    }


    @Override
    public void run() {
        try {
            client.setKeepAlive(true);
            client.setSoTimeout(SO_KEEP_ALIVE_TIMEOUT_MS);

            HTTPRequest request;
            HTTPResponse response;

            do {
                request = new HTTPRequest(reader);
                response = new HTTPResponse(request);
                response.write(output);

            } while (request.isKeepAlive());

            client.close();
        } catch (SocketTimeoutException e) {
            log.info("{} timed out after {} ms, closing.", client.toString(), SO_KEEP_ALIVE_TIMEOUT_MS);
            try {
                client.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            log.warn("Invalid write request. The client may have closed the socket already. {}", e.getMessage());
            //Bug in chrome: may request twice for files like pdf and mp3.
        }

    }
}
