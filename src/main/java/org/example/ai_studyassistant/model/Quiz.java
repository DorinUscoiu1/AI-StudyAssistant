package org.example.ai_studyassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Topic topic;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<QuizQuestion> questions = new ArrayList<>();

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }

    public Quiz(String topic, int totalQuestions, int correctAnswers) {
        this.topic = Topic.valueOf(topic);
        this.title = "Quiz on " + topic;
        this.questions = new ArrayList<>();
        for (int i = 0; i < totalQuestions; i++) {
            QuizQuestion question = new QuizQuestion();
            this.questions.add(question);
        }
    }
}
