package org.example.ai_studyassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.ai_studyassistant.model.AIRole;
import org.example.ai_studyassistant.model.ResponseStyle;

@Data
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    @Size(min = 5, max = 1000, message = "Question must be between 5 and 1000 characters")
    private String questionText;

    @NotNull(message = "Response style is required")
    private ResponseStyle responseStyle;

    @NotNull(message = "AI role is required")
    private AIRole aiRole;

    @NotNull(message = "Conversation ID is required")
    private Long conversationId;
}

