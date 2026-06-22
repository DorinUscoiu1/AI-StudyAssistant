package org.example.ai_studyassistant.controller;

import lombok.RequiredArgsConstructor;
import org.example.ai_studyassistant.service.AgentChatService;
import org.example.ai_studyassistant.agent.StudyAssistantAgent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent-chat")
@RequiredArgsConstructor
public class AgentChatController {

    private final AgentChatService chatService;
    private final StudyAssistantAgent agent;

    @GetMapping("/{username}")
    public ResponseEntity<List<?>> getChat(@PathVariable String username) {
        return ResponseEntity.ok(chatService.getChatForUser(username));
    }

    @PostMapping("/{username}/message")
    public ResponseEntity<Map<String, Object>> postMessage(@PathVariable String username, @RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        if (text.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));

        // save user message
        chatService.saveUserMessage(username, text);

        // call agent (sync)
        String agentResp = agent.run(username, text);

        // save agent response
        chatService.saveAgentMessage(username, agentResp);

        return ResponseEntity.ok(Map.of("success", true, "agentResponse", agentResp));
    }
}

