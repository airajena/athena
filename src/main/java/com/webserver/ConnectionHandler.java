package com.webserver;

import java.io.*;
import java.net.Socket;
import com.webserver.metrics.MetricsCollector;

public class ConnectionHandler implements Runnable {
    private final Socket clientSocket;
    private final RequestProcessor requestProcessor;
    private final long connectionId;

    public ConnectionHandler(Socket clientSocket, RequestProcessor requestProcessor, long connectionId) {
        this.clientSocket = clientSocket;
        this.requestProcessor = requestProcessor;
        this.connectionId = connectionId;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("🔄 [" + threadName + "] Handling connection #" + connectionId);

        try (BufferedReader input = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             OutputStream output = clientSocket.getOutputStream()) {

            long startTime = System.currentTimeMillis();

            Request request = parseRequest(input);
            if (request != null) {
                System.out.println("📨 [" + threadName + "] Processing: " + request);

                Response response = requestProcessor.processRequest(request);

                // Send HTTP response
                output.write(response.toHttpString().getBytes());
                if (response.getBody().length > 0) {
                    output.write(response.getBody());
                }
                output.flush();

                long processingTime = System.currentTimeMillis() - startTime;
                System.out.println("✅ [" + threadName + "] Completed connection #" +
                        connectionId + " in " + processingTime + "ms");

                // Record metrics
                MetricsCollector.getInstance().recordHttpRequest(
                        request.getMethod(), request.getPath(),
                        String.valueOf(response.getStatusCode()),
                        processingTime / 1000.0
                );
            }

        } catch (IOException e) {
            System.err.println("❌ [" + threadName + "] Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("🔌 [" + threadName + "] Closed connection #" + connectionId);
            } catch (IOException e) {
                System.err.println("❌ Error closing socket: " + e.getMessage());
            }
        }
    }

    private Request parseRequest(BufferedReader input) throws IOException {
        String requestLine = input.readLine();
        if (requestLine == null || requestLine.trim().isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ");
        String method = parts.length > 0 ? parts[0] : "GET";
        String path = parts.length > 1 ? parts[1] : "/";

        Request request = new Request(method, path);

        // Parse headers
        String line;
        while ((line = input.readLine()) != null && !line.trim().isEmpty()) {
            if (line.contains(":")) {
                int colonIndex = line.indexOf(':');
                String headerName = line.substring(0, colonIndex).trim();
                String headerValue = line.substring(colonIndex + 1).trim();
                request.addHeader(headerName, headerValue);
            }
        }

        // Read body for POST/PUT requests
        if (("POST".equals(method) || "PUT".equals(method)) && request.getContentLength() > 0) {
            StringBuilder bodyBuilder = new StringBuilder();
            int contentLength = request.getContentLength();
            for (int i = 0; i < contentLength; i++) {
                int ch = input.read();
                if (ch == -1) break;
                bodyBuilder.append((char) ch);
            }
            request.setBody(bodyBuilder.toString());
        }

        return request;
    }
}
