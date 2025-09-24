package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.SimilarityService;

import java.time.Duration;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class EventSimilarityProcessor implements Runnable {

    private final KafkaConfig config;
    private final SimilarityService similarityService;

    @Value("${kafka.event-consumer.topic}")
    private String topic;

    @Override
    public void run() {
        try (KafkaConsumer<String, EventSimilarityAvro> consumer = new KafkaConsumer<>(config.getEventConsumerProperties())) {
            consumer.subscribe(Collections.singleton(topic));

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    EventSimilarityAvro value = record.value();
                    similarityService.saveSimilarity(value);
                }
                consumer.commitSync();
            }
        } catch (Exception ignored) {
        }
    }
}
