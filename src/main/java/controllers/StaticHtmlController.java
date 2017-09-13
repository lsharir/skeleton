package controllers;

import com.google.common.io.Resources;
import java.io.IOException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static java.nio.charset.StandardCharsets.UTF_8;

@Path("/")
public class StaticHtmlController {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getIndexPage() throws IOException {
        Resources.getResource("index.html");
        return Resources.toString(Resources.getResource("index.html"), UTF_8);
    }

    @Path("js/{filename}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getJsFile(@PathParam("filename") String fileName) throws IOException {
        fileName = "js/" + fileName;
        Resources.getResource(fileName);
        return Resources.toString(Resources.getResource(fileName), UTF_8);
    }

    @Path("css/{filename}")
    @GET
    @Produces(MediaType.WILDCARD)
    public String getStyleSheet(@PathParam("filename") String fileName) throws IOException {
        fileName = "css/" + fileName;
        Resources.getResource(fileName);
        return Resources.toString(Resources.getResource(fileName), UTF_8);
    }

    @Path("/netid")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNetId() {
        return "ls873";
    }
}
