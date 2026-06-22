package org.example.ai_studyassistant.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.ai_studyassistant.dto.*;
import org.example.ai_studyassistant.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testEmailValidation_Invalid() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email")));
    }

    @Test
    void testEmailValidation_Valid() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testPasswordValidation_TooShort() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("pass");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("8 characters")));
    }
    @Test
    void testUserRoleValidation_InvalidRole() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Role is required")));
    }

    @Test
    void testUserRoleValidation_ValidRole() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(UserRole.ADMIN);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testUsernameValidation_Null() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(null);
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username")));
    }

    @Test
    void testQuestionValidation_TooShort() {
        QuestionRequest request = new QuestionRequest();
        request.setQuestionText("Hi");
        request.setResponseStyle(ResponseStyle.SIMPLE);
        request.setAiRole(AIRole.MENTOR);
        request.setConversationId(1L);

        Set<ConstraintViolation<QuestionRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("between 5 and 1000")));
    }

    @Test
    void testQuestionValidation_Valid() {
        QuestionRequest request = new QuestionRequest();
        request.setQuestionText("What is object-oriented programming?");
        request.setResponseStyle(ResponseStyle.SIMPLE);
        request.setAiRole(AIRole.MENTOR);
        request.setConversationId(1L);

        Set<ConstraintViolation<QuestionRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }


    @Test
    void testAIRoleValidation_NotNull() {
        QuestionRequest request = new QuestionRequest();
        request.setQuestionText("Ce este biologia?");
        request.setResponseStyle(ResponseStyle.SIMPLE);
        request.setAiRole(null);
        request.setConversationId(1L);

        Set<ConstraintViolation<QuestionRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("AI role")));
    }

    @Test
    void testTopicValidation_NotNull() {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setTitle("Test Conversation");
        request.setTopic(null);

        Set<ConstraintViolation<ConversationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Topic")));
    }

    @Test
    void testFeedbackRatingValidation_TooLow() {
        FeedbackRequest request = new FeedbackRequest();
        request.setRating(0);
        request.setComment("Good");

        Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("at least 1")));
    }

    @Test
    void testFeedbackRatingValidation_TooHigh() {
        FeedbackRequest request = new FeedbackRequest();
        request.setRating(6);
        request.setComment("Good");

        Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("at most 5")));
    }

    @Test
    void testConversationTitleValidation_Empty() {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setTitle("");
        request.setTopic(Topic.GENERAL);

        Set<ConstraintViolation<ConversationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Title")));
    }

    @Test
    void testLinkAnalysis_ValidLink() {
        LinkAnalysisRequest request = new LinkAnalysisRequest();
        request.setUrl("https://example.com/valid-topic");
        request.setTopic(Topic.BIOLOGIA);

        Set<ConstraintViolation<LinkAnalysisRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
    @Test
    void testConversationValidation_MissingTitleAndTopic() {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setTitle(null);
        request.setTopic(null);

        Set<ConstraintViolation<ConversationCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Title")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Topic")));
    }

    @Test
    void testLinkAnalysis_InvalidLink() {
        LinkAnalysisRequest request = new LinkAnalysisRequest();
        request.setUrl("invalid-url");
        request.setTopic(Topic.BIOLOGIA);

        Set<ConstraintViolation<LinkAnalysisRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("URL")));
    }

    @Test
    void testQuizSubmission_Valid() {
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setQuizId(1L);
        request.setAnswers(Map.of(1L, "A", 2L, "B", 3L, "C"));

        Set<ConstraintViolation<QuizSubmissionRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
    @Test
    void testLinkAnalysis_MissingTopic() {
        LinkAnalysisRequest request = new LinkAnalysisRequest();
        request.setUrl("https://example.com");
        request.setTopic(null);

        Set<ConstraintViolation<LinkAnalysisRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Topic cannot be null")));
    }


    @Test
    void testQuizSubmission_MissingAnswers() {
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setQuizId(1L);
        request.setAnswers(null);

        Set<ConstraintViolation<QuizSubmissionRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Answers")));
    }
}
