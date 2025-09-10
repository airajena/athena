package com.webserver;

import com.webserver.handlers.UserApiHandler;
import com.webserver.metrics.MetricsCollector;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class RequestProcessor {
    private final StaticFileHandler staticFileHandler;
    private final UserApiHandler userApiHandler;
    private final AtomicLong requestCount = new AtomicLong(0);

    public RequestProcessor() {
        this.staticFileHandler = new StaticFileHandler();
        this.userApiHandler = new UserApiHandler();
    }

    public Response processRequest(Request request) {
        long reqId = requestCount.incrementAndGet();
        String path = request.getPath();
        String method = request.getMethod();

        System.out.println("🍳 Processing request #" + reqId + ": " + method + " " + path);

        try {
            if (path.startsWith("/api/users")) {
                return userApiHandler.handleUserApi(request);
            } else if (path.equals("/health")) {
                return createHealthResponse();
            } else if (path.equals("/stats")) {
                return createStatsResponse();
            } else if (path.equals("/time")) {
                return createTimeResponse();
            } else {
                return staticFileHandler.handleStaticFile(path);
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing request: " + e.getMessage());
            return new Response(500, "<h1>500 - Internal Server Error</h1>");
        }
    }

    private Response createHealthResponse() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head><title>Health Check</title></head>
            <body>
                <h1>✅ Server Health: OK</h1>
                <p>Server Time: %s</p>
                <p>Total Requests: %d</p>
            </body>
            </html>
            """.formatted(
                LocalDateTime.now(),
                requestCount.get()
        );

        return new Response(200, html);
    }

    private Response createStatsResponse() {
        MetricsCollector metrics = MetricsCollector.getInstance();

        String html = """
            <!DOCTYPE html>
            <html>
            <head><title>Server Statistics</title></head>
            <body>
                <h1>📊 Server Statistics</h1>
                <p>Total Requests Processed: %d</p>
                <p>Active Thread: %s</p>
                <p>Server Uptime: Running</p>
            </body>
            </html>
            """.formatted(
                requestCount.get(),
                Thread.currentThread().getName()
        );

        return new Response(200, html);
    }

    private Response createTimeResponse() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head><title>Server Time</title></head>
            <body>
                <h1>⏰ Current Server Time</h1>
                <p>%s</p>
                <p>Handled by thread: %s</p>
            </body>
            </html>
            """.formatted(
                LocalDateTime.now(),
                Thread.currentThread().getName()
        );

        return new Response(200, html);
    }

    public long getRequestCount() {
        return requestCount.get();
    }
}
