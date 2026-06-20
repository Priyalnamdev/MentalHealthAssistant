package com.example.MentalHealthAssistantApplication.repository;

import com.example.MentalHealthAssistantApplication.entity.Mood;
import com.example.MentalHealthAssistantApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Long> {

    /**
     * Fetch all mood entries for a specific user.
     * Returns list ordered by default (insertion order).
     * Spring Data JPA auto-generates query from method name.
     */
    List<Mood> findByUser(User user);

    /**
     * Fetch mood history for a user, sorted by date descending.
     * Most recent mood entry comes first.
     */
    List<Mood> findByUserOrderByDateDesc(User user);
}
