package org.example.ai_studyassistant.internal;

import lombok.RequiredArgsConstructor;
import org.example.ai_studyassistant.mcp.StudyAssistantMcpTools;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/mcp-test")
@RequiredArgsConstructor
public class McpTestController {

    private final StudyAssistantMcpTools mcpTools;

    @PostMapping("/generate-quiz")
    public ResponseEntity<Map<String, Object>> generateQuiz(@RequestParam String username,
                                                            @RequestParam String topic,
                                                            @RequestParam(defaultValue = "3") int count) {
        Map<String, Object> res = mcpTools.generateQuiz(username, topic, count);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/list-conversations")
    public ResponseEntity<Object> listConversations(@RequestParam String username) {
        Object res = mcpTools.listConversations(username);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/ask-question")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestParam String username,
                                                           @RequestParam long conversationId,
                                                           @RequestParam String questionText) {
        Map<String, Object> res = mcpTools.askQuestion(username, conversationId, questionText);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/analyze-link")
    public ResponseEntity<Map<String, Object>> analyzeLink(@RequestParam String url,
                                                           @RequestParam(required = false) String subject) {
        Map<String, Object> res = mcpTools.analyzeLink(url, subject == null ? "General" : subject);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/get-quiz")
    public ResponseEntity<Map<String, Object>> getQuiz(@RequestParam long quizId, @RequestParam String username) {
        Map<String, Object> res = mcpTools.getQuiz(quizId, username);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/submit-quiz-attempt")
    public ResponseEntity<Map<String, Object>> submitQuizAttempt(@RequestBody Map<String, Object> request) {
        String username = (String) request.getOrDefault("username", "defaultUser");
        long quizId = ((Number) request.getOrDefault("quizId", 0)).longValue();
        Map<String, Object> answers = (Map<String, Object>) request.getOrDefault("answers", Map.of());
        Map<String, Object> res = mcpTools.submitQuizAttempt(username, quizId, answers);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/user-stats")
    public ResponseEntity<Map<String, Object>> userStats(@RequestParam String username) {
        Map<String, Object> res = mcpTools.getUserStats(username);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/summarize-conversation")
    public ResponseEntity<Map<String, Object>> summarizeConv(@RequestParam long conversationId) {
        Map<String, Object> res = mcpTools.summarizeConversation(conversationId);
        return ResponseEntity.ok(res);
    }
}
