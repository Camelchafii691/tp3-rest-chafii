package ma.emsi.chafii.tp3rest.llm;

import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;

/** Client Gemini sans historique partagé entre les requêtes REST. */
@ApplicationScoped
public class LlmClientForGuideTouristique {
    private volatile GuideTouristique guide;

    public String visiter(String lieu) {
        return service().visiter(lieu);
    }

    private synchronized GuideTouristique service() {
        if (guide == null) {
            String cle = System.getenv("GEMINI_API_KEY");
            if (cle == null || cle.isBlank()) cle = System.getenv("GEMINI_KEY");
            if (cle == null || cle.isBlank()) {
                throw new IllegalStateException("La clé Gemini n'est pas configurée sur le serveur.");
            }
            String modele = System.getenv("GEMINI_MODEL");
            if (modele == null || modele.isBlank()) {
                throw new IllegalStateException("Configure GEMINI_MODEL avec un modèle accessible à ton compte.");
            }
            var builder = GoogleGenAiChatModel.builder()
                    .apiKey(cle).modelName(modele)
                    .responseFormat(ResponseFormat.JSON)
                    .temperature(0.3).timeout(Duration.ofSeconds(60))
                    .maxRetries(0).logRequestsAndResponses(false);
            // Permet de tester le protocole Gemini avec un serveur local simulé.
            String endpoint = System.getenv("GEMINI_API_ENDPOINT");
            if (endpoint != null && !endpoint.isBlank()) builder.apiEndpoint(endpoint);
            guide = AiServices.builder(GuideTouristique.class)
                    .chatModel(builder.build()).build();
        }
        return guide;
    }
}
