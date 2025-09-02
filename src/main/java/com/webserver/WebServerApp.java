
package com.webserver;

public class WebServerApp {
    public static void main(String[] args) {
        System.out.println("🌟 Starting Simple Web Server...");

        SimpleServer server = new SimpleServer(8080);
        server.start();
    }
}
