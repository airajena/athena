// src/main/java/com/webserver/Response.java
package com.webserver;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private int statusCode;
    private String reasonPhrase;
    private Map<String, String> headers;
    private byte[] body; // Changed to byte[] to handle binary files

    public Response(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = new HashMap<>();

        // Default headers
        addHeader("Server", "Enterprise-WebServer/2.0");
        addHeader("Connection", "close");
    }

    // Convenience constructor for text responses
    public Response(int statusCode, String body) {
        this(statusCode, getReasonPhrase(statusCode));
        setTextBody(body);
    }

    public void setTextBody(String textBody) {
        this.body = textBody.getBytes();
        addHeader("Content-Type", "text/html; charset=UTF-8");
        addHeader("Content-Length", String.valueOf(body.length));
    }

    public void setBinaryBody(byte[] binaryBody, String contentType) {
        this.body = binaryBody;
        addHeader("Content-Type", contentType);
        addHeader("Content-Length", String.valueOf(body.length));
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public String toHttpString() {
        StringBuilder response = new StringBuilder();

        // Status line
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(reasonPhrase).append("\r\n");

        // Headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        // Empty line
        response.append("\r\n");

        return response.toString();
    }

    public byte[] getBody() {
        return body != null ? body : new byte[0];
    }

    private static String getReasonPhrase(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }
}
