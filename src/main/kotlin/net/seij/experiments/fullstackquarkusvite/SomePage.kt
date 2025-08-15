package net.seij.experiments.fullstackquarkusvite

import io.quarkus.qute.Location
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import io.smallrye.config.SmallRyeConfig
import jakarta.inject.Inject
import jakarta.json.Json
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType

@Path("/some-page")
class SomePage(@param:Location("some-page") val page: Template) {

    @Inject
    private lateinit var config: SmallRyeConfig

    fun isDev(): Boolean {
        return config.profiles.contains("dev")
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    operator fun get(@QueryParam("name") name: String?): TemplateInstance {
        val viteDevServerURL = "http://localhost:5173"

        val scriptsHeader = if (isDev()) $$"""
            <script type="module">
              import RefreshRuntime from '$${viteDevServerURL}/@react-refresh'
              RefreshRuntime.injectIntoGlobalHook(window)
              window.$RefreshReg$ = () => {}
              window.$RefreshSig$ = () => (type) => type
              window.__vite_plugin_react_preamble_installed__ = true
            </script>
            <script type="module" src="$${viteDevServerURL}/@vite/client"></script>
        """.trimIndent()
        else null

        val scriptsFooter = if (isDev()) $$"""
            <script type="module" src="$${viteDevServerURL}/src/main.tsx"></script>
        """.trimIndent() else null

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
