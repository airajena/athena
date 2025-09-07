package com.webserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler {
    private final String staticRoot = "src/main/resources/static";

    public Response handleStaticFile(String requestPath) {
        try {
            String cleanPath = sanitizePath(requestPath);
            if (cleanPath.isEmpty() || cleanPath.equals("/")) {
                cleanPath = "index.html";
            }

            Path filePath = Paths.get(staticRoot, cleanPath);

            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                byte[] fileContent = Files.readAllBytes(filePath);
                String contentType = getContentType(cleanPath);

                Response response = new Response(200, "");
                response.setBinaryBody(fileContent, contentType);

                System.out.println("📁 Served static file: " + cleanPath + " (" + fileContent.length + " bytes)");
                return response;
            } else {
                return new Response(404, "<h1>404 - File Not Found</h1><p>The file <strong>" + requestPath + "</strong> was not found.</p>");
            }

        } catch (IOException e) {
            System.err.println("❌ Error reading static file: " + e.getMessage());
            return new Response(500, "<h1>500 - Internal Server Error</h1>");
        }
    }

    private String sanitizePath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path.replaceAll("\\.\\./", "");
    }

    private String getContentType(String filename) {
        if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filename.endsWith(".css")) return "text/css";
        if (filename.endsWith(".js")) return "application/javascript";
        if (filename.endsWith(".json")) return "application/json";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain";
    }
}
