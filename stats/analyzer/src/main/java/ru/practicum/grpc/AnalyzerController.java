package ru.practicum.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.stats.analyzer.recommendations.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendations.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendations.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendations.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendations.UserPredictionsRequestProto;
import ru.practicum.service.RecommendationService;

import java.util.Iterator;

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
            responseObserver.onError(e);
        }
    }
}
