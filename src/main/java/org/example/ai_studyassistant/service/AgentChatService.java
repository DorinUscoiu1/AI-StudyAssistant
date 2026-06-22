package org.example.ai_studyassistant.service;

import lombok.RequiredArgsConstructor;
import org.example.ai_studyassistant.model.AgentChatMessage;
import org.example.ai_studyassistant.repository.AgentChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentChatService {

    private final AgentChatMessageRepository repo;

    public AgentChatMessage saveUserMessage(String username, String content) {
        AgentChatMessage m = new AgentChatMessage(username, "USER", content);
        return repo.save(m);
    }

    public AgentChatMessage saveAgentMessage(String username, String content) {
        AgentChatMessage m = new AgentChatMessage(username, "AGENT", content);
        return repo.save(m);
    }

    public List<AgentChatMessage> getChatForUser(String username) {
        return repo.findByUsernameOrderByCreatedAtAsc(username);
    }
}

