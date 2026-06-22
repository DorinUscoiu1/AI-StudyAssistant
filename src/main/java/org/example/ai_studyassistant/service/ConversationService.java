package org.example.ai_studyassistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_studyassistant.dto.ConversationCreateRequest;
import org.example.ai_studyassistant.model.Conversation;
import org.example.ai_studyassistant.model.Topic;
import org.example.ai_studyassistant.model.User;
import org.example.ai_studyassistant.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserService userService;
    private final AIService aiService;

    @Transactional
    public Conversation createConversation(ConversationCreateRequest request, String username) {
        User user = userService.findByUsername(username);

        Conversation conversation = new Conversation();
        conversation.setTitle(request.getTitle());
        conversation.setTopic(request.getTopic());
        conversation.setUser(user);

        log.info("Creating new conversation for user: {}, topic: {}", username, request.getTopic());
        return conversationRepository.save(conversation);
    }

    public List<Conversation> getUserConversations(String username) {
        User user = userService.findByUsername(username);
        return conversationRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    public Conversation getConversationById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + id));
    }

    public void validateUserOwnership(Long conversationId, String username) {
        Conversation conversation = getConversationById(conversationId);
        if (!conversation.getUser().getUsername().equals(username)) {
            throw new RuntimeException("User does not own this conversation");
        }
    }

    public void deleteConversation(Long conversationId, String username) {
        Conversation conversation = getConversationById(conversationId);
        if (!conversation.getUser().getUsername().equals(username)) {
            throw new RuntimeException("User does not own this conversation");
        }
        conversationRepository.delete(conversation);
        log.info("Conversation {} deleted by {}", conversationId, username);
    }

    public Topic getCurrentConversationTopic(String username) {
        List<Conversation> conversations = getUserConversations(username);
        if (conversations.isEmpty()) {
            return null;
        }
        return conversations.get(0).getTopic();
    }

    public String analyzeLink(String url, String subject) {
        log.info("Analyzing link: {} with subject: {}", url, subject);
        return conversationRepository.analyzeLink(url, subject);
    }

    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }
}
