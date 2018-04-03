package com.heng;

import com.heng.message.RequestMethod;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        HTTPServer sv = new HTTPServer();
        sv.start();
    }
}
