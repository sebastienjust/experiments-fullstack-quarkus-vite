package net.seij.experiments.fullstackquarkusvite.config

import io.quarkus.runtime.StartupEvent
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.FileSystemAccess
import io.vertx.ext.web.handler.StaticHandler
import jakarta.enterprise.event.Observes
import org.eclipse.microprofile.config.ConfigProvider

class StaticResources {

    fun installRoute(@Observes startupEvent: StartupEvent, router: Router) {
        val path = ConfigProvider.getConfig().getConfigValue("myapp.frontend.dist").rawValue

        fun makeHandler(subpath: String) = StaticHandler
            .create(FileSystemAccess.ROOT, "$path/$subpath")
            .setCachingEnabled(true)
            .setMaxAgeSeconds(31536000)
            .setAlwaysAsyncFS(true)
            .setIncludeHidden(false)

        router
            .route()
            .path("/assets/*")
            .handler(makeHandler("assets"))

        router
            .route()
            .path("/vite.svg")
            .handler(makeHandler("vite.svg"))
    }
}