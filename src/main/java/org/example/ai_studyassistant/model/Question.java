package org.example.ai_studyassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question text is required")
    @Size(min = 5, max = 1000, message = "Question must be between 5 and 1000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseStyle responseStyle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AIRole aiRole;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private AIResponse aiResponse;

    @Column(name = "is_correct")
    private boolean isCorrect;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
