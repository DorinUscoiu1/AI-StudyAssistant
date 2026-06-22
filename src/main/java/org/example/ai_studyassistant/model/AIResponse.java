package org.example.ai_studyassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @NotBlank(message = "Response text is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String responseText;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Min(value = 1, message = "Feedback rating must be at least 1")
    @Max(value = 5, message = "Feedback rating must be at most 5")
    private Integer feedbackRating;

    @Size(max = 500, message = "Feedback comment must be at most 500 characters")
    @Column(columnDefinition = "TEXT")
    private String feedbackComment;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
