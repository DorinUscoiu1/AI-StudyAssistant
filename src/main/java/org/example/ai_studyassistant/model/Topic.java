package org.example.ai_studyassistant.model;

public enum Topic {
    GENERAL("General"),
    MATEMATICA("Matematică"),
    INFORMATICA("Informatică"),
    FIZICA("Fizică"),
    CHIMIA("Chimie"),
    BIOLOGIA("Biologie"),
    ISTORIA("Istorie"),
    GEOGRAFIA("Geografie"),
    LOGICA("Logică"),
    LITERATURA_ROMANA("Literatură"),
    ENGLEZA("Engleză"),
    GERMANA("Germană"),
    FRANCEZA("Franceză"),
    RELIGIE("Religie"),
    PSIHOLOGIE("Psihologie"),
    ECONOMIE("Economie"),
    GRAMATICA("Gramatică");

    private final String displayName;

    Topic(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Short, topic-specific instruction that will be injected into the AI system prompt
     * so that answers are tailored to the selected subject (materie).
     */
    public String getInstruction() {
        switch (this) {
            case MATEMATICA:
                return "Concentrează-te pe concepte matematice, formule și demonstrații pas-cu-pas; oferă exemple și exerciții rezolvate.";
            case INFORMATICA:
                return "Focalizează-te pe algoritmi, pseudo-cod, explicații clare despre complexitate și exemple de cod acolo unde este util.";
            case FIZICA:
                return "Explică principiile fizice, deducții matematice când e cazul, și oferă exemple aplicate sau probleme rezolvate.";
            case CHIMIA:
                return "Oferă explicații despre reacții, mecanisme, formule chimice și exemple practice sau experimente relevante.";
            case BIOLOGIA:
                return "Explică concepte biologice, structuri și procese; folosește exemple, diagrame sugestive și analogii simple.";
            case ISTORIA:
                return "Furnizează context istoric, cronologie, cauze și consecințe; citează evenimente și perioade relevante.";
            case GEOGRAFIA:
                return "Oferă explicații despre procese geografice, hărți, climă și exemple regionale concrete.";
            case LOGICA:
                return "Explică raționamente logice, demonstrații formale și exemple practice de aplicare.";
            case LITERATURA_ROMANA:
                return "Analizează texte literare, oferă interpretări, teme, motive și exemple din opere reprezentative.";
            case ENGLEZA:
                return "Concentrează-te pe gramatică, vocabular, exemple de utilizare și exerciții de traducere sau exprimare.";
            case GERMANA:
                return "Concentrează-te pe gramatică germană, structuri, expresii uzuale și exemple practice.";
            case FRANCEZA:
                return "Concentrează-te pe gramatică franceză, expresii și exemple de utilizare în contexte reale.";
            case RELIGIE:
                return "Oferă explicații despre doctrine, texte sacre și context istoric/etic, respectând diversitatea confesiunilor.";
            case PSIHOLOGIE:
                return "Explică teorii psihologice, concepte-cheie și aplicații practice, cu exemple și referințe când sunt utile.";
            case ECONOMIE:
                return "Furnizează explicații asupra conceptelor economice, modele, grafice și exemple aplicate la economia reală.";
            case GRAMATICA:
                return "Corectează și explică greșelile gramaticale, oferă reguli și exemple de reformulare.";
            case GENERAL:
            default:
                return "Răspunde clar și concis, oferind exemple practice când este cazul; adaptează tonul la nivelul elevului.";
        }
    }
}
