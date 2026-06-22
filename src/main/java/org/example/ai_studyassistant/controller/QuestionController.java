package org.example.ai_studyassistant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_studyassistant.agent.StudyAssistantAgent;
import org.example.ai_studyassistant.dto.FeedbackRequest;
import org.example.ai_studyassistant.dto.QuestionRequest;
import org.example.ai_studyassistant.model.AIRole;
import org.example.ai_studyassistant.model.Question;
import org.example.ai_studyassistant.model.ResponseStyle;
import org.example.ai_studyassistant.service.QuestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final StudyAssistantAgent studyAssistantAgent;
    @PostMapping
    public ResponseEntity<?> askQuestion(
            @Valid @RequestBody QuestionRequest request,
            BindingResult result,
            Authentication authentication) {

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
//            Question question = questionService.askQuestion(request, authentication.getName());
//
//            String responseText = (question.getAiResponse() != null && question.getAiResponse().getResponseText() != null)
//                    ? question.getAiResponse().getResponseText()
//                    : "Ne cerem scuze, dar nu am putut genera un răspuns acum. Te rugăm să încerci din nou în câteva momente.";
//
//            return ResponseEntity.ok(Map.of(
//                "message", "Question answered successfully",
//                "questionId", question.getId(),
//                "response", responseText
//            ));

//            String agentResponse = studyAssistantAgent.run(authentication.getName(), request.getQuestionText());
//
//            return ResponseEntity.ok(Map.of(
//                    "message", "Agent processed request",
//                    "response", agentResponse
//            ));


            String agentResponse = studyAssistantAgent.run(authentication.getName(), request.getQuestionText());

            Question question =
                    questionService.saveQuestionWithResponse(request, agentResponse, authentication.getName());

            return ResponseEntity.ok(Map.of(
                    "questionId", question.getId(),
                    "response", agentResponse
            ));


        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> addFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request,
            BindingResult result,
            Authentication authentication) {

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            questionService.addFeedback(id, request, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Feedback added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

//    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> askQuestionWithFiles(
//            @RequestParam("questionText") String questionText,
//            @RequestParam("responseStyle") String responseStyle,
//            @RequestParam("aiRole") String aiRole,
//            @RequestParam("conversationId") Long conversationId,
//            @RequestParam(value = "urls", required = false) String urls,
//            @RequestPart(value = "files", required = false) MultipartFile[] files,
//            Authentication authentication) {
//        try {
//            QuestionRequest req = new QuestionRequest();
//            req.setQuestionText(questionText);
//            req.setResponseStyle(ResponseStyle.valueOf(responseStyle));
//            req.setAiRole(AIRole.valueOf(aiRole));
//            req.setConversationId(conversationId);
//
//            List<String> urlList = (urls == null || urls.isBlank()) ? List.of() : Arrays.stream(urls.split(","))
//                    .map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .toList();
//
//            Question question = questionService.askQuestionWithFilesAndUrls(req, files, urlList, authentication.getName());
//
//            String responseText = (question.getAiResponse() != null && question.getAiResponse().getResponseText() != null)
//                    ? question.getAiResponse().getResponseText()
//                    : "Ne cerem scuze, dar nu am putut genera un răspuns acum. Te rugăm să încerci din nou în câteva momente.";
//
//            return ResponseEntity.ok(Map.of(
//                    "message", "Question answered successfully",
//                    "questionId", question.getId(),
//                    "response", responseText
//            ));
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
}
