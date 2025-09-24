package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AggregatorRunner implements ApplicationRunner {

    private final KafkaSimilarity kafkaSimilarity;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Thread thread = new Thread(kafkaSimilarity);

        thread.setName("KafkaSimilarity");
        thread.start();
    }
}
