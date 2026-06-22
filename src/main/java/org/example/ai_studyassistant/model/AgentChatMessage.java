package org.example.ai_studyassistant.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_chat_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String role; // "USER" or "AGENT"

    @Column(columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public AgentChatMessage(String username, String role, String content) {
        this.username = username;
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}

