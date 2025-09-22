package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.domain.Interaction;

import java.util.List;
import java.util.Map;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> getInteractionsByUserId(Long userId);

    List<Interaction> findTopNByUserIdOrderByTsDesc(Long userId);

    @Query("""
    SELECT sub.eventId, SUM(sub.maxRating)
    FROM (SELECT i.eventId as eventId, i.userId as userId, MAX(i.rating) as maxRating
          FROM Interaction i
          WHERE i.eventId = ?1
          GROUP BY i.eventId, i.userId) sub
    GROUP BY sub.eventId""")
    Map<Long, Integer> findMaxWeightsByEventId(Long eventId);
}