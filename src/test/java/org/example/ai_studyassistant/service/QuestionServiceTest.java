package org.example.ai_studyassistant.service;

import org.example.ai_studyassistant.dto.FeedbackRequest;
import org.example.ai_studyassistant.dto.QuestionRequest;
import org.example.ai_studyassistant.model.*;
import org.example.ai_studyassistant.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private AIService aiService;

    @Mock
    private AIResponseService aiResponseService;

    @InjectMocks
    private QuestionService questionService;

    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setTopic(Topic.GENERAL);

        ReflectionTestUtils.setField(questionService, "questionLimitPerDay", 50);
    }
    @Test
    void testGetConversationQuestions() {
        Question q1 = new Question();
        Question q2 = new Question();
        when(questionRepository.findByConversationIdOrderByCreatedAtAsc(1L)).thenReturn(Arrays.asList(q1, q2));
        List<Question> questions = questionService.getConversationQuestions(1L);
        assertEquals(2, questions.size());
    }

    @Test
    void testGetQuestionById_Success() {
        Question question = new Question();
        question.setId(1L);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        Question found = questionService.getQuestionById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetQuestionById_NotFound() {
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> questionService.getQuestionById(1L));
    }

    @Test
    void testQuizCompletionStatus() {
        Question question1 = new Question();
        question1.setCorrect(true);
        Question question2 = new Question();
        question2.setCorrect(false);
        when(questionRepository.findByConversationIdOrderByCreatedAtAsc(1L)).thenReturn(Arrays.asList(question1, question2));
        List<Question> questions = questionService.getConversationQuestions(1L);
        long correctAnswers = questions.stream().filter(Question::isCorrect).count();
        assertEquals(2, questions.size());
        assertEquals(1, correctAnswers);
    }
}
