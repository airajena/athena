// src/main/java/com/webserver/WebServerApp.java
package com.webserver;

import com.webserver.metrics.MetricsCollector;
import com.webserver.metrics.MetricsServer;

public class WebServerApp {
    public static void main(String[] args) {
        System.out.println("🌟 Starting Multi-Threaded Web Server...");

        // Configuration with defaults
        int port = 8080;
        int threadCount = 10;
        int metricsPort = 9090;

        // Parse command line arguments if provided
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid port: " + args[0]);
                System.err.println("💡 Usage: java WebServerApp [port] [threads]");
                return;
            }
        }

        if (args.length > 1) {
            try {
                threadCount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid thread count: " + args[1]);
                System.err.println("💡 Usage: java WebServerApp [port] [threads]");
                return;
            }
        }

        System.out.println("📍 Port: " + port);
        System.out.println("🧵 Threads: " + threadCount);

        try {
            // 📊 Initialize metrics ONCE using singleton
            MetricsCollector metricsCollector = MetricsCollector.getInstance();
            MetricsServer metricsServer = new MetricsServer(metricsPort);

            // Start metrics server first
            metricsServer.start();

            // Create and start the main server
            MultiThreadedServer server = new MultiThreadedServer(port, threadCount);

            // Graceful shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🚨 Shutdown signal received");
                server.stop();
                metricsServer.stop();
            }));

            // Start the server (this blocks until server stops)
            server.start();

        } catch (Exception e) {
            System.err.println("💥 Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
