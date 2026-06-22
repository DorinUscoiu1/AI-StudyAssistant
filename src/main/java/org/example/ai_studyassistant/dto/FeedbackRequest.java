package org.example.ai_studyassistant.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {

    @Min(value = 1, message = "Feedback rating must be at least 1")
    @Max(value = 5, message = "Feedback rating must be at most 5")
    private Integer rating;

    @Size(max = 500, message = "Feedback comment must be at most 500 characters")
    private String comment;
}

