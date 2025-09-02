// src/main/java/com/webserver/WebServerApp.java
package com.webserver;

/**
 * Main application - now with multi-threading!
 */
public class WebServerApp {
    public static void main(String[] args) {
        System.out.println("🌟 Starting Multi-Threaded Web Server...");

        // Configuration
        int port = 8080;
        int threadCount = 10; // How many requests we can handle simultaneously

        // Parse command line arguments if provided
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid port: " + args);
                return;
            }
        }

        if (args.length > 1) {
            try {
                threadCount = Integer.parseInt(args);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid thread count: " + args);
                return;
            }
        }

        // Create and start the server
        MultiThreadedServer server = new MultiThreadedServer(port, threadCount);

        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🚨 Shutdown signal received");
            server.stop();
        }));

        // Start the server (this blocks until server stops)
        server.start();
    }
}
