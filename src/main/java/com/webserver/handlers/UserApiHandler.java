// src/main/java/com/webserver/handlers/UserApiHandler.java
package com.webserver.handlers;

import com.webserver.Request;
import com.webserver.Response;
import com.webserver.model.User;
import com.webserver.storage.UserStorage;
import com.webserver.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserApiHandler {
    private final UserStorage userStorage = new UserStorage(); // Now using in-memory storage only

    public Response handleUserApi(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        System.out.println("🎯 User API: " + method + " " + path);

        try {
            return switch (method.toUpperCase()) {
                case "GET" -> handleGetUsers(request);
                case "POST" -> handleCreateUser(request);
                case "PUT" -> handleUpdateUser(request);
                case "DELETE" -> handleDeleteUser(request);
                default -> createJsonResponse(405, JsonUtils.createErrorJson(
                        "Method not allowed", "Supported methods: GET, POST, PUT, DELETE"));
            };
        } catch (Exception e) {
            System.err.println("❌ Error in User API: " + e.getMessage());
            return createJsonResponse(500, JsonUtils.createErrorJson(
                    "Internal server error", e.getMessage()));
        }
    }

    private Response handleGetUsers(Request request) {
        String path = request.getPath();

        if (path.equals("/api/users")) {
            // GET /api/users - List all users
            List<User> users = userStorage.getAllUsers();
            String json = JsonUtils.createSuccessJson("Users retrieved successfully", users);
            return createJsonResponse(200, json);

        } else if (path.matches("/api/users/\\d+")) {
            // GET /api/users/{id} - Get specific user
            Long userId = extractIdFromPath(path);
            Optional<User> user = userStorage.getUserById(userId);

            if (user.isPresent()) {
                String json = JsonUtils.createSuccessJson("User found", user.get());
                return createJsonResponse(200, json);
            } else {
                String json = JsonUtils.createErrorJson("User not found", "No user with ID: " + userId);
                return createJsonResponse(404, json);
            }
        }

        String json = JsonUtils.createErrorJson("Invalid endpoint", "Use /api/users or /api/users/{id}");
        return createJsonResponse(400, json);
    }

    // In your UserApiHandler.java, update the handleCreateUser method:
    private Response handleCreateUser(Request request) {
        System.out.println("🔍 POST Request Debug:");
        System.out.println("  Content-Type: '" + request.getContentType() + "'");
        System.out.println("  Is JSON Request: " + request.isJsonRequest());
        System.out.println("  Body Length: " + request.getBody().length());
        System.out.println("  Body Content: '" + request.getBody() + "'");

        if (!request.isJsonRequest()) {
            String error = String.format(
                    "Invalid content type. Expected 'application/json', got '%s'",
                    request.getContentType()
            );
            System.out.println("❌ " + error);
            String json = JsonUtils.createErrorJson("Invalid content type", error);
            return createJsonResponse(400, json);
        }

        if (request.getBody().trim().isEmpty()) {
            String json = JsonUtils.createErrorJson("Empty body", "Request body is required for POST");
            return createJsonResponse(400, json);
        }

        try {
            User user = JsonUtils.fromJson(request.getBody(), User.class);

            // Validate user
            List<String> validationErrors = validateUser(user);
            if (!validationErrors.isEmpty()) {
                String json = JsonUtils.createValidationErrorJson("Validation failed", validationErrors);
                return createJsonResponse(400, json);
            }

            User createdUser = userStorage.createUser(user);
            String json = JsonUtils.createSuccessJson("User created successfully", createdUser);
            return createJsonResponse(201, json);

        } catch (RuntimeException e) {
            System.out.println("❌ JSON parsing error: " + e.getMessage());
            String json = JsonUtils.createErrorJson("Invalid JSON", e.getMessage());
            return createJsonResponse(400, json);
        }
    }


    private Response handleUpdateUser(Request request) {
        String path = request.getPath();

        if (!path.matches("/api/users/\\d+")) {
            String json = JsonUtils.createErrorJson("Invalid endpoint", "Use /api/users/{id}");
            return createJsonResponse(400, json);
        }

        if (!request.isJsonRequest()) {
            String json = JsonUtils.createErrorJson("Invalid content type", "Expected application/json");
            return createJsonResponse(400, json);
        }

        try {
            Long userId = extractIdFromPath(path);
            User user = JsonUtils.fromJson(request.getBody(), User.class);

            // Validate user
            List<String> validationErrors = validateUser(user);
            if (!validationErrors.isEmpty()) {
                String json = JsonUtils.createValidationErrorJson("Validation failed", validationErrors);
                return createJsonResponse(400, json);
            }

            User updatedUser = userStorage.updateUser(userId, user);
            if (updatedUser != null) {
                String json = JsonUtils.createSuccessJson("User updated successfully", updatedUser);
                return createJsonResponse(200, json);
            } else {
                String json = JsonUtils.createErrorJson("User not found", "No user with ID: " + userId);
                return createJsonResponse(404, json);
            }

        } catch (RuntimeException e) {
            String json = JsonUtils.createErrorJson("Invalid JSON", e.getMessage());
            return createJsonResponse(400, json);
        }
    }

    private Response handleDeleteUser(Request request) {
        String path = request.getPath();

        if (!path.matches("/api/users/\\d+")) {
            String json = JsonUtils.createErrorJson("Invalid endpoint", "Use /api/users/{id}");
            return createJsonResponse(400, json);
        }

        Long userId = extractIdFromPath(path);
        boolean deleted = userStorage.deleteUser(userId);

        if (deleted) {
            String json = JsonUtils.createSuccessJson("User deleted successfully", null);
            return createJsonResponse(200, json);
        } else {
            String json = JsonUtils.createErrorJson("User not found", "No user with ID: " + userId);
            return createJsonResponse(404, json);
        }
    }

    private List<String> validateUser(User user) {
        List<String> errors = new ArrayList<>();

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            errors.add("Name is required");
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            errors.add("Valid email is required");
        }
        if (user.getAge() <= 0 || user.getAge() > 150) {
            errors.add("Age must be between 1 and 150");
        }

        return errors;
    }

    private Long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }

    private Response createJsonResponse(int statusCode, String json) {
        Response response = new Response(statusCode, json);
        response.addHeader("Content-Type", "application/json; charset=UTF-8");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        return response;
    }

    public int getUserCount() {
        return userStorage.getUserCount();
    }
}
