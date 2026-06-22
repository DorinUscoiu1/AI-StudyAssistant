package org.example.ai_studyassistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_studyassistant.model.AIResponse;
import org.example.ai_studyassistant.model.Question;
import org.example.ai_studyassistant.repository.AIResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIResponseService {

    private final AIResponseRepository aiResponseRepository;

    @Transactional
    public AIResponse createResponse(Question question, String responseText) {
        AIResponse aiResponse = new AIResponse();
        aiResponse.setQuestion(question);
        aiResponse.setResponseText(responseText);

        log.info("Creating AI response for question: {}", question.getId());
        return aiResponseRepository.save(aiResponse);
    }

    public AIResponse getResponseByQuestionId(Long questionId) {
        return aiResponseRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new RuntimeException("AI response not found for question: " + questionId));
    }

    @Transactional
    public void addFeedback(Long questionId, Integer rating, String comment) {
        AIResponse response = getResponseByQuestionId(questionId);
        response.setFeedbackRating(rating);
        response.setFeedbackComment(comment);
        aiResponseRepository.save(response);

        log.info("Feedback added to response for question: {}, rating: {}", questionId, rating);
    }
}

