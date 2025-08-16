# seij-experiments fullstack-vite-quarkus

Modern frontend–backend development is often framed as a choice between two models:
– Single Page Applications hosted on a CDN, with the backend reduced to APIs.
– Full-stack JavaScript frameworks such as Next.js or Remix.

Both solve real problems, but they are not the only options. This project explores another path: a backend-driven
full-stack architecture, updated with today’s frontend practices. The backend (here Quarkus, but it could be Spring,
Django, Rails, etc.) remains in charge of routing, access rules, and page delivery, while the frontend (React + Vite +
TypeScript) is built as static assets and hydrated client-side.

This is not new, it is a proven model — one backend, one pipeline — modernized with fast bundling, hot reload, and
component-based UIs. The goal is to show that in 2025 this approach is still relevant, efficient, and often cheaper to
operate.

### Why this approach

Most teams already run a backend. Adding a second runtime just to serve a React app introduces extra cost and
complexity: new servers to host, new pipelines to maintain, more skills to recruit, more points of failure to watch. By
letting the backend also serve pages and assets, you:

– keep a single source of truth for routing and security,
– reuse existing deployment pipelines and monitoring,
– reduce operational overhead and infrastructure spend,
– simplify collaboration between backend and frontend teams.

The result is a familiar mental model — easy to understand, easy to run — but with a modern developer experience. It is
not a universal recipe: SEO-heavy sites may prefer SSR or static generation, and Node-centric stacks remain valid where
teams are fully invested in them. But in many enterprise and product contexts, backend-driven full-stack applications
strike a balance between speed, cost, and sustainability.

### About this tutorial

