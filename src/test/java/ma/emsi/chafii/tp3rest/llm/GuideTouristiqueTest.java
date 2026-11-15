package ma.emsi.chafii.tp3rest.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuideTouristiqueTest {
    static class ModeleTemoin implements ChatModel {
        final List<ChatRequest> requetes = new ArrayList<>();
        @Override public ChatResponse doChat(ChatRequest requete) {
            requetes.add(requete);
            return ChatResponse.builder().aiMessage(AiMessage.from("{}" )).build();
        }
    }

    @Test void transmetLeLieuEtLeNombreDansLesBonsMessages() {
        var modele = new ModeleTemoin();
        var guide = AiServices.builder(GuideTouristique.class).chatModel(modele).build();
        assertEquals("{}", guide.visiter("Paris", 4));
        var messages = modele.requetes.getFirst().messages();
        assertTrue(((SystemMessage) messages.getFirst()).text().contains("exactement 4"));
        assertTrue(((SystemMessage) messages.getFirst()).text().contains("prix_moyen_repas"));
        assertEquals("Lieu à visiter : Paris", ((UserMessage) messages.getLast()).singleText());
    }

    @Test void lesRequetesNePartagentPasDeConversation() {
        var modele = new ModeleTemoin();
        var guide = AiServices.builder(GuideTouristique.class).chatModel(modele).build();
        guide.visiter("Nice", 2);
        guide.visiter("Maroc", 4);
        var messages = modele.requetes.getLast().messages();
        assertEquals(2, messages.size());
        assertFalse(messages.toString().contains("Nice"));
        assertTrue(((UserMessage) messages.getLast()).singleText().contains("Maroc"));
    }
}
