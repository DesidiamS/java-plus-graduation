package ru.practicum.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.service.RecommendationService;

import java.util.Iterator;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AnalyzerController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            Iterator<RecommendedEventProto> iterator = recommendationService.getRecommendationsForUser(request);
            iterator.forEachRemaining(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.info("Ошибка при получении рекомендаций для пользователя: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            Iterator<RecommendedEventProto> iterator = recommendationService.getRecommendedEvents(request);
            iterator.forEachRemaining(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.info("Ошибка при получении похожих мероприятий: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            Iterator<RecommendedEventProto> iterator = recommendationService.getInteractionsCount(request);
            iterator.forEachRemaining(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.info("Ошибка при получении взаимодействий с мероприятиями: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }
}
