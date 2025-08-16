package net.seij.experiments.fullstackquarkusvite

import io.quarkus.qute.Location
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import io.smallrye.config.SmallRyeConfig
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.json.Json
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import net.seij.experiments.fullstackquarkusvite.config.PageScripts
import net.seij.experiments.fullstackquarkusvite.config.PageScriptsBuild
import net.seij.experiments.fullstackquarkusvite.config.PageScriptsDev
import net.seij.experiments.fullstackquarkusvite.config.ViteManifestReader

@Path("/some-page")
class SomePage @Inject constructor(
    @param:Location("some-page") val page: Template,
    val scripts: PageScriptsFactory
) {

    @GET
    @Produces(MediaType.TEXT_HTML)
    operator fun get(@QueryParam("name") name: String?): TemplateInstance {

        val scriptsHeader = scripts.scripts().scriptsHeader()
        val scriptsFooter = scripts.scripts().scriptsFooter()

        val initialJson = Json.createObjectBuilder()
            .add("name", name ?: "Unknown")
            .add("dangertest", "<script>alert('ATTACK XSS')</script>")
            .build()
            .toString()
            .replace("<", "\\u003c")

        return page.data("name", name)
            .data("scriptsHeader", scriptsHeader)
            .data("scriptsFooter", scriptsFooter)
            .data("initialJson", initialJson)
    }

}


@ApplicationScoped
class PageScriptsFactory() {
    @Inject
    private lateinit var config: SmallRyeConfig

    @Inject
    private lateinit var manifestReader: ViteManifestReader

    val viteDevServerURL = "http://localhost:5173"


    fun isDev(): Boolean {
        return config.profiles.contains("dev")
    }

    fun scripts(entry: String = "main"): PageScripts {
        return if (isDev()) {
            PageScriptsDev(viteDevServerURL, entry)
        } else {
            PageScriptsBuild(entry, manifestReader)
        }
    }
}