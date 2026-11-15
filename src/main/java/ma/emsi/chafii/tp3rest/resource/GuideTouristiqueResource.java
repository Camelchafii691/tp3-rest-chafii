package ma.emsi.chafii.tp3rest.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.io.StringReader;
import java.util.Set;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import ma.emsi.chafii.tp3rest.llm.LlmClientForGuideTouristique;

@Path("/guide")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class GuideTouristiqueResource {
    @Inject
    LlmClientForGuideTouristique client;

    /**
     * Demande à Gemini les principaux endroits et le prix moyen d'un repas.
     * @param villeOuPays nom de la ville ou du pays à visiter
     * @param nombre nombre de visites (2 par défaut, de 1 à 10)
     * @return objet JSON touristique, ou erreur JSON si le service est indisponible
     */
    @GET
    @Path("lieu/{ville_ou_pays}")
    public Response endroitsDeVisite(@PathParam("ville_ou_pays") String villeOuPays,
                                      @QueryParam("nb") @DefaultValue("2") int nombre) {
        if (villeOuPays.isBlank() || villeOuPays.length() > 120 || nombre < 1 || nombre > 10) {
            return Response.status(400).entity(Map.of("erreur", "Indique un lieu de 1 à 120 caractères et nb entre 1 et 10.")).build();
        }
        try {
            String json = client.visiter(villeOuPays.trim(), nombre);
            // Vérifie que le fournisseur a bien respecté le contrat JSON avant de répondre.
            JsonObject objet;
            try (var lecteur = Json.createReader(new StringReader(json))) {
                objet = lecteur.readObject();
            }
            if (!objet.keySet().equals(Set.of("ville_ou_pays", "endroits_a_visiter", "prix_moyen_repas"))
                    || objet.getString("ville_ou_pays").isBlank()
                    || objet.getString("prix_moyen_repas").isBlank()
                    || objet.getJsonArray("endroits_a_visiter").size() != nombre
                    || objet.getJsonArray("endroits_a_visiter").stream()
                        .anyMatch(v -> v.getValueType() != JsonValue.ValueType.STRING
                            || ((jakarta.json.JsonString) v).getString().isBlank())) {
                throw new IllegalArgumentException("Format de réponse invalide.");
            }
            return Response.ok(objet.toString()).header("Cache-Control", "no-store").build();
        } catch (IllegalStateException erreur) {
            return Response.status(503).entity(Map.of("erreur", erreur.getMessage())).build();
        } catch (RuntimeException erreur) {
            return Response.status(502).entity(Map.of("erreur", "Impossible d'obtenir une réponse de Gemini.")).build();
        }
    }
}
