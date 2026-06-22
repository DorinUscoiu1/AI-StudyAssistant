package org.example.ai_studyassistant.repository;

import org.example.ai_studyassistant.model.Conversation;
import org.example.ai_studyassistant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserOrderByUpdatedAtDesc(User user);
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    default String analyzeLink(String url, String subject) {
        return "Analysis result for " + subject + " at " + url;
    }
}
