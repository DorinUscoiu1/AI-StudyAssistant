package org.example.ai_studyassistant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_studyassistant.dto.LoginRequest;
import org.example.ai_studyassistant.dto.RegisterRequest;
import org.example.ai_studyassistant.model.Conversation;
import org.example.ai_studyassistant.model.User;
import org.example.ai_studyassistant.model.UserRole;
import org.example.ai_studyassistant.repository.ConversationRepository;
import org.example.ai_studyassistant.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConversationRepository conversationRepository;

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT);

        log.info("Registering new user: {}", request.getUsername());
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Transactional
    public void updateLastLogin(String username) {
        User user = findByUsername(username);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    public boolean authenticateUser(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            updateLastLogin(user.getUsername());
            return true;
        }

        return false;
    }
}
