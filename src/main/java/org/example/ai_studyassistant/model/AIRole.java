package org.example.ai_studyassistant.model;

public enum AIRole {
    PROFESSOR("Profesor"),
    MENTOR("Mentor"),
    COLLEAGUE("Coleg de studii"),
    TUTOR("Tutor personal");

    private final String displayName;

    AIRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRolePrompt() {
        switch (this) {
            case PROFESSOR:
                return "Ești un profesor universitar experimentat, cu cunoștințe vaste în domeniu. " +
                       "Răspunde cu autoritate și competență, oferind explicații clare și structurate.";
            case MENTOR:
                return "Ești un mentor prietenos și suportiv care ghidează studenții în călătoria lor de învățare. " +
                       "Încurajează gândirea critică și oferă sfaturi practice bazate pe experiență.";
            case COLLEAGUE:
                return "Ești un coleg de studii care ajută la înțelegerea materiei. " +
                       "Comunică într-un mod relaxat și prietenos, folosind un limbaj accesibil și exemple practice.";
            case TUTOR:
                return "Ești un tutor personal dedicat care se adaptează la nevoile studentului. " +
                       "Ești răbdător, verifici înțelegerea și oferi exerciții practice când este necesar.";
            default:
                return "Ești un asistent educațional util.";
        }
    }
}

