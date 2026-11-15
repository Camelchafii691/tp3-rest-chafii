package ma.emsi.chafii.tp3rest.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello-world")
public class HelloResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello, World!";
    }
    /** Exercice préparatoire : la réponse 500 est volontaire, conformément à l'énoncé. */
    @GET
    @Path("personnes/{nom}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello(@PathParam("nom") String nom) {
        // return Response.ok("Hello, " + nom).build();
        return Response.serverError().entity("Internal Server Error").build();
    }
}

