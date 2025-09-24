package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.InteractionService;

import java.time.Duration;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {

    private final KafkaConfig config;
    private final InteractionService interactionService;
    @Value("${kafka.user-consumer.topic}")
    private String topic;

    @Override
    public void run() {
        try (KafkaConsumer<String, UserActionAvro> consumer = new KafkaConsumer<>(config.getUserConsumerProperties())) {
            consumer.subscribe(Collections.singleton(topic));

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    UserActionAvro value = record.value();
                    interactionService.saveInteraction(value);
                }
                consumer.commitSync();
            }
        } catch (Exception ignored) {}
    }
}
