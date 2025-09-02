// src/main/java/com/webserver/security/AuthenticationFilter.java
package com.webserver.security;

import com.webserver.Request;
import com.webserver.Response;
import com.webserver.utils.JsonUtils;

import java.util.HashSet;
import java.util.Set;

public class AuthenticationFilter {
    private final Set<String> validApiKeys;
    private final Set<String> publicEndpoints;

    public AuthenticationFilter() {
        // Initialize with some API keys (in production, load from database/config)
        this.validApiKeys = new HashSet<>();
        validApiKeys.add("sk-test-12345");
        validApiKeys.add("sk-prod-67890");
        validApiKeys.add("demo-key-999");

        // Public endpoints that don't require authentication
        this.publicEndpoints = new HashSet<>();
        publicEndpoints.add("/");
        publicEndpoints.add("/hello");
        publicEndpoints.add("/time");
        publicEndpoints.add("/stats");
        publicEndpoints.add("/health");
        publicEndpoints.add("/metrics");
    }

    public boolean requiresAuthentication(String path) {
        // API endpoints require auth, static files don't
        return path.startsWith("/api/");
    }

    public Response authenticate(Request request) {
        String path = request.getPath();

        // Check if authentication is required
        if (!requiresAuthentication(path)) {
            return null; // No auth required, continue processing
        }

        // Get API key from header
        String apiKey = request.getHeader("x-api-key");
        if (apiKey == null) {
            apiKey = request.getHeader("authorization");
            if (apiKey != null && apiKey.startsWith("Bearer ")) {
                apiKey = apiKey.substring(7); // Remove "Bearer " prefix
            }
        }

        // Validate API key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("🔒 Authentication failed: Missing API key for " + path);
            String json = JsonUtils.createErrorJson(
                    "Authentication required",
                    "Provide API key in 'X-API-Key' header or 'Authorization: Bearer <key>' header"
            );
            return createAuthResponse(401, json);
        }

        if (!validApiKeys.contains(apiKey.trim())) {
            System.out.println("🔒 Authentication failed: Invalid API key for " + path);
            String json = JsonUtils.createErrorJson(
                    "Invalid API key",
                    "The provided API key is not valid"
            );
            return createAuthResponse(401, json);
        }

        System.out.println("🔓 Authentication successful for " + path + " (key: " +
                apiKey.substring(0, Math.min(8, apiKey.length())) + "...)");
        return null; // Authentication successful, continue processing
    }

    private Response createAuthResponse(int statusCode, String json) {
        Response response = new Response(statusCode, json);
        response.addHeader("Content-Type", "application/json; charset=UTF-8");
        response.addHeader("WWW-Authenticate", "Bearer realm=\"API\"");
        return response;
    }

    public void addApiKey(String apiKey) {
        validApiKeys.add(apiKey);
    }

    public boolean isValidApiKey(String apiKey) {
        return validApiKeys.contains(apiKey);
    }
}
