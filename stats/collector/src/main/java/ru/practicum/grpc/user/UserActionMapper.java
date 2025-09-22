package ru.practicum.grpc.user;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.user.ActionTypeProto;
import ru.practicum.grpc.stats.user.UserActionProto;

import java.time.Instant;


@Component
public class UserActionMapper {

    public UserActionAvro toUserActionAvro(UserActionProto request) {

        Instant instant = Instant.ofEpochSecond(request.getTimestamp().getSeconds(), request.getTimestamp().getNanos());

        ActionTypeAvro actionTypeAvro;

        switch (request.getActionType()) {
            case ACTION_VIEW -> actionTypeAvro = ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> actionTypeAvro = ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> actionTypeAvro = ActionTypeAvro.LIKE;
            default -> throw new IllegalArgumentException("Unknown action type: " + request.getActionType());
        }

        return UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setTimestamp(instant)
                .setActionType(actionTypeAvro)
                .build();
    }

    private ActionTypeAvro toActionType(ActionTypeProto request) {
        return ActionTypeAvro.valueOf(request.name());
    }
}
