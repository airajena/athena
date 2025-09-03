package com.webserver;

import com.webserver.metrics.MetricsCollector;
import com.webserver.metrics.MetricsServer;

public class WebServerApp {
    public static void main(String[] args) {
        System.out.println("🌟 Starting Enterprise Multi-Threaded Web Server...");

        int port = 8080;
        int threadCount = 50; // Increased for better concurrency
        int metricsPort = 9090;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid port: " + args[0]);
                return;
            }
        }

        if (args.length > 1) {
            try {
                threadCount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid thread count: " + args[1]);
                return;
            }
        }

        System.out.println("📍 Port: " + port);
        System.out.println("🧵 Thread Count: " + threadCount);

        try {
            // Initialize Prometheus metrics
            MetricsCollector metricsCollector = MetricsCollector.getInstance();
            MetricsServer metricsServer = new MetricsServer(metricsPort);
            metricsServer.start();

            // Create and start the multi-threaded server
            MultiThreadedServer server = new MultiThreadedServer(port, threadCount);

            // Graceful shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🚨 Shutdown signal received");
                server.stop();
                metricsServer.stop();
            }));

            // Start the server
            server.start();

        } catch (Exception e) {
            System.err.println("💥 Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
