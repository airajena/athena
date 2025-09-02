// src/main/java/com/webserver/metrics/MetricsCollector.java
package com.webserver.metrics;

import io.prometheus.client.*;
import io.prometheus.client.hotspot.DefaultExports;

public class MetricsCollector {

    // 📈 HTTP Request Metrics
    private final Counter httpRequestsTotal = Counter.build()
            .name("http_requests_total")
            .help("Total HTTP requests")
            .labelNames("method", "endpoint", "status")
            .register();

    private final Histogram httpRequestDuration = Histogram.build()
            .name("http_request_duration_seconds")
            .help("HTTP request latency")
            .labelNames("method", "endpoint")
            .buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0, 5.0)
            .register();

    // 👥 User API Metrics
    private final Gauge activeConnections = Gauge.build()
            .name("active_connections")
            .help("Number of active connections")
            .register();

    private final Counter userOperationsTotal = Counter.build()
            .name("user_operations_total")
            .help("Total user CRUD operations")
            .labelNames("operation", "status")
            .register();

    // 💾 Cache Metrics
    private final Counter cacheOperationsTotal = Counter.build()
            .name("cache_operations_total")
            .help("Total cache operations")
            .labelNames("operation", "result")
            .register();

    // 🧵 Thread Pool Metrics
    private final Gauge threadPoolSize = Gauge.build()
            .name("thread_pool_size")
            .help("Current thread pool size")
            .register();

    private final Gauge threadPoolActive = Gauge.build()
            .name("thread_pool_active")
            .help("Active threads in pool")
            .register();

    public MetricsCollector() {
        // Register JVM metrics automatically
        DefaultExports.initialize();
        System.out.println("📊 Prometheus metrics collector initialized");
    }

    // Record HTTP request metrics
    public void recordHttpRequest(String method, String endpoint, String status, double durationSeconds) {
        httpRequestsTotal.labels(method, endpoint, status).inc();
        httpRequestDuration.labels(method, endpoint).observe(durationSeconds);
    }

    // Record user operations
    public void recordUserOperation(String operation, String status) {
        userOperationsTotal.labels(operation, status).inc();
    }

    // Record cache operations
    public void recordCacheOperation(String operation, String result) {
        cacheOperationsTotal.labels(operation, result).inc();
    }

    // Update connection count
    public void setActiveConnections(int count) {
        activeConnections.set(count);
    }

    // Update thread pool metrics
    public void updateThreadPoolMetrics(int poolSize, int activeThreads) {
        threadPoolSize.set(poolSize);
        threadPoolActive.set(activeThreads);
    }

    public CollectorRegistry getRegistry() {
        return CollectorRegistry.defaultRegistry;
    }
}
