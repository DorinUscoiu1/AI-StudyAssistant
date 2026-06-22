package org.example.ai_studyassistant.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_studyassistant.dto.QuestionRequest;
import org.example.ai_studyassistant.model.*;
import org.example.ai_studyassistant.service.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyAssistantMcpTools {

    private final QuizService quizService;
    private final ConversationService conversationService;
    private final QuestionService questionService;
    private final AIService aiService;

    // ---------------- QUIZ ----------------
    @Tool(description = "Generează un quiz pentru un utilizator și subiect")
    public Map<String, Object> generateQuiz(String username, String topic, int count) {
        log.info("MCP Tool Call: generateQuiz for {} on {}", username, topic);
        Topic topicEnum;
        try {
            topicEnum = Topic.valueOf(topic.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Subiect invalid: " + topic);
        }
        Quiz quiz = quizService.generateQuiz(username, topicEnum, count);
        return Map.of(
                "quizId", quiz.getId(),
                "title", quiz.getTitle(),
                "questionCount", quiz.getQuestions().size(),
                "status", "CREATED"
        );
    }

    // ---------------- CONVERSATIONS ----------------
    @Tool(description = "Listează conversațiile unui utilizator")
    public List<Map<String, Object>> listConversations(String username) {
        log.info("MCP Tool Call: listConversations for {}", username);
        return conversationService.getUserConversations(username).stream()
                .map(c -> Map.of(
                        "id", (Object) c.getId(),
                        "title", c.getTitle(),
                        "topic", c.getTopic().name(),
                        "date", c.getUpdatedAt().toString()
                ))
                .collect(Collectors.toList());
    }

    // ---------------- ASK QUESTION ----------------
    @Tool(description = "Adresarea unei întrebări în conversație")
    public Map<String, Object> askQuestion(String username, long conversationId, String questionText) {
        log.info("MCP Tool Call: askQuestion in conv {} by {}", conversationId, username);
        QuestionRequest req = new QuestionRequest();
        req.setConversationId(conversationId);
        req.setQuestionText(questionText);
        req.setResponseStyle(ResponseStyle.SIMPLE);
        req.setAiRole(AIRole.MENTOR);

        Question q = questionService.askQuestion(req, username);
        String responseText = (q.getAiResponse() != null) ? q.getAiResponse().getResponseText() : "Niciun răspuns.";

        return Map.of("questionId", q.getId(), "response", responseText);
    }

    // ---------------- LINK ANALYSIS ----------------
    @Tool(description = "Analizează un link pentru un subiect și returnează analiza")
    public Map<String, Object> analyzeLink(String url, String subject) {
        log.info("MCP Tool Call: analyzeLink {} for subject {}", url, subject);
        LinkAnalysis la = aiService.analyzeLinkForSubject(url, subject);
        return Map.of(
                "usefulness", la.getUsefulness(),
                "score", la.getScore(),
                "reason", la.getReason(),
                "highlights", la.getHighlights()
        );
    }

    // ---------------- GET QUIZ ----------------
    @Tool(description = "Obține detalii despre un quiz (întrebări, titlu, id)")
    public Map<String, Object> getQuiz(long quizId, String username) {
        log.info("MCP Tool Call: getQuiz {} for user {}", quizId, username);

        Quiz quiz = quizService.get(quizId, username);

        List<Map<String, Object>> questionsDto = quiz.getQuestions().stream()
                .map(q -> Map.<String, Object>of(
                        "id", q.getId(),
                        "questionText", q.getQuestionText(),
                        "optionA", q.getOptionA(),
                        "optionB", q.getOptionB(),
                        "optionC", q.getOptionC(),
                        "optionD", q.getOptionD()
                ))
                .collect(Collectors.toList());

        return Map.of(
                "quizId", (Object) quiz.getId(),
                "title", quiz.getTitle(),
                "topic", quiz.getTopic().name(),
                "questions", questionsDto
        );
    }
    // ---------------- SUBMIT QUIZ ATTEMPT ----------------
    @Tool(description = "Trimite un attempt pentru quiz: answers este un map string->string (cheia e id-ul intrebarii)")
    public Map<String, Object> submitQuizAttempt(String username, long quizId, Map<String, Object> answers) {
        log.info("MCP Tool Call: submitQuizAttempt quiz {} by {}", quizId, username);
        java.util.Map<Long, String> parsed = answers.entrySet().stream()
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(e -> {
                    try {
                        return Long.parseLong(e.getKey());
                    } catch (Exception ex) {
                        return -1L;
                    }
                }, e -> e.getValue() == null ? "" : e.getValue().toString()));

        // Remove invalid keys
        parsed.entrySet().removeIf(en -> en.getKey() <= 0);

        QuizAttempt attempt = quizService.submitAttempt(username, quizId, parsed);
        return Map.of(
                "attemptId", attempt.getId(),
                "score", attempt.getScore(),
                "total", attempt.getTotal()
        );
    }

    // ---------------- USER STATS ----------------
    @Tool(description = "Returnează statistici sumare pentru un utilizator (număr quiz-uri, întrebări, etc.)")
    public Map<String, Object> getUserStats(String username) {
        log.info("MCP Tool Call: getUserStats for {}", username);
        int quizzes = quizService.listUserQuizzes(username).size();
        int conversations = conversationService.getUserConversations(username).size();
        int questions = questionService.countUserQuestions(username);
        return Map.of(
                "quizzes", quizzes,
                "conversations", conversations,
                "questions", questions
        );
    }

    // ---------------- SUMMARIZE CONVERSATION ----------------
    @Tool(description = "Returnează un sumar scurt al unei conversații (titlu, număr întrebări, ultim răspuns AI)")
    public Map<String, Object> summarizeConversation(long conversationId) {
        log.info("MCP Tool Call: summarizeConversation {}", conversationId);
        Conversation conv = conversationService.getConversationById(conversationId);
        List<Question> qs = questionService.getConversationQuestions(conversationId);
        String lastAi = "";
        if (!qs.isEmpty()) {
            Question last = qs.get(qs.size() - 1);
            if (last.getAiResponse() != null) lastAi = last.getAiResponse().getResponseText();
        }
        return Map.of(
                "conversationId", conv.getId(),
                "title", conv.getTitle(),
                "topic", conv.getTopic().name(),
                "questionCount", qs.size(),
                "lastAiResponse", lastAi
        );
    }
}