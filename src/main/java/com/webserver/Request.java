package com.webserver;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private String method;
    private String path;
    private String version = "HTTP/1.1";
    private Map<String, String> headers = new HashMap<>();
    private String body = "";

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public void addHeader(String name, String value) {
        headers.put(name.toLowerCase(), value.trim());
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public Map<String, String> getHeaders() { return headers; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getContentType() {
        return getHeader("content-type");
    }

    public int getContentLength() {
        String lengthStr = getHeader("content-length");
        return lengthStr != null ? Integer.parseInt(lengthStr) : 0;
    }

    public boolean isJsonRequest() {
        String contentType = getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    @Override
    public String toString() {
        return String.format("Request{method='%s', path='%s', contentType='%s', bodyLength=%d}",
                method, path, getContentType(), body.length());
    }
}
