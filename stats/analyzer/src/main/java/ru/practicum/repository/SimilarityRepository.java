package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.domain.Similarity;

import java.util.List;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    List<Similarity> getSimilaritiesByEvent1OrEvent2(Long event1, Long event2);

    @Query("""
    SELECT sim
      FROM Similarity sim
     WHERE NOT EXISTS (SELECT 1 FROM Interaction i WHERE i.userId = ?1 AND i.eventId in (sim.event1, sim.event2))
       AND (sim.event1 = ?2 OR sim.event2 = ?2)
     ORDER BY sim.similarity desc
     LIMIT ?3"""
    )
    List<Similarity> getRecommendations(Long userId, Long eventId, int limit);
}