package org.example.ai_studyassistant.controller;

import org.example.ai_studyassistant.service.ConversationService;
import org.example.ai_studyassistant.service.UserService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final UserService userService;
    private final ConversationService conversationService;

    public AdminController(UserService userService, ConversationService conversationService) {
        this.userService = userService;
        this.conversationService = conversationService;
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("conversations", conversationService.getAllConversations());
        return "admin-dashboard";
    }
}
