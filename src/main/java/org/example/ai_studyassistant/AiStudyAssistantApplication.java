package org.example.ai_studyassistant;

import org.example.ai_studyassistant.mcp.StudyAssistantMcpTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class AiStudyAssistantApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "mcp");
        SpringApplication.run(AiStudyAssistantApplication.class, args);
    }

    @Bean
    public List<ToolCallback> toolCallbacks(
            @Lazy StudyAssistantMcpTools studyAssistantMcpTools
    ) {
        return List.of(
                ToolCallbacks.from(studyAssistantMcpTools)
        );
    }

}
