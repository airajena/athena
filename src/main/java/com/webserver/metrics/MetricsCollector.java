package com.webserver.metrics;

import io.prometheus.client.*;
import io.prometheus.client.hotspot.DefaultExports;

public class MetricsCollector {
    private static MetricsCollector INSTANCE = null;
    private static boolean initialized = false;

    private final Counter httpRequestsTotal;
    private final Histogram httpRequestDuration;
    private final Gauge activeConnections;

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
                .register();

        activeConnections = Gauge.build()
                .name("active_connections")
                .help("Number of active connections")
                .register();

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
}
