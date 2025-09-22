package client;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.dto.UserActionDto;
import ru.practicum.grpc.stats.user.ActionTypeProto;
import ru.practicum.grpc.stats.user.UserActionControllerGrpc;
import ru.practicum.grpc.stats.user.UserActionProto;

@Service
@Slf4j
public class CollectorClient {

    private final UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public CollectorClient(@GrpcClient("collector") UserActionControllerGrpc.UserActionControllerBlockingStub client) {
        this.client = client;
    }

    public void createHit(UserActionDto request) {
         log.info("Creating hit for user {}", request.getUserId());
        UserActionProto userActionProto = getUserActionProto(request);

        log.info("Created hit for user {}", userActionProto.getUserId());
        client.collectUserAction(userActionProto);
        log.info("Hit was sent for user {}", userActionProto.getUserId());
    }

    private UserActionProto getUserActionProto(UserActionDto userActionDto) {
        ActionTypeProto actionTypeProto = null;
        switch (userActionDto.getActionType()) {
            case ACTION_VIEW -> actionTypeProto = ActionTypeProto.ACTION_VIEW;
            case ACTION_REGISTER -> actionTypeProto = ActionTypeProto.ACTION_REGISTER;
            case ACTION_LIKE -> actionTypeProto = ActionTypeProto.ACTION_LIKE;
        }

        Timestamp timestamp = Timestamp.newBuilder().setNanos(userActionDto.getTs().getNano()).build();

        return UserActionProto.newBuilder()
                .setUserId(Math.toIntExact(userActionDto.getUserId()))
                .setEventId(Math.toIntExact(userActionDto.getEventId()))
                .setActionType(actionTypeProto)
                .setTimestamp(timestamp)
                .build();
    }
}
