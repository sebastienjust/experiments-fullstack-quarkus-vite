package net.seij.experiments.fullstackquarkusvite

import io.quarkus.qute.Location
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType

@Path("/some-page")
class SomePage(@param:Location("some-page") val page: Template) {

    @GET
    @Produces(MediaType.TEXT_HTML)
    operator fun get(@QueryParam("name") name: String?): TemplateInstance {
        return page.data("name", name).data("scriptsHeader", null).data("scriptsFooter", null)
    }
}
