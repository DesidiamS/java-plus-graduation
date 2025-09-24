package ru.practicum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "similarities")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Similarity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "event1", nullable = false)
    private Long event1;

    @NotNull
    @Column(name = "event2", nullable = false)
    private Long event2;

    @NotNull
    @Column(name = "similarity", nullable = false)
    private Double similarity;

    @NotNull
    @Column(name = "ts", nullable = false)
    private Instant ts;

}