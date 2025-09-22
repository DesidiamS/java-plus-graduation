package client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.dto.RecommendationDto;
import ru.practicum.grpc.stats.analyzer.recommendations.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendations.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendations.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendations.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendations.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class AnalyzerClient {

    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public AnalyzerClient(@GrpcClient("analyzer") RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client) {
        this.client = client;
    }

    public List<RecommendationDto> getRecommendations(Long userId, Integer limit) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(Math.toIntExact(userId))
                .setMaxResults(limit)
                .build();

        Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);

        return asStream(iterator)
                .map(rec -> new RecommendationDto((long) rec.getEventId(), rec.getScore()))
                .toList();
    }

    public List<RecommendationDto> getSimilarEvents(Long userId, Long eventId, Integer limit) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(Math.toIntExact(eventId))
                .setUserId(Math.toIntExact(userId))
                .setMaxResults(limit)
                .build();

        Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);

        return asStream(iterator)
                .map(rec -> new RecommendationDto((long) rec.getEventId(), rec.getScore()))
                .toList();
    }

    public List<RecommendationDto> getInteractionsCount(Long eventId) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .setEventId(Math.toIntExact(eventId))
                .build();

        Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);

        return asStream(iterator)
                .map(rec -> new RecommendationDto((long) rec.getEventId(), rec.getScore()))
                .toList();
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
