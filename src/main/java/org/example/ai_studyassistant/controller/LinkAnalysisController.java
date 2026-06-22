package org.example.ai_studyassistant.controller;

import lombok.RequiredArgsConstructor;
import org.example.ai_studyassistant.model.LinkAnalysis;
import org.example.ai_studyassistant.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analysis/link")
@RequiredArgsConstructor
public class LinkAnalysisController {

    private final AIService aiService;

    @GetMapping("/analyze")
    public ResponseEntity<LinkAnalysis> analyzeLink(@RequestParam("url") String url,
                                                    @RequestParam(value = "subject", required = false) String subject) {
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String subj = (subject == null || subject.isBlank()) ? "General" : subject;
        LinkAnalysis result = aiService.analyzeLinkForSubject(url, subj);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/analyze")
    public ResponseEntity<LinkAnalysis> analyzeLinkPost(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        String subject = request.get("subject");

        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String subj = (subject == null || subject.isBlank()) ? "General" : subject;
        LinkAnalysis result = aiService.analyzeLinkForSubject(url, subj);
        return ResponseEntity.ok(result);
    }
}
