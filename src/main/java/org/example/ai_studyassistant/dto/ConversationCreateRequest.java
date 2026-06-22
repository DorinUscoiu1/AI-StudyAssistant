package org.example.ai_studyassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.ai_studyassistant.model.Topic;

@Data
public class ConversationCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    @NotNull(message = "Topic is required")
    private Topic topic;
}

