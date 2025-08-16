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

    val viteDevServerURL = "http://localhost:5173"

    fun isDev(): Boolean {
        return config.profiles.contains("dev")
    }

    fun scripts(): PageScripts {
        return if (isDev()) {
            PageScriptsDev(viteDevServerURL)
        } else {
            PageScriptsBuild()
        }
    }
}

sealed interface PageScripts {
    fun scriptsHeader(): String
    fun scriptsFooter(): String
}

class PageScriptsDev(val viteDevServerURL: String) : PageScripts {
    override fun scriptsHeader(): String {
        return $$"""
            <script type="module">
              import RefreshRuntime from '$${viteDevServerURL}/@react-refresh'
              RefreshRuntime.injectIntoGlobalHook(window)
              window.$RefreshReg$ = () => {}
              window.$RefreshSig$ = () => (type) => type
              window.__vite_plugin_react_preamble_installed__ = true
            </script>
            <script type="module" src="$${viteDevServerURL}/@vite/client"></script>
        """.trimIndent()
    }

    override fun scriptsFooter(): String {
        return $$"""
            <script type="module" src="$${viteDevServerURL}/src/main.tsx"></script>
        """.trimIndent()
    }
}

class PageScriptsBuild() : PageScripts {
    override fun scriptsHeader(): String {
        return """<script type="text/javascript">alert("production mode not implemented yet")</script>"""
    }

    override fun scriptsFooter(): String {
        return ""
    }
}