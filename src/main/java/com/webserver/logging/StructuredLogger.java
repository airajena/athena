// src/main/java/com/webserver/logging/StructuredLogger.java
package com.webserver.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class StructuredLogger {
    private final Logger logger;

    public StructuredLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public void logHttpRequest(String method, String path, int status, long duration, String userAgent, String ip) {
        MDC.put("http.method", method);
        MDC.put("http.path", path);
        MDC.put("http.status", String.valueOf(status));
        MDC.put("http.duration", String.valueOf(duration));
        MDC.put("http.user_agent", userAgent);
        MDC.put("http.remote_ip", ip);

        logger.info("HTTP Request processed: {} {} -> {} ({}ms)", method, path, status, duration);
        MDC.clear();
    }

    public void logUserOperation(String operation, String userId, String status) {
        MDC.put("user.operation", operation);
        MDC.put("user.id", userId);
        MDC.put("operation.status", status);

        logger.info("User operation: {} for user {} - {}", operation, userId, status);
        MDC.clear();
    }

    public void logCacheOperation(String operation, String key, String result) {
        MDC.put("cache.operation", operation);
        MDC.put("cache.key", key);
        MDC.put("cache.result", result);

        logger.debug("Cache operation: {} on key {} -> {}", operation, key, result);
        MDC.clear();
    }

    public void logError(String operation, String error, Exception e) {
        MDC.put("error.operation", operation);
        MDC.put("error.message", error);

        logger.error("Error in {}: {}", operation, error, e);
        MDC.clear();
    }
}
