package org.example.ai_studyassistant.repository;

import org.example.ai_studyassistant.model.Quiz;
import org.example.ai_studyassistant.model.QuizAttempt;
import org.example.ai_studyassistant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    Optional<QuizAttempt> findByQuizAndUser(Quiz quiz, User user);
}

