package net.seij.experiments.fullstackquarkusvite.config

class ViteManifestChunk(
    val src: String?,
    val file: String?,
    val css: List<String>,
    val assets: List<String>,
    val isEntry: Boolean,
    val name: String?,
    val names: List<String>,
    val isDynamicEntry: Boolean,
    val imports: List<String>,
    val dynamicImports: List<String>
)