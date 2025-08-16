package net.seij.experiments.fullstackquarkusvite.config


sealed interface PageScripts {
    fun scriptsHeader(): String
    fun scriptsFooter(): String
}

class PageScriptsDev(val viteDevServerURL: String, val entry: String) : PageScripts {
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
            <script type="module" src="$${viteDevServerURL}/src/$$entry.tsx"></script>
        """.trimIndent()
    }
}

class PageScriptsBuild(val entry: String, val manifestReader: ViteManifestReader) : PageScripts {

    override fun scriptsHeader(): String {
        val entry = manifestReader.findEntry(entry)
        val cssUrls = entry.css.map { """<link rel="stylesheet" href="/$it" />""" }
        val moduleUrl = entry.file ?: ""
        val module = """<script type="module" src="/$moduleUrl" ></script>"""
        return (cssUrls + module).joinToString("\n")
    }

    override fun scriptsFooter(): String {
        return ""
    }
}