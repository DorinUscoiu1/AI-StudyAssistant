package org.example.ai_studyassistant.service;

import org.example.ai_studyassistant.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private ChatModel chatModel;

    @InjectMocks
    private AIService aiService;

    private Question testQuestion;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation();
        testConversation.setId(1L);
        testConversation.setTopic(Topic.BIOLOGIA);

        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setQuestionText("What is photosynthesis?");
        testQuestion.setResponseStyle(ResponseStyle.SIMPLE);
        testQuestion.setAiRole(AIRole.MENTOR);
        testQuestion.setConversation(testConversation);
    }

    @Test
    void testGenerateResponse_WithContext() {
        Question prevQuestion = new Question();
        prevQuestion.setQuestionText("What is the role of chlorophyll?");
        AIResponse prevResponse = new AIResponse();
        prevResponse.setResponseText("Chlorophyll captures sunlight for photosynthesis.");
        prevQuestion.setAiResponse(prevResponse);

        List<Question> recentQuestions = Arrays.asList(prevQuestion);

        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage message = new AssistantMessage("Photosynthesis involves chlorophyll capturing sunlight to produce glucose.");
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(message);
        String response = aiService.generateResponse(testQuestion, recentQuestions);
        assertNotNull(response);
        assertTrue(response.contains("Photosynthesis"));
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    void testGenerateResponse_ErrorHandling() {
        List<Question> recentQuestions = new ArrayList<>();
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("API Error"));
        String response = aiService.generateResponse(testQuestion, recentQuestions);
        assertNotNull(response);
        assertTrue(response.contains("eroare"));
    }
}
