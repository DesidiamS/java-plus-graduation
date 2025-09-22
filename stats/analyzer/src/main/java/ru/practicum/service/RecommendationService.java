package ru.practicum.service;

import ru.practicum.grpc.stats.recommendations.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendations.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendations.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendations.UserPredictionsRequestProto;

import java.util.Iterator;

public interface RecommendationService {

    Iterator<RecommendedEventProto> getRecommendedEvents(SimilarEventsRequestProto request);

    Iterator<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

    Iterator<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}
