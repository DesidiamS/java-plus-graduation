package ru.practicum.service;

import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.Iterator;

public interface RecommendationService {

    Iterator<RecommendedEventProto> getRecommendedEvents(SimilarEventsRequestProto request);

    Iterator<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

    Iterator<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}
