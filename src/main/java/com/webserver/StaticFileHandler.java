// src/main/java/com/webserver/StaticFileHandler.java
package com.webserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler {
    private final String staticRoot;

    public StaticFileHandler() {
        this.staticRoot = "src/main/resources/static";
    }

    public Response handleStaticFile(String requestPath) {
        try {
            // Clean and secure the path
            String cleanPath = sanitizePath(requestPath);

            // Default to index.html for root requests
            if (cleanPath.isEmpty() || cleanPath.equals("/")) {
                cleanPath = "index.html";
            }

            Path filePath = Paths.get(staticRoot, cleanPath);

            // Security check: ensure file is within static directory
            if (!filePath.normalize().startsWith(Paths.get(staticRoot).normalize())) {
                System.out.println("🔒 Security: Blocked directory traversal attempt: " + requestPath);
                return new Response(403, "<h1>403 - Forbidden</h1><p>Access denied</p>");
            }

            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                byte[] fileContent = Files.readAllBytes(filePath);
                String contentType = getContentType(cleanPath);

                // Create response with proper content type
                Response response = new Response(200, "");
                response.setBinaryBody(fileContent, contentType);

                // Add caching headers for better performance
                response.addHeader("Cache-Control", "public, max-age=3600");
                response.addHeader("ETag", String.valueOf(fileContent.length));

                System.out.println("📁 Served static file: " + cleanPath +
                        " (" + fileContent.length + " bytes, " + contentType + ")");
                return response;

            } else {
                System.out.println("❌ Static file not found: " + cleanPath);
                return createNotFoundPage(requestPath);
            }

        } catch (IOException e) {
            System.err.println("❌ Error reading static file: " + e.getMessage());
            return new Response(500, "<h1>500 - Internal Server Error</h1>");
        }
    }

    private String sanitizePath(String path) {
        // Remove leading slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // Security: prevent directory traversal
        path = path.replace("..", "");
        path = path.replace("\\", "/");

        return path;
    }

    private String getContentType(String fileName) {
        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        }
        if (lowerName.endsWith(".css")) {
            return "text/css";
        }
        if (lowerName.endsWith(".js")) {
            return "application/javascript";
        }
        if (lowerName.endsWith(".json")) {
            return "application/json";
        }
        if (lowerName.endsWith(".png")) {
            return "image/png";
        }
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerName.endsWith(".gif")) {
            return "image/gif";
        }
        if (lowerName.endsWith(".ico")) {
            return "image/x-icon";
        }
        if (lowerName.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lowerName.endsWith(".txt")) {
            return "text/plain";
        }

        return "application/octet-stream";
    }

    private Response createNotFoundPage(String requestPath) {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>404 - Not Found</title>
                <link rel="stylesheet" href="/css/style.css">
            </head>
            <body>
                <div class="container">
                    <div class="card">
                        <h1>🔍 404 - Page Not Found</h1>
                        <p>The requested file <strong>%s</strong> could not be found.</p>
                        <p><a href="/" class="btn primary">← Back to Home</a></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(requestPath);

        return new Response(404, html);
    }
}
