// src/main/java/com/webserver/metrics/MetricsCollector.java
package com.webserver.metrics;

import io.prometheus.client.*;
import io.prometheus.client.hotspot.DefaultExports;

public class MetricsCollector {
    private static MetricsCollector INSTANCE = null;
    private static boolean initialized = false;

    private final Counter httpRequestsTotal;
    private final Histogram httpRequestDuration;
    private final Gauge activeConnections;
    private final Gauge threadPoolSize;
    private final Gauge threadPoolActive;
    private final Counter userOperationsTotal;

    private MetricsCollector() {
        httpRequestsTotal = Counter.build()
                .name("http_requests_total")
                .help("Total HTTP requests")
                .labelNames("method", "endpoint", "status")
                .register();

        httpRequestDuration = Histogram.build()
                .name("http_request_duration_seconds")
                .help("HTTP request latency")
                .labelNames("method", "endpoint")
                .buckets(0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0, 5.0)
                .register();

        activeConnections = Gauge.build()
                .name("active_connections")
                .help("Number of active connections")
                .register();

        threadPoolSize = Gauge.build()
                .name("thread_pool_size")
                .help("Current thread pool size")
                .register();

        threadPoolActive = Gauge.build()
                .name("thread_pool_active")
                .help("Active threads in pool")
                .register();

        userOperationsTotal = Counter.build()
                .name("user_operations_total")
                .help("Total user operations")
                .labelNames("operation", "status")
                .register();

        // Register default JVM metrics only once
        if (!initialized) {
            DefaultExports.initialize();
            initialized = true;
        }

        System.out.println("📊 Prometheus metrics collector initialized");
    }

    public static synchronized MetricsCollector getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MetricsCollector();
        }
        return INSTANCE;
    }

    public void recordHttpRequest(String method, String endpoint, String status, double durationSeconds) {
        httpRequestsTotal.labels(method, endpoint, status).inc();
        httpRequestDuration.labels(method, endpoint).observe(durationSeconds);
    }

    public void setActiveConnections(int count) {
        activeConnections.set(count);
    }

    public void updateThreadPoolMetrics(int poolSize, int activeThreads) {
        threadPoolSize.set(poolSize);
        threadPoolActive.set(activeThreads);
    }

    public void recordUserOperation(String operation, String status) {
        userOperationsTotal.labels(operation, status).inc();
    }
}
