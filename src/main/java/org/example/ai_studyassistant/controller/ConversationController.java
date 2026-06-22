package org.example.ai_studyassistant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_studyassistant.dto.ConversationCreateRequest;
import org.example.ai_studyassistant.model.LinkAnalysis;
import org.example.ai_studyassistant.model.Topic;
import org.example.ai_studyassistant.service.AIService;
import org.example.ai_studyassistant.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final AIService aiService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ConversationCreateRequest request,
                                    BindingResult result,
                                    Authentication authentication) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            var conv = conversationService.createConversation(request, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Conversation created", "conversationId", conv.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        try {
            conversationService.deleteConversation(id, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Conversation deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/analyze-link")
    public ResponseEntity<LinkAnalysis> analyzeLinkInConversation(@RequestBody Map<String, String> request, Authentication authentication) {
        String url = request.get("url");

        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Topic topicEnum = conversationService.getCurrentConversationTopic(authentication.getName());
        String topic = (topicEnum != null) ? topicEnum.getDisplayName() : "General";

        LinkAnalysis result = aiService.analyzeLinkForSubject(url, topic);
        return ResponseEntity.ok(result);
    }
}
