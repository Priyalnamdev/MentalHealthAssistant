package com.example.MentalHealthAssistantApplication.repository;


import com.example.MentalHealthAssistantApplication.entity.ChatHistory;
import com.example.MentalHealthAssistantApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    /**
     * Fetch all chat messages for a specific user.
     * Returns full conversation history.
     */
    List<ChatHistory> findByUser(User user);

    /**
     * Fetch chat history for a user, sorted by timestamp descending.
     * Most recent conversation comes first.
     */
    List<ChatHistory> findByUserOrderByTimestampDesc(User user);


}
