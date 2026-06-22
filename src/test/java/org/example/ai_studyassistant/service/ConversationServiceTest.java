package org.example.ai_studyassistant.service;

import org.example.ai_studyassistant.dto.ConversationCreateRequest;
import org.example.ai_studyassistant.model.Conversation;
import org.example.ai_studyassistant.model.Topic;
import org.example.ai_studyassistant.model.User;
import org.example.ai_studyassistant.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ConversationService conversationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    void testCreateConversation_Success() {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setTitle("Test Conversation");
        request.setTopic(Topic.GENERAL);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(i -> {
            Conversation conv = i.getArgument(0);
            conv.setId(1L);
            return conv;
        });
        Conversation conversation = conversationService.createConversation(request, "testuser");
        assertNotNull(conversation);
        assertEquals("Test Conversation", conversation.getTitle());
        assertEquals(Topic.GENERAL, conversation.getTopic());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void testGetUserConversations() {
        Conversation conv1 = new Conversation();
        conv1.setTitle("Conv 1");
        Conversation conv2 = new Conversation();
        conv2.setTitle("Conv 2");
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(conversationRepository.findByUserOrderByUpdatedAtDesc(testUser))
            .thenReturn(Arrays.asList(conv1, conv2));
        List<Conversation> conversations = conversationService.getUserConversations("testuser");
        assertEquals(2, conversations.size());
    }

    @Test
    void testGetConversationById_Success() {
        Conversation conversation = new Conversation();
        conversation.setId(1L);
        conversation.setTitle("Test");
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        Conversation found = conversationService.getConversationById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetConversationById_NotFound() {
        when(conversationRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> conversationService.getConversationById(1L));
    }
    @Test
    void testAnalyzeLink_Success() {
        String url = "http://example.com";
        String subject = "Test Subject";
        String analysisResult = "Analysis successful";
        when(conversationRepository.analyzeLink(url, subject)).thenReturn(analysisResult);
        String result = conversationService.analyzeLink(url, subject);
        assertNotNull(result);
        assertEquals("Analysis successful", result);
    }

    @Test
    void testAnalyzeLink_Failure() {
        String url = "http://example.com";
        String subject = "Test Subject";
        when(conversationRepository.analyzeLink(url, subject)).thenThrow(new RuntimeException("Analysis failed"));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> conversationService.analyzeLink(url, subject));
        assertEquals("Analysis failed", exception.getMessage());
    }
}
