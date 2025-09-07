package com.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;
import com.webserver.metrics.MetricsCollector;

public class MultiThreadedServer {
    private final int port;
    private final ThreadPoolManager threadPool;
    private final RequestProcessor requestProcessor;
    private final AtomicLong connectionCounter;
    private volatile boolean isRunning = false;
    public static MetricsCollector getMetricsCollector() {
        return MetricsCollector.getInstance();
    }

    public MultiThreadedServer(int port, int threadCount) {
        this.port = port;
        this.threadPool = new ThreadPoolManager(threadCount);
        this.requestProcessor = new RequestProcessor();
        this.connectionCounter = new AtomicLong(0);

        System.out.println("🏗️  Multi-threaded server created");
        System.out.println("📍 Port: " + port);
        System.out.println("🧵 Thread pool size: " + threadCount);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            isRunning = true;

            System.out.println("🚀 Multi-threaded server started on http://localhost:" + port);
            System.out.println("📝 Try these URLs:");
            System.out.println("   • http://localhost:" + port + "/hello");
            System.out.println("   • http://localhost:" + port + "/time");
            System.out.println("   • http://localhost:" + port + "/stats");
            System.out.println("   • http://localhost:" + port + "/slow");
            System.out.println("⏹️  Press Ctrl+C to stop");
            System.out.println();

            while (isRunning) {
                try {

                    Socket clientSocket = serverSocket.accept();

                    long connectionId = connectionCounter.incrementAndGet();

                    System.out.println("🔗 New connection #" + connectionId + " from " +
                            clientSocket.getRemoteSocketAddress());

                    ConnectionHandler handler = new ConnectionHandler(
                            clientSocket,
                            requestProcessor,
                            connectionId
                    );

                    threadPool.execute(handler);

                    System.out.println("📤 Connection #" + connectionId + " submitted to thread pool");

                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("❌ Error accepting connection: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("💥 Failed to start server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        if (isRunning) {
            System.out.println("\n🛑 Stopping server...");
            isRunning = false;

            threadPool.shutdown();

            System.out.println("📊 Final stats:");
            System.out.println("   • Total connections handled: " + connectionCounter.get());
            System.out.println("   • Total requests processed: " + requestProcessor.getRequestCount());
            System.out.println("✅ Server stopped successfully");
        }
    }
}
