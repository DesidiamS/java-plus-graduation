package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.domain.Interaction;
import ru.practicum.domain.Similarity;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.repository.InteractionRepository;
import ru.practicum.repository.SimilarityRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;

    @Override
    public Iterator<RecommendedEventProto> getRecommendedEvents(SimilarEventsRequestProto request) {
        log.info("Getting recommendation events for user {}", request.getUserId());
        log.info("getRecommendedEvents request: {}", request);
        List<Similarity> similarities = similarityRepository.getRecommendations(
                request.getUserId(), request.getEventId(), request.getMaxResults());

        Iterator<RecommendedEventProto> recommendations = similarities.stream()
                .map(similarity -> RecommendedEventProto.newBuilder()
                        .setEventId(Math.toIntExact(similarity.getEvent1()))
                        .setScore(similarity.getSimilarity())
                        .build())
                .collect(Collectors.toList()).iterator();

        log.info("getRecommendedEvents response: {}", recommendations);

        return recommendations;
    }

    @Override
    public Iterator<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        log.info("Getting recommendation for user {}", request.getUserId());
        List<Interaction> recentInteractions = interactionRepository
                .findTopNByUserIdOrderByTsDesc(request.getUserId());

        if (recentInteractions.isEmpty()) {
            return Collections.emptyIterator();
        }

        Set<Long> seenEvents = recentInteractions.stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        Set<Long> candidateEvents = new HashSet<>();
        for (Interaction interaction : recentInteractions) {
            List<Similarity> similarities = similarityRepository
                    .getSimilaritiesByEvent1OrEvent2(interaction.getEventId(), interaction.getEventId());

            for (Similarity sim : similarities) {
                Long other = sim.getEvent1().equals(interaction.getEventId())
                        ? sim.getEvent2()
                        : sim.getEvent1();
                if (!seenEvents.contains(other)) {
                    candidateEvents.add(other);
                }
            }
        }

        return candidateEvents.stream()
                .map(eventId -> RecommendedEventProto.newBuilder()
                        .setEventId(Math.toIntExact(eventId))
                        .setScore(predictRating(request.getUserId(), eventId, request.getMaxResults()))
                        .build())
                .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed())
                .limit(request.getMaxResults())
                .toList().iterator();
    }

    private double predictRating(Long userId, Long candidateEvent, int kNeighbors) {
        List<Interaction> userInteractions = interactionRepository.getInteractionsByUserId(userId);
        List<Similarity> similarities = similarityRepository
                .getSimilaritiesByEvent1OrEvent2(candidateEvent, candidateEvent);

        List<Map<String, Double>> neighbors = getNeighbors(candidateEvent, userInteractions, similarities);

        List<Map<String, Double>> topK = neighbors.stream()
                .sorted((a, b) -> Double.compare(b.get("similarity"), a.get("similarity")))
                .limit(kNeighbors)
                .toList();

        double weightedSum = topK.stream()
                .mapToDouble(n -> n.get("similarity") * n.get("rating"))
                .sum();

        double simSum = topK.stream()
                .mapToDouble(n -> n.get("similarity"))
                .sum();

        return simSum == 0 ? 0 : weightedSum / simSum;
    }

    private static List<Map<String, Double>> getNeighbors(Long candidateEvent, List<Interaction> userInteractions, List<Similarity> similarities) {
        List<Map<String, Double>> neighbors = new ArrayList<>();
        for (Interaction interaction : userInteractions) {
            for (Similarity sim : similarities) {
                Long other = sim.getEvent1().equals(candidateEvent) ? sim.getEvent2() : sim.getEvent1();
                if (interaction.getEventId().equals(other)) {
                    Map<String, Double> neighbor = new HashMap<>();
                    neighbor.put("similarity", sim.getSimilarity());
                    neighbor.put("rating", interaction.getRating());
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    @Override
    public Iterator<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        log.info("Getting interactions count request: {}", request);
        log.info("Received proto class: {}", request.getClass());
        List<Long> eventId = request.getEventIdList();
        log.info("Event Id: {}", eventId);

        List<Object[]> raw = interactionRepository.findMaxWeightsByEventId(eventId);

        List<RecommendedEventProto> recommendedList;

        Map<Long, Double> result = raw.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).doubleValue()
                ));

        recommendedList = result.entrySet().stream()
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();

        return recommendedList.iterator();
    }
}
