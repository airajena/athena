// src/main/java/com/webserver/events/EventPublisher.java
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
    private final String topicName = "user-events";
    private boolean isConnected = false;

    public EventPublisher(String bootstrapServers) {
        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.RETRIES_CONFIG, 3);
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

            producer = new KafkaProducer<>(props);
            isConnected = true;
            System.out.println("🚀 Kafka producer connected to: " + bootstrapServers);
        } catch (Exception e) {
            System.err.println("❌ Kafka connection failed: " + e.getMessage());
            System.out.println("💡 Continuing without Kafka event publishing...");
        }
    }

    public void publishUserEvent(String eventType, Object userData, String userId) {
        if (!isConnected) return;

        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("userId", userId);
            event.put("userData", userData);
            event.put("timestamp", System.currentTimeMillis());
            event.put("service", "webserver");

            String eventJson = JsonUtils.toJson(event);

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topicName,
                    userId, // Use userId as key for partitioning
                    eventJson
            );

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("❌ Failed to publish event: " + exception.getMessage());
                } else {
                    System.out.println("🚀 Event published: " + eventType + " for user " + userId +
                            " (partition: " + metadata.partition() + ", offset: " + metadata.offset() + ")");
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error publishing event: " + e.getMessage());
        }
    }

    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            System.out.println("🚀 Kafka producer closed");
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
