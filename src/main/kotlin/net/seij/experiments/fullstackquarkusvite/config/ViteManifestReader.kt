package net.seij.experiments.fullstackquarkusvite.config

import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.json.Json
import jakarta.json.JsonObject
import jakarta.json.JsonString
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.reader

@ApplicationScoped
class ViteManifestReader {

    var manifestChunks = emptyList<ViteManifestChunk>()

    @ConfigProperty(name = "myapp.frontend.dist")
    private lateinit var distPath: String

    @PostConstruct
    fun init() {
        val path = Path.of(distPath).resolve(".vite").resolve("manifest.json").absolute()
        if (path.exists()) {
            val manifest = Json.createReader(path.reader()).readObject()

            fun toStringList(manifest: JsonObject, key: String): List<String> =
                if (!manifest.containsKey(key) || manifest.isNull(key)) emptyList()
                else manifest.getJsonArray(key).getValuesAs(JsonString::getString).toList()

            fun toChunk(manifestEntry: JsonObject): ViteManifestChunk {
                return ViteManifestChunk(
                    src = manifestEntry.getString("src", null),
                    file = manifestEntry.getString("file", null),
                    css = toStringList(manifestEntry, "css"),
                    assets = toStringList(manifestEntry, "assets"),
                    isEntry = manifestEntry.getBoolean("isEntry", false),
                    name = manifestEntry.getString("name", null),
                    names = toStringList(manifestEntry, "names"),
                    isDynamicEntry = manifestEntry.getBoolean("isDynamicEntry", false),
                    imports = toStringList(manifestEntry, "imports"), dynamicImports = toStringList(manifest, "imports")

                )
            }

            this.manifestChunks = manifest.map { entry -> toChunk(manifest.getJsonObject(entry.key)) }.toList()

        }
    }

    fun findEntry(entry: String): ViteManifestChunk {
        return manifestChunks.firstOrNull { it.isEntry && it.name == entry }
            ?: throw Exception("Entry $entry not found")
    }
}


