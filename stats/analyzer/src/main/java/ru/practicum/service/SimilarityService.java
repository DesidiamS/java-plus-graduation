package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.domain.Similarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.repository.SimilarityRepository;

@Service
@RequiredArgsConstructor
public class SimilarityService {

    private final SimilarityRepository similarityRepository;

    public void saveSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        Similarity similarity = Similarity.builder()
                .event1((long) eventSimilarityAvro.getEventA())
                .event2((long) eventSimilarityAvro.getEventB())
                .similarity(eventSimilarityAvro.getScore())
                .ts(eventSimilarityAvro.getTimestamp())
                .build();

        similarityRepository.save(similarity);
    }
}
