package ru.practicum.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;

    @Value("${spring.kafka.event-consumer.key-deserializer}")
    private String eventConsumerKeyDeserializer;

    @Value("${spring.kafka.event-consumer.value-deserializer}")
    private String eventConsumerValueDeserializer;

    @Value("${spring.kafka.event-consumer.group-id}")
    private String eventConsumerGroupId;

    @Value("${spring.kafka.event-consumer.client-id}")
    private String eventConsumerClientId;

    @Value("${spring.kafka.event-consumer.enable-auto-commit}")
    private String eventConsumerEnableAutoCommit;

    @Value("${spring.kafka.user-consumer.key-deserializer}")
    private String userConsumerKeyDeserializer;

    @Value("${spring.kafka.user-consumer.value-deserializer}")
    private String userConsumerValueDeserializer;

    @Value("${spring.kafka.user-consumer.group-id}")
    private String userConsumerGroupId;

    @Value("${spring.kafka.user-consumer.client-id}")
    private String userConsumerClientId;

    @Value("${spring.kafka.user-consumer.enable-auto-commit}")
    private String userConsumerEnableAutoCommit;

    public Properties getEventConsumerProperties() {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, eventConsumerKeyDeserializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, eventConsumerValueDeserializer);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, eventConsumerGroupId);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, eventConsumerClientId);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, eventConsumerEnableAutoCommit);

        return properties;
    }

    public Properties getUserConsumerProperties() {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, userConsumerKeyDeserializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, userConsumerValueDeserializer);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, userConsumerGroupId);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, userConsumerClientId);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, userConsumerEnableAutoCommit);

        return properties;
    }
}
