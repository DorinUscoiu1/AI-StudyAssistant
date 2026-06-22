package org.example.ai_studyassistant.repository;

import org.example.ai_studyassistant.model.Quiz;
import org.example.ai_studyassistant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByUserOrderByCreatedAtDesc(User user);
}

