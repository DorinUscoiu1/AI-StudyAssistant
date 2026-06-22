package org.example.ai_studyassistant.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean
    public ChatClient agentChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                Ești un AI Study Assistant agentic, expert în educație.
                Ai acces la tool-uri pe care trebuie să le folosești pentru a oferi informații exacte.
                
                REGULI DE FORMATARE:
                1. Folosește tabele Markdown pentru a lista întrebări sau statistici.
                2. Folosește liste cu bullet-uri pentru opțiuni.
                3. Folosește bold (între stelute) pentru termeni importanți sau titluri.
                4. Pentru formule matematice sau complexitate (ex: O(n^2)), folosește formatul LaTeX simplu: $O(n^2)$.
                5. Răspunsul trebuie să fie curat, bine structurat și ușor de citit.
                
                Dacă utilizatorul cere detalii despre un quiz, prezintă-le sub formă de tabel cu coloanele: #, Întrebare, Variante.
            """)
                .build();
    }
}
