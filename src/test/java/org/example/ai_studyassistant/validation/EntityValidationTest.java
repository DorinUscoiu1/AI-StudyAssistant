package org.example.ai_studyassistant.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.ai_studyassistant.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testUser_ValidUser() {
        User user = new User("testuser", "password123", "STUDENT");
        user.setEmail("dorin@gmail.com");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid user should not produce violations");
    }

    @Test
    void testUser_NullUsername() {
        User user = new User(null, "password123", "STUDENT");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Null username should produce violations");
    }

    @Test
    void testQuizQuestion_NullQuestionText() {
        QuizQuestion question = new QuizQuestion(null, null, null, "A", "B", "C", "D", "A");
        Set<ConstraintViolation<QuizQuestion>> violations = validator.validate(question);
        assertFalse(violations.isEmpty(), "Null question text should produce violations");
    }

    @Test
    void testQuizAttempt_NegativeScore() {
        QuizAttempt attempt = new QuizAttempt(null, null, null, -1, 10, LocalDateTime.now());
        Set<ConstraintViolation<QuizAttempt>> violations = validator.validate(attempt);
        assertFalse(violations.isEmpty(), "Negative score should produce violations");
    }

    @Test
    void testQuiz_NullTitle() {
        Quiz quiz = new Quiz(null, null, null, null, null, null);
        Set<ConstraintViolation<Quiz>> violations = validator.validate(quiz);
        assertFalse(violations.isEmpty(), "Null title should produce violations");
    }

    @Test
    void testQuestion_NullQuestionText() {
        Question question = new Question(null, null, null, ResponseStyle.SIMPLE, AIRole.TUTOR, null, null, false);
        Set<ConstraintViolation<Question>> violations = validator.validate(question);
        assertFalse(violations.isEmpty(), "Null question text should produce violations");
    }

    @Test
    void testConversation_NullTitle() {
        Conversation conversation = new Conversation(null, null, null, null, null, null, null);
        Set<ConstraintViolation<Conversation>> violations = validator.validate(conversation);
        assertFalse(violations.isEmpty(), "Null title should produce violations");
    }

    @Test
    void testAIResponse_NullResponseText() {
        AIResponse response = new AIResponse(null, null, null, null, null, null);
        Set<ConstraintViolation<AIResponse>> violations = validator.validate(response);
        assertFalse(violations.isEmpty(), "Null response text should produce violations");
    }

    @Test
    void testAIResponse_InvalidFeedbackRating() {
        AIResponse response = new AIResponse(null, null, "Valid response", LocalDateTime.now(), 6, "Good");
        Set<ConstraintViolation<AIResponse>> violations = validator.validate(response);
        assertFalse(violations.isEmpty(), "Feedback rating out of range should produce violations");
    }
}
