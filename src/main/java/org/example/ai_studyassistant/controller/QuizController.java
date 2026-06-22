package org.example.ai_studyassistant.controller;

import lombok.RequiredArgsConstructor;
import org.example.ai_studyassistant.model.Quiz;
import org.example.ai_studyassistant.model.Topic;
import org.example.ai_studyassistant.service.QuizService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public String list(Authentication auth, Model model) {
        List<Quiz> quizzes = quizService.listUserQuizzes(auth.getName());
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("topics", Topic.values());
        return "quiz_list";
    }

    @PostMapping("/generate")
    public String generate(Authentication auth, @RequestParam("topic") Topic topic,
                           @RequestParam(name = "count", defaultValue = "5") int count) {
        quizService.generateQuiz(auth.getName(), topic, Math.max(3, Math.min(count, 15)));
        return "redirect:/quiz";
    }

    @GetMapping("/{id}")
    public String take(Authentication auth, @PathVariable Long id, Model model) {
        try {
            if (quizService.hasAttempt(auth.getName(), id)) {
                return "redirect:/quiz?error=Ai%20completat%20deja%20acest%20quiz";
            }
            Quiz quiz = quizService.get(id, auth.getName());
            model.addAttribute("quiz", quiz);
            return "quiz_take";
        } catch (RuntimeException e) {
            return "redirect:/quiz?error=" + e.getMessage();
        }
    }

    @PostMapping("/{id}/submit")
    public String submit(Authentication auth, @PathVariable Long id, @RequestParam Map<String, String> params, Model model) {
        try {

            Map<Long, String> answers = new HashMap<>();
            for (Map.Entry<String, String> e : params.entrySet()) {
                if (e.getKey().startsWith("q")) {
                    try {
                        Long qid = Long.parseLong(e.getKey().substring(1));
                        answers.put(qid, e.getValue());
                    } catch (NumberFormatException ignored) {}
                }
            }
            var attempt = quizService.submitAttempt(auth.getName(), id, answers);
            model.addAttribute("attempt", attempt);
            model.addAttribute("quiz", quizService.get(id, auth.getName()));
            return "quiz_result";
        } catch (RuntimeException e) {
            return "redirect:/quiz?error=" + e.getMessage();
        }
    }
}
