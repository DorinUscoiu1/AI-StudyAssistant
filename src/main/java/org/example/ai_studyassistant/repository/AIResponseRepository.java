package org.example.ai_studyassistant.repository;

import org.example.ai_studyassistant.model.AIResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIResponseRepository extends JpaRepository<AIResponse, Long> {
    Optional<AIResponse> findByQuestionId(Long questionId);
}

