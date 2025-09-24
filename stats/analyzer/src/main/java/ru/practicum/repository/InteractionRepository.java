package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.domain.Interaction;

import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> getInteractionsByUserId(Long userId);

    List<Interaction> findTopNByUserIdOrderByTsDesc(Long userId);

    @Query("""
            SELECT i.eventId, SUM(i.rating)
            FROM Interaction i
            WHERE i.eventId in ?1
            GROUP BY i.eventId
            """)
    List<Object[]> findMaxWeightsByEventId(List<Long> eventIds);

    Interaction findByUserIdAndEventId(Long userId, Long eventId);
}