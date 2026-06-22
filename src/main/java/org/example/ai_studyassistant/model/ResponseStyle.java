package org.example.ai_studyassistant.model;

public enum ResponseStyle {
    SIMPLE("Explică simplu (pentru începători)"),
    DETAILED("Detaliat și cuprinzător"),
    QUICK("Rapid și concis"),
    ACADEMIC("Academic și formal");

    private final String displayName;

    ResponseStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSystemPrompt() {
        switch (this) {
            case SIMPLE:
                return "Explică conceptele în mod simplu și accesibil, ca și cum ai vorbi cu un începător. " +
                       "Folosește exemple practice și analogii ușor de înțeles. Evită jargonul tehnic complex.";
            case DETAILED:
                return "Oferă un răspuns detaliat și cuprinzător. Include explicații aprofundate, exemple de cod, " +
                       "cazuri de utilizare și best practices. Acoperă și aspectele avansate ale subiectului.";
            case QUICK:
                return "Oferă un răspuns rapid, concis și la obiect. Concentrează-te pe esențial fără detalii " +
                       "suplimentare. Folosește bullet points când este posibil.";
            case ACADEMIC:
                return "Răspunde în stil academic și formal. Include terminologie tehnică precisă, " +
                       "referințe la concepte teoretice și explicații riguroase. Structurează răspunsul logic.";
            default:
                return "Răspunde clar și structurat la întrebare.";
        }
    }
}

