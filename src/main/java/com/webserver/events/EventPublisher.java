package com.webserver.events;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import com.webserver.utils.JsonUtils;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

public class EventPublisher {
    private KafkaProducer<String, String> producer;
    private final String topicName = "webserver-events";
    private boolean connected = false;

    // 🔧 ADD THIS: Default constructor that uses localhost:9093
    public EventPublisher() {
        this("localhost:9093"); // Calls the main constructor with default port
    }

    // Existing constructor (keep as is)
    public EventPublisher(String bootstrapServers) {
        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "1");
            props.put(ProducerConfig.RETRIES_CONFIG, 3);
            props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);

            producer = new KafkaProducer<>(props);
            connected = true;
            System.out.println("🚀 Kafka producer connected: " + bootstrapServers);

        } catch (Exception e) {
            System.out.println("⚠️  Kafka not available: " + e.getMessage());
            System.out.println("💡 Continuing without Kafka event publishing...");
        }
    }

    // ... rest of your existing methods (publishEvent, isConnected, close) ...

    public void publishEvent(String eventType, Object data) {
        if (!connected) return;

        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("data", data);
            event.put("timestamp", System.currentTimeMillis());
            event.put("source", "webserver");

            String eventJson = JsonUtils.toJson(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topicName, eventType, eventJson);

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("❌ Kafka publish failed: " + exception.getMessage());
                } else {
                    System.out.println("🚀 Event published: " + eventType);
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error publishing event: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            System.out.println("🚀 Kafka producer closed");
        }
    }
}
