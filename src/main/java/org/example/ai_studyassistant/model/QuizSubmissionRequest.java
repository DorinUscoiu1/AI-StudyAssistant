package org.example.ai_studyassistant.model;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class QuizSubmissionRequest {

    @NotNull(message = "Quiz ID cannot be null")
    private Long quizId;

    @NotNull(message = "Answers cannot be null")
    private Map<Long, String> answers;

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }
}
