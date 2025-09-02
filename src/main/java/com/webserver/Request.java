// src/main/java/com/webserver/Request.java
package com.webserver;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private String method;
    private String path;
    private String version;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private String body;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
        this.version = "HTTP/1.1";
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.body = "";
    }

    // Getters and Setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Map<String, String> getHeaders() { return headers; }
    public void addHeader(String name, String value) {
        headers.put(name.toLowerCase(), value.trim()); // Normalize and trim
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public Map<String, String> getQueryParams() { return queryParams; }
    public void addQueryParam(String name, String value) {
        queryParams.put(name, value);
    }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    // 🔧 IMPROVED: More flexible Content-Type checking
    public String getContentType() {
        return getHeader("content-type");
    }

    public int getContentLength() {
        String lengthStr = getHeader("content-length");
        return lengthStr != null ? Integer.parseInt(lengthStr) : 0;
    }

    // 🔧 IMPROVED: Relaxed JSON content type checking
    public boolean isJsonRequest() {
        String contentType = getContentType();
        if (contentType == null) {
            return false;
        }

        // Normalize and check for JSON content type
        String lowerContentType = contentType.toLowerCase().trim();

        // Accept various JSON content types
        return lowerContentType.startsWith("application/json") ||
                lowerContentType.contains("application/json");
    }

    @Override
    public String toString() {
        return String.format("Request{method='%s', path='%s', contentType='%s', bodyLength=%d}",
                method, path, getContentType(), body.length());
    }
}