The tutorial is organized as a sequence of checkpoints 🚩. It is not a React, Vite, or Quarkus lesson, but a practical
guide on how to integrate them into a coherent backend-driven stack, so teams can adopt modern frontend workflows
without fragmenting their architecture.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation process](#installation-process)
    - [Java/Kotlin + Quarkus + Gradle](#javakotlin--quarkus--gradle)
    - [Typescript + Vite + pnpm](#typescript--vite--pnpm)
    - [Cleanup the mess from Vite samples](#cleanup-the-mess-from-vite-samples)
3. [Backend webpage template](#backend-webpage-template)
4. [Fusion (in development mode)](#fusion-in-development-mode)
5. [Module preload polyfill](#module-preload-polyfill)
6. [Static assets (development mode)](#static-assets-development-mode)
7. [Push data to the front](#push-data-to-the-front)
8. [Prepare for build mode: separate dev and build](#prepare-for-build-mode-separate-dev-and-build)
9. [Static assets in production mode](#static-assets-in-production-mode)
10. [Instructions for shipping in Docker](#instructions-for-shipping-in-docker)

## Prerequisites

- Java 21 installed (using https://sdkman.io/ for example)
- NodeJS LTS (20 or more) installed (using https://github.com/nvm-sh/nvm for example)
- pnpm (used here because we want to spare disk space, but you can use npm or yarn, doesn't matter)

## Installation process

### Java/Kotlin + Quarkus + Gradle

1. Create a Quarkus project using https://code.quarkus.io/
2. Enter: `net.seij.experiments` as package name, `fullstack-quarkus-vite` as project name
3. Select: Gradle with Kotlin DSL, Java 21
4. Select
    - quarkus-rest (because we have a REST API)
    - quarkus-rest-qute (to generate HTML pages with templates)
    - quarkus-kotlin (to enable Kotlin support)
5. Download and unzip

Test that it works: `./gradlew quarkusDev`

- http://localhost:8080/ Quarkus dev UI
- http://localhost:8080/some-page your first HTML page made with Qute template
- http://localhost:8080/hello your first API that returns text/plain

Stop it. Open with your IDE. Launch with your IDE in debug mode. Test again with a breakpoint in one of the classes in
`src/main/kotlin`.

🚩 Checkpoint: commit and push

### Typescript + Vite + pnpm

Create a Vite + Typescript + React project

```bash
pnpm create vite -t react-ts frontend
cd frontend
pnpm install
pnpm approve-builds # select esbuild, it will generate required instructions for pnpm to build node_module executables in the repository
pnpm run dev
```

Test http://localhost:5173

🚩 Checkpoint: commit and push

### Cleanup the mess from Vite samples

In `frontend` simplify the Vite sample code to reduce noise and focus on integration.

We will simplify CSS but not remove them, in order to test that all links are resolved.

Simplify [App.css](frontend/src/App.css) and [index.css](frontend/src/index.css) to keep only minimal styles (border,
font family, background).

Create a component, this will test that links are resolved and we have source maps in the browser.

`frontend/src/components/FirstComponent.tsx`

```typescript jsx
export function FirstComponent({message}: { message: string }) {
    const [count, setCount] = useState(0)
    return <div>
        <div style={{border: "1px solid red", padding: "1em"}}>{message}</div>
        <button onClick={() => setCount((count) => count + 1)}>
            count is {count}
        </button>
    </div>
}
```

Include your component in the App and simplify App.tsx (we keep images to test image resolution later).

```typescript jsx
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import {FirstComponent} from "./components/FirstComponent.tsx";

function App() {
    return (
        <>
            <div>
                <a href="https://vite.dev" target="_blank">
                    <img src={viteLogo} className="logo" alt="Vite logo"/>
                </a>
                <a href="https://react.dev" target="_blank">
                    <img src={reactLogo} className="logo react" alt="React logo"/>
                </a>
            </div>
            <h1>Vite + React</h1>
            <FirstComponent message={"Initial message !!!"}/>
        </>
    )
}

export default App
```

🚩 Checkpoint: commit and push

## Backend webpage template

Quarkus gives you an example webpage in /some-page. Let's adjust it.

```kotlin
@Path("/some-page")
class SomePage(@param:Location("some-page") val page: Template) {

    @GET
    @Produces(MediaType.TEXT_HTML)
    operator fun get(@QueryParam("name") name: String?): TemplateInstance {
        return page.data("name", name).data("scriptsHeader", null).data("scriptsFooter", null)
    }
}
```

We added the `@Location` annotation because relying only on a parameter name to resolve a template is unsafe.

Also rename `src/main/resources/templates/page.qute.html` to `src/main/resources/templates/some-page.qute.html`

Verify at http://localhost:8080/some-page.

Keep the template minimal to focus on integration.

`src/main/resources/templates/some-page.qute.html`

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Hello {name ?: "Qute"}</title>
    {scriptsHeader.raw}
</head>
<body>
<h1>Hello <b>{name ?: "Qute"}</b></h1>
<div id="root"></div>
<footer>Footer of the page</footer>
{scriptsFooter.raw}
</body>
</html>
```

`{scriptsHeader.raw}` and `{scriptsFooter.raw}` will later contain references to your JavaScript.
We also add `<div id="root"></div>` to tell the frontend App where to start writing React.

Note that we use the `{xxx.raw}` notation to avoid Qute html-escaping our Strings.

Note that we intentionally keep the Hello message and add a footer at screen to visualize
where React is written.

Reload the page, http://localhost:8080/some-page, you should see "Hello Qute"

Try adding URL parameters to verify dynamic rendering, for example: `?name=YourName`.

So, now, Quarkus can push data directly to the page. We can adjust title and what we need. Later we will push data to
the React component.

🚩 Checkpoint: commit and push

## Fusion (in development mode)

What interests us now is to fuse in development so we can start coding.
But also, we need to prepare for production (a little, step by step).

This first iteration will not cover all cases. The goal is to show the principles.

First we need to inject in the webpage references to Vite live server.

To do that, we need to follow Vite documentation
on [Backend integration](https://vite.dev/guide/backend-integration.html).

```kotlin
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

        return page.data("name", name).data("scriptsHeader", scriptsHeader).data("scriptsFooter", scriptsFooter)
    }
}
```

Notes

- We `@Inject` SmallRyeConfig to detect if we are in development mode or not
- script for header and footer are filled considering Vite's documentation.
- Note the use of double escape in Kotlin ($$) to avoid problems related to $ in the script's snippet.

This implementation works, but static assets are not yet handled.

Components now support Hot Module Replacement (HMR).

🚩 Checkpoint: commit and push

## Module preload polyfill

Now, you need to adjust your App.tsx to include this :

```typescript
// add the beginning of your app entry
import 'vite/modulepreload-polyfill' 
```

## Static assets (development mode)

Adjust vite configuration to serve static assets directly from Vite's server.

In `vite.config.ts` :

```typescript
import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        origin: "http://localhost:5173",
    }
})
```

## Push data to the front

The goal is to have the backend manage routing. When backend had some clues on where to go next and what
data provide to the front, you need to pass down this data.

One common pattern is to write a JavaScript string directly in the webpage.

First, from the `SomePage.kt`, send Json to the page template:

```kotlin
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
```

Note that this examples tries to add an XSS attack using a `<script>` element.

Then, adjust the template `some-page.qute.html` to accept this Json, and write it in the webpage inside
a `<script type="application/json" id="__INITIAL_DATA__">` tag.

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Hello {name ?: "Qute"}</title>
    {scriptsHeader.raw}
    {#if initialJson}
    <script type="application/json" id="__INITIAL_DATA__">{initialJson.raw}</script>
    {/if}
</head>
<body>
<h1>Hello <b>{name ?: "Qute"}</b></h1>
<div id="root"></div>
<footer>Footer of the page</footer>
{scriptsFooter.raw}
</body>
</html>
```

Finally read the initial Json in frontend: (`App.tsx`)

```typescript jsx
const initialDataJsonStr = document.getElementById("__INITIAL_DATA__")?.textContent
const initialData = initialDataJsonStr ? JSON.parse(initialDataJsonStr) : {}

function App() {
    // ...
    return (
        // ...
        <FirstComponent message={"Initial data: " + JSON.stringify(initialData)}/>
        // ...
    )
}
```

Now you should be able to handle server-sent initial data.

🚩 Checkpoint: commit and push

## Prepare for build mode: separate dev and build

To avoid having too much code in the page controller, let's split script injection into two implementations:
`PageScriptsDev` and `PageScriptsBuild`. Each implementation will implement `PageScripts` and will be produced by a
`PageScriptsFactory` depending if we are in dev mode or build mode.

Let's arrange our webpage to inject the factory :

```kotlin
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
```

The factory

```kotlin 

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
```

Interface

```kotlin
sealed interface PageScripts {
    fun scriptsHeader(): String
    fun scriptsFooter(): String
}
```

And Development implementation, letting the "build" implementation empty for now

```kotlin

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
```

```kotlin
class PageScriptsBuild() : PageScripts {
    override fun scriptsHeader(): String {
        return """<script type="text/javascript">alert("production mode not implemented yet")</script>"""
    }

    override fun scriptsFooter(): String {
        return ""
    }
}
```

To be sure that everything works:

- In development
    - be sure to have Vite running in `cd frontend; pnpm run dev;`
    - `./gradlew quarkusDev`
    -
        - open http://localhost:8080/some-page and you should get your web page running
- In build mode
    - Generate your frontend with `cd frontend; pnpm run build;`
    - `./gradlew quarkusRun`
    - open http://localhost:8080/some-page and you should get "production mode not implemented" alert

🚩 Checkpoint: commit and push

## Static assets in production mode

Now we are building the "production-like" mode.

We need to

- make the server serve static assets
- make the server generate correct asset links for chunks generated by vite

You can not just write `<script src="/assets/main.js />` in your webpage, because
when the bundler (Vite) generates assets, it gives names with hashes inside. This
made to make assets cacheable forever on the browser, and it is a great choice and
we need to keep that.

```
$ ls frontend/dist/assets/
main-BJ7uPMgx.css  main-e_fXbc23.js  react-CHdo91hT.svg
```

Fortunately, Vite provides a way to generate a `manifest` in Json format
with the list of all files it had generated. This looks like this:

```javascript
export default defineConfig({
    plugins: [react()],
    server: {
        origin: "http://localhost:5173",
    },
    build: {
        manifest: true,
        rollupOptions: {
            // overwrite default .html entry
            input: '/src/main.tsx',
        },
    }
})
```

Remember that everything is explained in Vite documentation and you have several possible approaches;
others may better suit your needs.

Now run `pnpm run build` and you will see Vite's manifest generated in
`frontend/dist/.vite/manifest.json`.

Next, we need the server to read and interpret this manifest.

In the source code of this project, look
at [ViteManifestReader.kt](src/main/kotlin/net/seij/experiments/fullstackquarkusvite/config/ViteManifestReader.kt)
a utility class you can @Inject. To locate the manifest, you need to provide an **absolute path to frontend/dist**
environment
variable in your application launcher.

Create a new launcher in IntelliJ for your project or be careful to have an environment variable named
`myapp.frontend.dist=pathto/frontend/dist` in your Shell when you launch `./gradlew quarkusRun`.

Now we can complete our PageScriptBuild for production.

```kotlin 
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
```

Now we need to serve static resources. In this example, with Quarkus,
we can configure the web server (Vertx) to handle routes to our built assets.

```kotlin 
class StaticResources {

    fun installRoute(@Observes startupEvent: StartupEvent, router: Router) {
        val path = ConfigProvider.getConfig().getConfigValue("myapp.frontend.dist").rawValue
        router
            .route()
            .path("/assets/*")
            .handler(StaticHandler.create(FileSystemAccess.ROOT, "$path/assets"))

        router
            .route()
            .path("/vite.svg")
            .handler(StaticHandler.create(FileSystemAccess.ROOT, "$path/vite.svg"))
    }
}
```

Note that Vite puts `public/` content in the root directory of `dist/`.
We cannot allow serving `dist/` directly at our application root, without
risking overriding other Quarkus stuff, namely the Dev UI or Rest APIs.

So just to keep the things clear, each asset like `vite.svg` must be
explicitly declared in the routes.

Final test:

```
cd frontend; pnpm run build
export MYAPP_FRONTEND_DIST=$(pwd)/frontend/dist
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

Open http://localhost:8080/some-page to verify.

🚩 Checkpoint: commit and push

## Instructions for shipping in Docker

To deploy, package everything in a Docker container. There are Dockerfiles here you can build (the ones
given by Quarkus). Adjust them to copy the dist/ directory in the container.

First, allow Docker to copy files from dist/ in the container. Adjust the .dockerignore file.

```dockerignore
frontend/**
!frontend/dist/**
```

Copy the `dist/` directory inside the container by modifying the DockerFile

```dockerfile
COPY --chown=185 frontend/dist /deployments/frontend/dist/
ENV MYAPP_FRONTEND_DIST="/deployments/frontend/dist"
```

Build, run and test

```bash
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/fullstack-demo-jvm .
docker run -i --rm -p 8080:8080 quarkus/fullstack-demo-jvm .
```

Go to http://localhost:8080/some-page and verify the result. Clear your browser cache before testing.
Then check that Quarkus DEV UI is not present anymore http://localhost:8080/q/dev-ui/welcome should
give an error.

🚩 Checkpoint: commit and push




