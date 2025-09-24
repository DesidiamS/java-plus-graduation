package ru.practicum.grpc.user;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.serializer.AvroSerializer;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class KafkaUserActionProducer {

    private final UserActionMapper mapper;

    @Value("${kafka.local.config}")
    private String kafkaServerConfig;

    @Value("${kafka.topic}")
    private String topic;

    public void send(UserActionProto proto) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerConfig);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);

        SpecificRecordBase record = mapper.toUserActionAvro(proto);

        ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(topic, record);

        try (Producer<String, SpecificRecordBase> producer = new KafkaProducer<>(properties)) {
            producer.send(producerRecord);
            producer.flush();
        }
    }
}
