package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.domain.Interaction;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.repository.InteractionRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionService {

    private static final Map<ActionTypeAvro, Double> ACTION_WEIGHTS = Map.of(
            ActionTypeAvro.VIEW, 0.4,
            ActionTypeAvro.REGISTER, 0.8,
            ActionTypeAvro.LIKE, 1.0
    );

    private final InteractionRepository interactionRepository;

    @Transactional
    public void saveInteraction(UserActionAvro userActionAvro) {
        log.info("Saving interaction {}", userActionAvro);
        Interaction interaction = Interaction.builder()
                .eventId((long) userActionAvro.getEventId())
                .userId((long) userActionAvro.getUserId())
                .rating(ACTION_WEIGHTS.get(userActionAvro.getActionType()))
                .ts(userActionAvro.getTimestamp())
                .build();
        interactionRepository.save(interaction);
    }
}
