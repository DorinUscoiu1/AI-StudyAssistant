package org.example.ai_studyassistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_studyassistant.dto.FeedbackRequest;
import org.example.ai_studyassistant.dto.QuestionRequest;
import org.example.ai_studyassistant.model.*;
import org.example.ai_studyassistant.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ConversationService conversationService;
    private final AIService aiService;
    private final AIResponseService aiResponseService;

    @Value("${app.question.limit.per-day:50}")
    private int questionLimitPerDay;

    @Transactional
    public Question askQuestion(QuestionRequest request, String username) {
        // Validate conversation ownership
        conversationService.validateUserOwnership(request.getConversationId(), username);

        // Check daily limit
        Conversation conversation = conversationService.getConversationById(request.getConversationId());
        long todayQuestions = questionRepository.countQuestionsByUserIdAndCreatedAtAfter(
                conversation.getUser().getId(),
                LocalDateTime.now().minusDays(1)
        );

        if (todayQuestions >= questionLimitPerDay) {
            throw new RuntimeException("Daily question limit reached: " + questionLimitPerDay);
        }

        // Create question
        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setResponseStyle(request.getResponseStyle());
        question.setAiRole(request.getAiRole());
        question.setConversation(conversation);

        question = questionRepository.save(question);
        log.info("Question created for conversation: {}", request.getConversationId());

        // Get conversation context (last 5 messages)
        List<Question> recentQuestions = getRecentQuestionsForContext(request.getConversationId());

        // Generate AI response
        String aiResponseText = aiService.generateResponse(question, recentQuestions);

        // Save AI response
        AIResponse aiResponse = aiResponseService.createResponse(question, aiResponseText);

        // Set the aiResponse on the question to avoid null reference
        question.setAiResponse(aiResponse);

        return question;
    }
    @Transactional
    public Question saveQuestionWithResponse(
            QuestionRequest request,
            String aiResponseText,
            String username
    ) {
        conversationService.validateUserOwnership(
                request.getConversationId(), username);

        Conversation conversation =
                conversationService.getConversationById(request.getConversationId());

        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setResponseStyle(request.getResponseStyle());
        question.setAiRole(request.getAiRole());
        question.setConversation(conversation);

        question = questionRepository.save(question);

        AIResponse aiResponse = aiResponseService.createResponse(question, aiResponseText);

        question.setAiResponse(aiResponse);
        return question;
    }

    private String extractPdfText(InputStream in) {
        try (PDDocument doc = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(doc);
            if (text.length() > 4000) {
                return text.substring(0, 4000) + "\n... (text trunchiat)";
            }
            return text;
        } catch (IOException e) {
            log.warn("PDF extraction failed: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Safe image OCR extraction that catches all Throwables (including native JNA Errors)
     * so that a faulty Tesseract/native library doesn't crash the JVM flow.
     */
    private String extractImageTextSafe(InputStream in, String filename) {
        try {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                return "[Imagine atașată: " + filename + "]\n";
            }
            try {
                Tesseract tesseract = new Tesseract();
                String ocr;
                try {
                    ocr = tesseract.doOCR(img);
                } catch (Throwable t) {
                    // Catch any native Errors (JNA) or other Throwables from Tess4J
                    log.warn("OCR native failure for {}: {}", filename, t.getMessage());
                    return "[Imagine atașată: " + filename + " - OCR eșuat]\n";
                }
                if (ocr != null && !ocr.isBlank()) {
                    if (ocr.length() > 4000) ocr = ocr.substring(0, 4000) + "\n... (text trunchiat)";
                    return "[Text extras din imagine: " + filename + "]\n" + ocr + "\n\n";
                } else {
                    return "[Imagine atașată: " + filename + " - fără text recunoscut]\n";
                }
            } catch (Throwable t) {
                log.warn("Tesseract init/failure for {}: {}", filename, t.getMessage());
                return "[Imagine atașată: " + filename + " - OCR eșuat]\n";
            }
        } catch (IOException e) {
            log.warn("Failed to read image {}: {}", filename, e.getMessage());
            return "[Imagine atașată: " + filename + " - nu s-a putut citi]\n";
        }
    }

    @Transactional
    public Question askQuestionWithFilesAndUrls(QuestionRequest request, MultipartFile[] files, List<String> urls, String username) {
        // Validate owner & limit (reuse from other method)
        conversationService.validateUserOwnership(request.getConversationId(), username);
        Conversation conversation = conversationService.getConversationById(request.getConversationId());
        long todayQuestions = questionRepository.countQuestionsByUserIdAndCreatedAtAfter(
                conversation.getUser().getId(),
                LocalDateTime.now().minusDays(1)
        );
        if (todayQuestions >= questionLimitPerDay) {
            throw new RuntimeException("Daily question limit reached: " + questionLimitPerDay);
        }
        // Create question
        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setResponseStyle(request.getResponseStyle());
        question.setAiRole(request.getAiRole());
        question.setConversation(conversation);
        question = questionRepository.save(question);
        log.info("Question (with files+urls) created for conversation: {}", request.getConversationId());

        List<Question> recentQuestions = getRecentQuestionsForContext(request.getConversationId());

        StringBuilder extracted = new StringBuilder();
        // Files
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String ct = file.getContentType() != null ? file.getContentType() : "";
                String filename = file.getOriginalFilename();
                String lower = filename != null ? filename.toLowerCase() : "";
                try (InputStream in = file.getInputStream()) {
                    if (ct.equalsIgnoreCase("application/pdf") || lower.endsWith(".pdf")) {
                        extracted.append(extractPdfText(in)).append("\n\n");
                    } else if (ct.startsWith("image/") || lower.matches(".*\\.(jpg|jpeg|png|bmp|tiff)$")) {
                        extracted.append(extractImageTextSafe(in, filename));
                    } else {
                        extracted.append("[Fișier atașat: ").append(filename).append("]\n");
                    }
                } catch (IOException e) {
                    log.warn("Failed to read file {}: {}", filename, e.getMessage());
                }
            }
        }
        // URLs
        if (urls != null) {
            for (String url : urls) {
                String text = fetchUrlTextSafe(url);
                if (!text.isBlank()) {
                    extracted.append("[Sursă: ").append(url).append("]\n");
                    extracted.append(text).append("\n\n");
                } else {
                    extracted.append("[Link menționat dar conținutul nu a putut fi extras: ").append(url).append("]\n");
                }

                // Analyze link usefulness for the conversation's subject and append short summary
                try {
                    String subjectName = conversation.getTopic() != null ? conversation.getTopic().getDisplayName() : "General";
                    LinkAnalysis la = aiService.analyzeLinkForSubject(url, subjectName);

                    extracted.append("[Analiză link] Utilitate: ").append(la.getUsefulness())
                            .append(" | Scor: ").append(la.getScore()).append("\n")
                            .append("Motiv: ").append(la.getReason()).append("\n");

                    if (la.getHighlights() != null && !la.getHighlights().isEmpty()) {
                        extracted.append("Highlights:\n");
                        for (String h : la.getHighlights()) {
                            extracted.append("- ").append(h).append("\n");
                        }
                    }
                    extracted.append("\n");
                } catch (Exception e) {
                    log.warn("Link analysis failed for {}: {}", url, e.getMessage());
                }
            }
        }

        String aiResponseText = aiService.generateResponseWithContext(question, recentQuestions, extracted.toString());
        AIResponse aiResponse = aiResponseService.createResponse(question, aiResponseText);
        question.setAiResponse(aiResponse);
        return question;
    }

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
                // crude text extraction: remove scripts/styles/tags
                html = html.replaceAll("(?is)<script[^>]*>.*?</script>", " ")
                           .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
                           .replaceAll("(?is)<[^>]+>", " ")
                           .replaceAll("&nbsp;", " ");
                String text = html.replaceAll("\\s+", " ").trim();
                if (text.length() > 4000) text = text.substring(0, 4000) + "\n... (text trunchiat)";
                return text;
            }
            return "";
        } catch (Exception e) {
            log.warn("URL fetch failed for {}: {}", url, e.getMessage());
            return "";
        }
    }

    public List<Question> getConversationQuestions(Long conversationId) {
        return questionRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public int countUserQuestions(String username) {
        // Count questions across all user's conversations
        List<Conversation> convs = conversationService.getUserConversations(username);
        int total = 0;
        for (Conversation c : convs) {
            List<Question> qs = questionRepository.findByConversationIdOrderByCreatedAtAsc(c.getId());
            total += qs != null ? qs.size() : 0;
        }
        return total;
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found: " + id));
    }

    private List<Question> getRecentQuestionsForContext(Long conversationId) {
        List<Question> allQuestions = questionRepository.findLastQuestionsInConversation(conversationId);
        // Return last 5 questions for context
        return allQuestions.stream()
                .limit(5)
                .toList();
    }

    @Transactional
    public void addFeedback(Long questionId, FeedbackRequest request, String username) {
        Question question = getQuestionById(questionId);
        conversationService.validateUserOwnership(question.getConversation().getId(), username);

        aiResponseService.addFeedback(question.getId(), request.getRating(), request.getComment());
        log.info("Feedback added for question: {}", questionId);
    }
}
