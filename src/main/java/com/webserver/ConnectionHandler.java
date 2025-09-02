// src/main/java/com/webserver/ConnectionHandler.java
package com.webserver;

import java.io.*;
import java.net.Socket;

/**
 * Handles a single client connection in its own thread
 * Each instance is like one waiter serving one table
 */
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

        try (
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );
                java.io.OutputStream output = clientSocket.getOutputStream()
        ) {
            long startTime = System.currentTimeMillis();

            Request request = parseRequest(input);

            if (request != null) {
                System.out.println("📨 [" + threadName + "] Processing: " + request);

                Response response = requestProcessor.processRequest(request);

                // Send HTTP headers
                output.write(response.toHttpString().getBytes());

                // Send binary body
                byte[] body = response.getBody();
                if (body.length > 0) {
                    output.write(body);
                }

                output.flush();

                long processingTime = System.currentTimeMillis() - startTime;
                System.out.println("✅ [" + threadName + "] Completed connection #" + connectionId +
                        " in " + processingTime + "ms");
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


    /**
     * Parse HTTP request from client
     * Same logic as before, but now in its own thread!
     */
    // Update the parseRequest method in your ConnectionHandler.java
    // Update the parseRequest method in ConnectionHandler.java
    private Request parseRequest(BufferedReader input) throws IOException {
        String requestLine = input.readLine();
        if (requestLine == null || requestLine.trim().isEmpty()) {
            return null;
        }

        // Parse: "PUT /api/users/123 HTTP/1.1"
        String method = "GET";
        String path = "/";

        if (requestLine.contains(" ")) {
            int firstSpace = requestLine.indexOf(' ');
            int secondSpace = requestLine.indexOf(' ', firstSpace + 1);

            if (firstSpace > 0) {
                method = requestLine.substring(0, firstSpace);
            }
            if (secondSpace > firstSpace) {
                path = requestLine.substring(firstSpace + 1, secondSpace);
            }
        }

        Request request = new Request(method, path);

        // Parse headers
        String line;
        while ((line = input.readLine()) != null && !line.trim().isEmpty()) {
            if (line.contains(":")) {
                int colonIndex = line.indexOf(':');
                String headerName = line.substring(0, colonIndex).trim();
                String headerValue = line.substring(colonIndex + 1).trim();
                request.addHeader(headerName, headerValue);

                if (headerName.toLowerCase().equals("content-type")) {
                    System.out.println("🔍 Content-Type: '" + headerValue + "'");
                }
            }
        }

        // 🆕 Read request body for POST and PUT requests
        if (("POST".equals(method) || "PUT".equals(method)) && request.getContentLength() > 0) {
            StringBuilder bodyBuilder = new StringBuilder();
            int contentLength = request.getContentLength();

            for (int i = 0; i < contentLength; i++) {
                int ch = input.read();
                if (ch == -1) break;
                bodyBuilder.append((char) ch);
            }

            request.setBody(bodyBuilder.toString());
            System.out.println("📝 Request body (" + method + "): " + request.getBody().length() + " chars");
        }

        return request;
    }



    // Add this method to ConnectionHandler.java after the existing run() method

    private void sendResponse(Response response, PrintWriter textOutput,
                              java.io.OutputStream binaryOutput) throws IOException {
        // Send HTTP headers as text
        textOutput.print(response.toHttpString());
        textOutput.flush();

        // Send body as binary data
        byte[] body = response.getBody();
        if (body.length > 0) {
            binaryOutput.write(body);
            binaryOutput.flush();
        }
    }

}
