package com.webserver;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private int statusCode;
    private String reasonPhrase;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.reasonPhrase = getReasonPhrase(statusCode);

        addHeader("Server", "Enterprise-WebServer/1.0");
        addHeader("Connection", "close");
        addHeader("Content-Type", "text/html; charset=UTF-8");

        setTextBody(body);
    }

    public void setTextBody(String textBody) {
        this.body = textBody.getBytes();
        addHeader("Content-Length", String.valueOf(this.body.length));
    }

    public void setBinaryBody(byte[] binaryBody, String contentType) {
        this.body = binaryBody;
        addHeader("Content-Type", contentType);
        addHeader("Content-Length", String.valueOf(binaryBody.length));
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public String toHttpString() {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(reasonPhrase).append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        response.append("\r\n");
        return response.toString();
    }

    public byte[] getBody() {
        return body != null ? body : new byte[0];
    }

    public int getStatusCode() { return statusCode; }

    private String getReasonPhrase(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
}
