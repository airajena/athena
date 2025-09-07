package com.webserver.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            System.err.println("❌ JSON serialization error: " + e.getMessage());
            return createErrorJson("Serialization error", e.getMessage());
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            System.err.println("❌ JSON parsing error: " + e.getMessage());
            throw new RuntimeException("Invalid JSON format", e);
        }
    }

    public static String createSuccessJson(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return toJson(response);
    }

    public static String createErrorJson(String error, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("details", details);
        response.put("timestamp", System.currentTimeMillis());
        return toJson(response);
    }

    public static String createValidationErrorJson(String error, List<String> validationErrors) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("validationErrors", validationErrors);
        response.put("timestamp", System.currentTimeMillis());
        return toJson(response);
    }
}
