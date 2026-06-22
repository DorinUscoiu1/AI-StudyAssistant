package org.example.ai_studyassistant.repository;

import org.example.ai_studyassistant.model.Conversation;
import org.example.ai_studyassistant.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.aiResponse WHERE q.conversation.id = :conversationId ORDER BY q.createdAt ASC")
    List<Question> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.conversation.user.id = :userId AND q.createdAt >= :startDate")
    long countQuestionsByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.aiResponse WHERE q.conversation.id = :conversationId ORDER BY q.createdAt DESC")
    List<Question> findLastQuestionsInConversation(@Param("conversationId") Long conversationId);
}

