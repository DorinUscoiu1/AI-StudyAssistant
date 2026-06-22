package org.example.ai_studyassistant.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/agent")
public class AgentController {

    private final StudyAssistantAgent agent;

    @PostMapping
    public Map<String, Object> runAgent(
            @RequestParam String username,
            @RequestBody String input
    ) {
        String result = agent.run(username, input);
        return Map.of("result", result);
    }
}
