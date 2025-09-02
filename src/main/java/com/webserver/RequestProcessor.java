// src/main/java/com/webserver/RequestProcessor.java
package com.webserver;

import com.webserver.handlers.UserApiHandler;
import com.webserver.handlers.UserApiHandler;
import com.webserver.security.AuthenticationFilter;
import com.webserver.utils.JsonUtils;
import java.util.Map;

public class RequestProcessor {
    private long requestCount = 0;
    private final StaticFileHandler staticFileHandler = new StaticFileHandler();
    private final UserApiHandler userApiHandler = new UserApiHandler();
    private final AuthenticationFilter authFilter = new AuthenticationFilter(); // 🆕 Add this

    public synchronized Response processRequest(Request request) {
        requestCount++;
        String path = request.getPath();
        String method = request.getMethod();

        System.out.println("🍳 Processing request #" + requestCount + ": " + method + " " + path);

        // 🆕 Authentication check FIRST
        Response authResponse = authFilter.authenticate(request);
        if (authResponse != null) {
            return authResponse; // Return 401 if authentication fails
        }

        // Add processing delay to see multi-threading
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Route to appropriate handler
        if (path.startsWith("/api/")) {
            System.out.println("🎯 Routing to API handler: " + path);
            return userApiHandler.handleUserApi(request);
        }

        // Handle other endpoints
        switch (path) {
            case "/hello":
                return createWelcomeResponse();
            case "/time":
                return createTimeResponse();
            case "/stats":
                return createStatsResponse();
            case "/slow":
                return createSlowResponse();
            case "/health":
                return createHealthResponse(); // 🆕 Add health check
            default:
                System.out.println("📁 Routing to static file handler: " + path);
                return staticFileHandler.handleStaticFile(path);
        }
    }

    // 🆕 Add health check endpoint
    private Response createHealthResponse() {
        Map<String, Object> healthData = Map.of(
                "status", "UP",
                "uptime", System.currentTimeMillis(),
                "totalRequests", requestCount,
                "totalUsers", userApiHandler.getUserCount()
        );

        String json = JsonUtils.createSuccessJson("Server is healthy", healthData);
        Response response = new Response(200, json);
        response.addHeader("Content-Type", "application/json");
        return response;
    }

    private Response createWelcomeResponse() {
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>My Awesome Web Server</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 40px; }\n" +
                "        h1 { color: #333; }\n" +
                "        .info { background: #f0f0f0; padding: 20px; border-radius: 8px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>🎉 Welcome to My Multi-Threaded Web Server!</h1>\n" +
                "    <div class='info'>\n" +
                "        <p>Congratulations! Your server is working perfectly!</p>\n" +
                "        <p>Try these endpoints:</p>\n" +
                "        <ul>\n" +
                "            <li><a href='/hello'>/hello</a> - This page</li>\n" +
                "            <li><a href='/time'>/time</a> - Current server time</li>\n" +
                "            <li><a href='/stats'>/stats</a> - Server statistics</li>\n" +
                "            <li><a href='/slow'>/slow</a> - Slow endpoint</li>\n" +
                "            <li><a href='/api/users'>/api/users</a> - User API (JSON)</li>\n" +
                "        </ul>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        Response response = new Response(200, html);
        response.addHeader("Content-Type", "text/html");
        return response;
    }

    private Response createTimeResponse() {
        String currentTime = new java.util.Date().toString();
        String threadName = Thread.currentThread().getName();

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body style='font-family: Arial;'>\n" +
                "    <h1>⏰ Current Time</h1>\n" +
                "    <p>Time: <strong>" + currentTime + "</strong></p>\n" +
                "    <p>Served by thread: <strong>" + threadName + "</strong></p>\n" +
                "    <a href='/hello'>← Back to Home</a>\n" +
                "</body>\n" +
                "</html>";

        Response response = new Response(200, html);
        response.addHeader("Content-Type", "text/html");
        return response;
    }

    private Response createStatsResponse() {
        String threadName = Thread.currentThread().getName();
        int activeThreads = Thread.activeCount();

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body style='font-family: Arial;'>\n" +
                "    <h1>📊 Server Statistics</h1>\n" +
                "    <p>Total requests processed: <strong>" + requestCount + "</strong></p>\n" +
                "    <p>Current thread: <strong>" + threadName + "</strong></p>\n" +
                "    <p>Active threads: <strong>" + activeThreads + "</strong></p>\n" +
                "    <p>Total users in API: <strong>" + userApiHandler.getUserCount() + "</strong></p>\n" +
                "    <a href='/hello'>← Back to Home</a>\n" +
                "</body>\n" +
                "</html>";

        Response response = new Response(200, html);
        response.addHeader("Content-Type", "text/html");
        return response;
    }

    private Response createSlowResponse() {
        // Simulate a slow operation
        try {
            Thread.sleep(3000); // 3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String threadName = Thread.currentThread().getName();
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body style='font-family: Arial;'>\n" +
                "    <h1>🐌 Slow Response Complete!</h1>\n" +
                "    <p>This response took 3 seconds to generate.</p>\n" +
                "    <p>But other requests were served simultaneously!</p>\n" +
                "    <p>Served by thread: <strong>" + threadName + "</strong></p>\n" +
                "    <a href='/hello'>← Back to Home</a>\n" +
                "</body>\n" +
                "</html>";

        Response response = new Response(200, html);
        response.addHeader("Content-Type", "text/html");
        return response;
    }

    public long getRequestCount() {
        return requestCount;
    }
}
