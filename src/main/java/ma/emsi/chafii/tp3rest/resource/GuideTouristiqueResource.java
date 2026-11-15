package ma.emsi.chafii.tp3rest.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
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
     * Demande à Gemini les deux principaux endroits et le prix moyen d'un repas.
     * @param villeOuPays nom de la ville ou du pays à visiter
     * @return objet JSON touristique, ou erreur JSON si le service est indisponible
     */
    @GET
    @Path("lieu/{ville_ou_pays}")
    public Response endroitsDeVisite(@PathParam("ville_ou_pays") String villeOuPays) {
        try {
            return Response.ok(client.visiter(villeOuPays)).build();
        } catch (IllegalStateException erreur) {
            return Response.status(503).entity(Map.of("erreur", erreur.getMessage())).build();
        } catch (RuntimeException erreur) {
            return Response.status(502).entity(Map.of("erreur", "Impossible d'obtenir une réponse de Gemini.")).build();
        }
    }
}
