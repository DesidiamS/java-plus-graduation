package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.practicum.processor.EventSimilarityProcessor;
import ru.practicum.processor.UserActionProcessor;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements ApplicationRunner {

    private final EventSimilarityProcessor eventSimilarityProcessor;
    private final UserActionProcessor userActionProcessor;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Thread hubEventProcessorThread = new Thread(eventSimilarityProcessor);
        hubEventProcessorThread.setName("EventSimilarityProcessor");
        hubEventProcessorThread.start();

        Thread snapshotProcessorThread = new Thread(userActionProcessor);
        snapshotProcessorThread.setName("UserActionProcessor");
        snapshotProcessorThread.start();
    }
}
