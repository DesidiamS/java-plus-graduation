package ru.practicum.deserializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;

public abstract class AvroDeserializer<T> implements Deserializer<T> {

    private final DecoderFactory decoderFactory;
    private final DatumReader<T> datumReader;

    public AvroDeserializer(Schema schema) {
        decoderFactory = DecoderFactory.get();
        datumReader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                BinaryDecoder binaryDecoder = decoderFactory.binaryDecoder(data, null);
                return this.datumReader.read(null, binaryDecoder);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
