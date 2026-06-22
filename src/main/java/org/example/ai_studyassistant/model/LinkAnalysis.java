package org.example.ai_studyassistant.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkAnalysis {
    private String usefulness;
    private int score;
    private String reason;
    private List<String> highlights;

    public LinkAnalysis(String usefulness, String reason, int score) {
        this.usefulness = usefulness;
        this.reason = reason;
        this.score = score;
    }
}

