// src/main/java/com/webserver/metrics/MetricsServer.java
package com.webserver.metrics;

import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.CollectorRegistry;
import java.io.IOException;

public class MetricsServer {
    private HTTPServer server;
    private final int port;

    public MetricsServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            server = new HTTPServer.Builder()
                    .withPort(port)
                    .withRegistry(CollectorRegistry.defaultRegistry)
                    .build();

            System.out.println("📊 Prometheus metrics server started on http://localhost:" + port + "/metrics");
        } catch (IOException e) {
            System.err.println("❌ Failed to start metrics server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (server != null) {
            server.close();
            System.out.println("📊 Metrics server stopped");
        }
    }
}
