package org.example.ai_studyassistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_studyassistant.model.Question;
import org.example.ai_studyassistant.model.Topic;
import org.example.ai_studyassistant.model.LinkAnalysis;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generates AI response with context from recent messages.
     * Implements Mark 8: contextual + parameterized AI.
     */
    public String generateResponse(Question currentQuestion, List<Question> recentQuestions) {
        List<Message> messages = new ArrayList<>();

        // 1️⃣ System prompt (role + style + topic)
        messages.add(new SystemMessage(buildSystemPrompt(currentQuestion)));

        // 2️⃣ Context: last questions + AI answers
        for (Question recentQuestion : recentQuestions) {

            messages.add(
                    new UserMessage(recentQuestion.getQuestionText())
            );

            if (recentQuestion.getAiResponse() != null) {
                messages.add(
                        new AssistantMessage(
                                recentQuestion.getAiResponse().getResponseText()
                        )
                );
            }
        }

        // 3️⃣ Current user question
        messages.add(
                new UserMessage(currentQuestion.getQuestionText())
        );

        log.info("Calling AI with {} messages (context included)", messages.size());

        try {
            Prompt prompt = new Prompt(messages);

            String response = chatModel
                    .call(prompt)
                    .getResult()
                    .getOutput()
                    .getText();

            log.debug("AI response generated successfully");
            return response;

        } catch (Exception e) {
            log.error("AI call failed: {}", e.getMessage(), e);
            return buildFallbackResponse(currentQuestion);
        }
    }

    /** New: with extra extracted text context (e.g., from PDF/image). */
    public String generateResponseWithContext(Question currentQuestion, List<Question> recentQuestions, String extractedText) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildSystemPrompt(currentQuestion)));
        for (Question recentQuestion : recentQuestions) {
            messages.add(new UserMessage(recentQuestion.getQuestionText()));
            if (recentQuestion.getAiResponse() != null) {
                messages.add(new AssistantMessage(recentQuestion.getAiResponse().getResponseText()));
            }
        }
        if (extractedText != null && !extractedText.isBlank()) {
            messages.add(new UserMessage("Context din document/imagine (extras):\n" + extractedText));
        }
        messages.add(new UserMessage(currentQuestion.getQuestionText()));
        log.info("Calling AI with {} messages (extra context)", messages.size());
        try {
            return chatModel.call(new Prompt(messages)).getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("AI call failed: {}", e.getMessage(), e);
            return buildFallbackResponse(currentQuestion);
        }
    }

    /**
     * Analyze a web link for a specific subject and return raw JSON (validated).
     * The JSON schema matches LinkAnalysis.java fields.
     */
    public String analyzeLinkForSubjectJson(String url, String subject) {
        try {
            String pageText = fetchUrlTextSafe(url);
            String systemPrompt = """
                You are an assistant that evaluates how useful a web link is for teaching or discussing a specific academic subject.
                Return ONLY a JSON object with these exact fields:
                - usefulness: one of "HIGH", "MEDIUM", "LOW"
                - score: integer 0-100 (higher means more useful)
                - reason: short explanation why the link is useful or not (max 200 chars)
                - highlights: array of up to 5 short strings (quotes or sections that support your judgement)
                Return pure JSON only, no markdown or code fences.
                """;

            String userTemplate = "Subject: {subject}\nURL: {url}\n\nPage text (truncated):\n{page}\n\nEvaluate usefulness for the subject and return the JSON object described above.";

            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.add(new UserMessage(userTemplate
                    .replace("{subject}", subject != null ? subject : "")
                    .replace("{url}", url != null ? url : "")
                    .replace("{page}", pageText)
            ));

            String response = chatModel.call(new Prompt(messages)).getResult().getOutput().getText();
            String cleaned = cleanJsonResponse(response);

            // Validate by attempting to parse
            validateLinkAnalysisJson(cleaned);
            return cleaned;
        } catch (Exception e) {
            log.error("Error analyzing link {} for subject {}: {}", url, subject, e.getMessage());
            return "{\"usefulness\":\"LOW\",\"score\":0,\"reason\":\"Analysis failed\",\"highlights\":[]}";
        }
    }

    public LinkAnalysis analyzeLinkForSubject(String url, String subject) {
        try {
            String json = analyzeLinkForSubjectJson(url, subject);
            return objectMapper.readValue(json, LinkAnalysis.class);
        } catch (Exception e) {
            log.warn("Parsing LinkAnalysis failed for {}: {}", url, e.getMessage());
            return new LinkAnalysis("LOW", 0, "Analysis unavailable", new ArrayList<>());
        }
    }

    // Simple HTML/text fetcher — similar to QuestionService.fetchUrlTextSafe
    private String fetchUrlTextSafe(String url) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Mozilla/5.0 (StudyAssistant)")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String contentType = response.headers().firstValue("content-type").orElse("").toLowerCase();
                if (!contentType.contains("text/html") && !contentType.contains("text/plain")) {
                    return "";
                }
                String html = new String(response.body());
                if (html.length() > 200000) { // 200 KB cap
                    html = html.substring(0, 200000);
                }
                // crude text extraction
                html = html.replaceAll("(?is)<script[^>]*>.*?</script>", " ")
                           .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
                           .replaceAll("(?is)<[^>]+>", " ")
                           .replaceAll("&nbsp;", " ");
                String text = html.replaceAll("\\s+", " ").trim();
                if (text.length() > 15000) text = text.substring(0, 15000) + "...";
                return text;
            }
            return "";
        } catch (Exception e) {
            log.warn("URL fetch failed for {}: {}", url, e.getMessage());
            return "";
        }
    }

    // Reuse existing private methods (we add small helpers used above)
    private String cleanJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return "{}";
        }

        String cleaned = response.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();
        if (!cleaned.startsWith("{") && !cleaned.startsWith("[")) {
            log.warn("Response doesn't look like JSON after cleaning: {}", cleaned.substring(0, Math.min(100, cleaned.length())));
            return "{}";
        }
        return cleaned;
    }

    private void validateLinkAnalysisJson(String json) throws com.fasterxml.jackson.core.JsonProcessingException {
        objectMapper.readValue(json, LinkAnalysis.class);
    }

    /**
     * Fallback response when AI is unavailable.
     */
    private String buildFallbackResponse(Question question) {
        String topic = question.getConversation().getTopic().getDisplayName();

        // include the word 'eroare' so unit tests expecting it pass
        return String.format(
                " A apărut o eroare: Serviciul AI este temporar indisponibil.\n\n" +
                        "Subiect: %s\n\n" +
                        " Recomandări:\n" +
                        "- Consultă documentația oficială\n" +
                        "- Caută tutoriale și exemple practice\n" +
                        "- Reformulează întrebarea mai specific\n\n" +
                        " Încearcă din nou mai târziu sau verifică OpenAI API Key.",
                topic
        );
    }

    /**
     * Builds system prompt based on role, style and topic.
     */
    private String buildSystemPrompt(Question question) {
        Topic topic = question.getConversation().getTopic();
        String topicInstruction = topic != null ? topic.getInstruction() : "";

        String baseInstruction = """
    REGULI GENERALE (OBLIGATORII):
    - Răspunde folosind Markdown.
    - Structurează răspunsul cu titluri și liste.
    - Codul, formulele sau pseudo-codul trebuie puse în blocuri ``` ``` .
    - Explică pas-cu-pas, clar și pe nivel de elev.
    - Evită paragrafele foarte lungi; aerisește răspunsul.
    """;

        return String.format(
                "%s\n\n%s\n\n%s\n\n" +
                        "Subiect: %s\n\n" +
                        "%s\n\n" +
                        "Răspunde în limba română.",
                question.getAiRole().getRolePrompt(),
                question.getResponseStyle().getSystemPrompt(),
                baseInstruction,
                topic != null ? topic.getDisplayName() : "General",
                topicInstruction
        );
    }

}
