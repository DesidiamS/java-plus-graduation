package ru.practicum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EventCompilation;

import java.util.List;

public interface EventCompilationRepository extends JpaRepository<EventCompilation, Long> {

    @Query("SELECT ec FROM EventCompilation ec WHERE ec.eventCompilationId.compilationId in ?1")
    List<EventCompilation> findAllByCompilationIdIn(List<Long> compilationId);

}
