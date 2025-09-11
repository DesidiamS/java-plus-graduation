package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Boolean existsByNameIgnoreCase(String name);
}
