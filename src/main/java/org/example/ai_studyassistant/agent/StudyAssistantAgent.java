package org.example.ai_studyassistant.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_studyassistant.mcp.StudyAssistantMcpTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyAssistantAgent {

    private final ChatClient chatClient;
    private final StudyAssistantMcpTools tools;

    public String run(String username, String input) {
        log.error(" AGENT START: {}", input);
        return chatClient
                .prompt()
                .user("""
                    User: %s
                    Cerere: %s
                """.formatted(username, input))
                .tools(tools)
                .call()
                .content();
    }
}
