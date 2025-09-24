package client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.dto.RecommendationDto;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
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
                .map(rec -> new RecommendationDto(rec.getEventId(), rec.getScore()))
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
                .map(rec -> new RecommendationDto(rec.getEventId(), rec.getScore()))
                .toList();
    }

    public List<RecommendationDto> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);

        return asStream(iterator)
                .map(rec -> new RecommendationDto(rec.getEventId(), rec.getScore()))
                .toList();
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
