package org.example.ai_studyassistant.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class LinkAnalysisRequest {

    @NotBlank(message = "URL cannot be blank")
    @Pattern(regexp = "^(https?://).+", message = "Invalid URL format")
    private String url;

    @NotNull(message = "Topic cannot be null")
    private Topic topic;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }
}
