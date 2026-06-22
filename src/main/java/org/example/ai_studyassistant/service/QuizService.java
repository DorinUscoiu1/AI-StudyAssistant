package org.example.ai_studyassistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_studyassistant.model.*;
import org.example.ai_studyassistant.repository.QuizAttemptRepository;
import org.example.ai_studyassistant.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final SqsService sqsService;
    private final QuizAttemptRepository attemptRepository;
    private final UserService userService;
    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern NUMBERING_PREFIX = Pattern.compile("^\\s*\\d+\\s*[).:\\-]\\s*");

    @Transactional
    public Quiz generateQuiz(String username, Topic topic, int count) {
        User user = userService.findByUsername(username);
        sqsService.sendQuizEvent(username, topic.getDisplayName(), count);
        // 1. Construim promptul pentru AI
        String prompt = "Generează în limba română un quiz de exact " + count +
                " întrebări (nivel liceu) pentru subiectul: '" + topic.getDisplayName() + "'. " +
                "Pentru fiecare întrebare: 1 răspuns corect și 3 răspunsuri greșite (opțiuni A, B, C, D). " +
                "Condiții: întrebări clare, fără ambiguități; variante plauzibile; NU adăuga explicații/justificări; " +
                "NU numerota în textul întrebării; răspunsul corect indicat doar ca literă (A/B/C/D). " +
                "Returnează DOAR JSON cu schema: {\"title\":\"...\", \"questions\":[{\"q\":\"...\",\"a\":\"...\",\"b\":\"...\",\"c\":\"...\",\"d\":\"...\",\"correct\":\"A|B|C|D\"}]}";

        Conversation conv = new Conversation();
        conv.setTopic(topic);
        Question fakeQ = new Question();
        fakeQ.setQuestionText(prompt);
        fakeQ.setAiRole(AIRole.MENTOR);
        fakeQ.setResponseStyle(ResponseStyle.SIMPLE);
        fakeQ.setConversation(conv);

        // 2. Apelăm AI-ul și obținem JSON-ul brut
        String aiText = aiService.generateResponse(fakeQ, List.of());
        String json = extractJson(aiText);

        Quiz quiz = new Quiz();
        quiz.setUser(user);
        quiz.setTopic(topic);
        quiz.setTitle("Quiz: " + topic.getDisplayName());

        List<QuizQuestion> qqList = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.hasNonNull("title")) {
                quiz.setTitle(root.get("title").asText());
            }
            JsonNode arr = root.path("questions");
            if (arr.isArray()) {
                int added = 0;
                for (JsonNode qn : arr) {
                    if (added >= count) break;

                    String qText = sanitizeQuestion(qn.path("q").asText("Întrebare"));

                    // --- LOGICA DE SHUFFLE (AMESTECARE) ---

                    // 1. Luăm opțiunile brute de la AI
                    String rawA = qn.path("a").asText("A");
                    String rawB = qn.path("b").asText("B");
                    String rawC = qn.path("c").asText("C");
                    String rawD = qn.path("d").asText("D");
                    String rawCorrectLetter = sanitizeCorrect(qn.path("correct").asText("A"));

                    // 2. Determinăm care este textul efectiv al răspunsului corect
                    String correctAnswerText = rawA; // Default
                    if ("B".equals(rawCorrectLetter)) correctAnswerText = rawB;
                    else if ("C".equals(rawCorrectLetter)) correctAnswerText = rawC;
                    else if ("D".equals(rawCorrectLetter)) correctAnswerText = rawD;

                    // 3. Punem variantele într-o listă și le amestecăm
                    List<String> options = new ArrayList<>(Arrays.asList(rawA, rawB, rawC, rawD));
                    Collections.shuffle(options);

                    QuizQuestion qq = new QuizQuestion();
                    qq.setQuiz(quiz);
                    qq.setQuestionText(qText);

                    // 4. Setăm opțiunile în ordinea nouă (amestecată)
                    qq.setOptionA(options.get(0));
                    qq.setOptionB(options.get(1));
                    qq.setOptionC(options.get(2));
                    qq.setOptionD(options.get(3));

                    // 5. Recalculăm litera corectă bazată pe unde a ajuns textul corect
                    if (options.get(0).equals(correctAnswerText)) qq.setCorrectOption("A");
                    else if (options.get(1).equals(correctAnswerText)) qq.setCorrectOption("B");
                    else if (options.get(2).equals(correctAnswerText)) qq.setCorrectOption("C");
                    else qq.setCorrectOption("D");

                    // --- END SHUFFLE LOGIC ---

                    qqList.add(qq);
                    added++;
                }
            }
        } catch (Exception e) {
            log.warn("AI quiz parse failed, using fallback: {}", e.getMessage());
            // Fallback rămâne neschimbat, deoarece acolo folosim Math.random() oricum
        }

        // 3. Fallback: Dacă AI-ul a eșuat sau a dat mai puține întrebări, completăm cu dummy data
        for (int i = qqList.size() + 1; i <= count; i++) {
            QuizQuestion qq = new QuizQuestion();
            qq.setQuiz(quiz);
            qq.setQuestionText("Întrebare #" + i + " despre " + topic.getDisplayName() + " (Generată automat)");
            qq.setOptionA("Varianta A");
            qq.setOptionB("Varianta B");
            qq.setOptionC("Varianta C");
            qq.setOptionD("Varianta D");

            String[] options = {"A", "B", "C", "D"};
            String correctOption = options[(int) (Math.random() * options.length)];
            qq.setCorrectOption(correctOption);

            qqList.add(qq);
        }

        quiz.setQuestions(qqList);
        return quizRepository.save(quiz);
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        String t = text.trim();
        // strip markdown code fences
        if (t.startsWith("```") && t.endsWith("```")) {
            t = t.substring(3, t.length() - 3).trim();
        }
        // try to locate the first '{' and last '}' to isolate JSON
        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return t.substring(start, end + 1);
        }
        return t;
    }

    private String sanitizeQuestion(String q) {
        if (q == null) return "Întrebare";
        String s = NUMBERING_PREFIX.matcher(q).replaceFirst("");
        return s.trim();
    }

    private String sanitizeCorrect(String letter) {
        if (letter == null) return "A";
        String l = letter.trim().toUpperCase();
        return (l.equals("A") || l.equals("B") || l.equals("C") || l.equals("D")) ? l : "A";
    }

    public List<Quiz> listUserQuizzes(String username) {
        return quizRepository.findByUserOrderByCreatedAtDesc(userService.findByUsername(username));
    }

    public Quiz get(Long id, String username) {
        Quiz q = quizRepository.findById(id).orElseThrow(() -> new RuntimeException("Quiz not found"));
        if (!q.getUser().getUsername().equals(username)) throw new RuntimeException("Access denied");
        return q;
    }

    public boolean hasAttempt(String username, Long quizId) {
        User user = userService.findByUsername(username);
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("Quiz not found"));
        return attemptRepository.findByQuizAndUser(quiz, user).isPresent();
    }

    public QuizAttempt submitAttempt(String username, Long quizId, java.util.Map<Long, String> answers) {
        User user = userService.findByUsername(username);
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("Quiz not found"));
        if (!quiz.getUser().getUsername().equals(username)) throw new RuntimeException("Access denied");
        if (attemptRepository.findByQuizAndUser(quiz, user).isPresent()) {
            throw new RuntimeException("Ai completat deja acest quiz");
        }
        int total = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        int score = 0;
        if (quiz.getQuestions() != null) {
            for (var q : quiz.getQuestions()) {
                String expected = q.getCorrectOption() != null ? q.getCorrectOption().trim().toUpperCase() : "A";
                String selected = answers.getOrDefault(q.getId(), "");
                if (selected != null && expected.equalsIgnoreCase(selected.trim())) {
                    score++;
                }
            }
        }
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setScore(score);
        attempt.setTotal(total);
        return attemptRepository.save(attempt);
    }
}
