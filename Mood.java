package com.example.MentalHealthAssistantApplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "moods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mood;


    /**
     * Many moods can belong to one user.
     * Stores user_id as foreign key in moods table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private LocalDate date;

    @PrePersist
    public void prePersist(){
        this.date = LocalDate.now();
    }

}

