package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SimilarityService {

    private final Map<Long, Map<Long, Double>> eventUserWeights = new ConcurrentHashMap<>();

    private final Map<Long, Double> eventSums = new ConcurrentHashMap<>();

    private final Map<Long, Map<Long, Double>> minSums = new ConcurrentHashMap<>();

    private final Map<Long, Map<Long, Double>> userEventWeights = new ConcurrentHashMap<>();

    private static final Map<ActionTypeAvro, Double> ACTION_WEIGHTS = Map.of(
            ActionTypeAvro.VIEW, 0.4,
            ActionTypeAvro.REGISTER, 0.8,
            ActionTypeAvro.LIKE, 1.0
    );

    private double getWeight(ActionTypeAvro type) {
        return ACTION_WEIGHTS.getOrDefault(type, 0.0);
    }

    public List<EventSimilarityAvro> getEventSimilarity(UserActionAvro userActionAvro) {
        List<EventSimilarityAvro> updates = onUserAction(userActionAvro);

        return updates.stream()
                .map(rec -> EventSimilarityAvro.newBuilder()
                        .setEventA(rec.getEventA())
                        .setEventB(rec.getEventB())
                        .setScore(rec.getScore())
                        .setTimestamp(userActionAvro.getTimestamp())
                        .build())
                .toList();
    }

    private double computeSimilarity(Long e1, Long e2) {
        double sMin = get(e1, e2);
        double s1 = eventSums.getOrDefault(e1, 0.0);
        double s2 = eventSums.getOrDefault(e2, 0.0);

        if (s1 == 0 || s2 == 0) return 0.0;
        return sMin / (Math.sqrt(s1) * Math.sqrt(s2));
    }

    /*private List<EventSimilarityAvro> onUserAction(UserActionAvro userActionAvro) {
        List<EventSimilarityAvro> updates = new ArrayList<>();

        long eventId = userActionAvro.getEventId();
        long userId = userActionAvro.getUserId();
        double weight = getWeight(userActionAvro.getActionType());

        Map<Long, Double> userWeights =
                eventUserWeights.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>());

        double oldWeight = userWeights.getOrDefault(userId, 0.0);
        double newWeight = Math.max(oldWeight, weight);

        if (newWeight == oldWeight) {
            return updates;
        }

        userWeights.put(userId, newWeight);
        double deltaEvent = newWeight - oldWeight;
        eventSums.merge(eventId, deltaEvent, Double::sum);

        for (Map.Entry<Long, Map<Long, Double>> entry : eventUserWeights.entrySet()) {
            Long otherEventId = entry.getKey();
            if (otherEventId.equals(eventId)) continue;

            Map<Long, Double> otherUsers = entry.getValue();
            double wOther = otherUsers.getOrDefault(userId, 0.0);

            double oldMin = Math.min(oldWeight, wOther);
            double newMin = Math.min(newWeight, wOther);
            double deltaMin = newMin - oldMin;

            if (deltaMin != 0.0) {
                add(eventId, otherEventId, deltaMin);

                double score = computeSimilarity(eventId, otherEventId);
                long first = Math.min(eventId, otherEventId);
                long second = Math.max(eventId, otherEventId);

                updates.add(EventSimilarityAvro.newBuilder()
                        .setEventA((int) first)
                        .setEventB((int) second)
                        .setScore(score)
                        .setTimestamp(userActionAvro.getTimestamp())
                        .build());
            }
        }

        return updates;
    }*/

    private List<EventSimilarityAvro> onUserAction(UserActionAvro userActionAvro) {
        List<EventSimilarityAvro> updates = new ArrayList<>();

        long eventId = userActionAvro.getEventId();
        long userId = userActionAvro.getUserId();
        double weight = getWeight(userActionAvro.getActionType());

        Map<Long, Double> userWeights =
                eventUserWeights.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>());

        double oldWeight = userWeights.getOrDefault(userId, 0.0);
        double newWeight = Math.max(oldWeight, weight);

        if (Double.compare(newWeight, oldWeight) == 0) {
            return updates;
        }

        userWeights.put(userId, newWeight);

        double deltaEvent = newWeight - oldWeight;
        eventSums.merge(eventId, deltaEvent, Double::sum);

        Map<Long, Double> eventsOfUser =
                userEventWeights.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        eventsOfUser.put(eventId, newWeight);

        for (Map.Entry<Long, Double> entry : eventsOfUser.entrySet()) {
            long otherEventId = entry.getKey();
            if (otherEventId == eventId) continue;

            double wOther = entry.getValue();
            double oldMin = Math.min(oldWeight, wOther);
            double newMin = Math.min(newWeight, wOther);

            if (newMin != oldMin) {
                add(eventId, otherEventId, newMin - oldMin);
            }

            double score = computeSimilarity(eventId, otherEventId);
            if (score > 0) {
                updates.add(EventSimilarityAvro.newBuilder()
                        .setEventA((int) Math.min(eventId, otherEventId))
                        .setEventB((int) Math.max(eventId, otherEventId))
                        .setScore(score)
                        .setTimestamp(userActionAvro.getTimestamp())
                        .build());
            }
        }

        log.debug("onUserAction for event {} user {}: oldWeight={}, newWeight={}, deltaEvent={}, updates={}",
                eventId, userId, oldWeight, newWeight, deltaEvent, updates.size());

        return updates;
    }


    private void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void add(long eventA, long eventB, double delta) {
        double oldValue = get(eventA, eventB);
        put(eventA, eventB, oldValue + delta);
    }
}
