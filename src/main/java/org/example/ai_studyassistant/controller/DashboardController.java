package org.example.ai_studyassistant.controller;

import lombok.RequiredArgsConstructor;
import org.example.ai_studyassistant.model.*;
import org.example.ai_studyassistant.service.ConversationService;
import org.example.ai_studyassistant.service.QuestionService;
import org.example.ai_studyassistant.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ConversationService conversationService;
    private final QuestionService questionService;
    private final UserService userService;

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<Conversation> conversations = conversationService.getUserConversations(username);

        model.addAttribute("username", username);
        model.addAttribute("conversations", conversations);
        model.addAttribute("topics", Topic.values());
        model.addAttribute("responseStyles", ResponseStyle.values());
        model.addAttribute("aiRoles", AIRole.values());

        return "dashboard";
    }

    @GetMapping("/conversation/{id}")
    public String viewConversation(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();

        try {
            conversationService.validateUserOwnership(id, username);
            Conversation conversation = conversationService.getConversationById(id);
            List<Question> questions = questionService.getConversationQuestions(id);

            model.addAttribute("conversation", conversation);
            model.addAttribute("questions", questions);
            model.addAttribute("responseStyles", ResponseStyle.values());
            model.addAttribute("aiRoles", AIRole.values());

            return "conversation";
        } catch (RuntimeException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("conversations", conversationService.getAllConversations());
        return "admin-dashboard";
    }
}
