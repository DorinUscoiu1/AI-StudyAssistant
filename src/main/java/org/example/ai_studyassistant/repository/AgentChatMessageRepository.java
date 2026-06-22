package org.example.ai_studyassistant.repository;

import org.example.ai_studyassistant.model.AgentChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentChatMessageRepository extends JpaRepository<AgentChatMessage, Long> {
    List<AgentChatMessage> findByUsernameOrderByCreatedAtAsc(String username);
}

