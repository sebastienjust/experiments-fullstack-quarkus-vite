# seij-experiments fullstack-vite-quarkus

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
    - quarkus-kotlin (because we like it)
5. Download and unzip

Test that it works: `./gradlew quarkusDev`

- http://localhost:8080/ Quarkus dev UI
- http://localhost:8080/some-page your first HTML page made with Qute template
- http://localhost:8080/hello your first API that returns text/plain

Stop it. Open with your IDE. Launch with your IDE in debug mode. Test again with a breakpoint in one of the classes in
`src/main/kotlin`.

🚩 You can commit and push

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

🚩 You can commit and push

### Cleanup the mess from Vite samples

In `frontend` let's simplify a lot of things, otherwise we won't see anything.

We will simplify CSS but not remove them, in order to test that all links are resolved.

In `frontend/src/frontend/src/App.css`, keep only that:

```css
#root {
    /* Keep border to have a visual separation from React to the native HTML page */
    border: 1px dashed darkorange;
    margin: 0 auto;
    padding: 2rem;
}
```

In `frontend/src/frontend/src/index.css`, keep only that:

```css
:root {
    font-family: system-ui, Avenir, Helvetica, Arial, sans-serif;
    line-height: 1.5;
    font-weight: 400;

    color-scheme: light dark;
    color: rgba(255, 255, 255, 0.87);
    background-color: #242424;

    font-synthesis: none;
    text-rendering: optimizeLegibility;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
}
```

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

🚩 You can commit and push if it works

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

We added the `@Location` annotation because just determining the name of a template by a parameter name is a big danger.

Also rename `src/main/resources/templates/page.qute.html` to `src/main/resrouces/templates/som-page.qute.html`

Go to http://localhost:8081/some-page and it should work.

Keep the template to minimum, so we can start playing.

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

`{scriptsHeader.raw}` and `{scriptsFooter.raw}` will later contain references to your Javascript.
We also add `<div id="root"></div>` to tell the frontend App where to start writing React.

Note that we use the `{xxx.raw}` notation to avoid Qute html-escaping our Strings.

Note that we intentionally keep the Hello message and add a footer at screen to visualize
where React is written.

Reload the page, http://localhost:8081/some-page, you should see "Hello Qute"

Then play with URL parameters http://localhost:8081/some-page?name=Call%20me%20by%20YourName for example, and you should
see "Hello Call me by YourName".

So, now, Quarkus can push data directly to the page. We can adjust title and what we need. Later we will push data to
the React component.

🚩 You can commit and push if it works

## Fusion 👉👈 (in development mode)

What interests us now is to fuse in development so we can start coding.
But also, we need to prepare for production (a little, step by step).

This is a first iteration and everything won't work. The goal is to show the principles.

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

- We Inject SmallRyeConfig to detect if we are in development mode or not
- script for header and footer are filled considering Vite's documentation.
- Note the use of double escape in Kotlin ($$) to avoid problems related to $ in the script's snippet.

Ok this is ugly and it works, except for static assets.

You can now play with your component, it will use HMR to refresh the webpage, develop new pages and let's go.

We have never been so close to the end.

🚩 You can commit and push if it works

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

One common pattern is to write a Javascript string directly in the webpage.

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

Then, ajust the template `some-page.qute.html` to accept this Json, and write it in the webpage inside
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

🚩 You can commit and push if it works

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

To be sure that everything works correctly:

- In development
  - be sure to have Vite running in `cd frontend; pnpm run dev;` 
  - `./gradlew quarkusDev`
  - - open http://localhost:8080/some-page and you should get your web page running
- In build mode
  - Generate your frontend with `cd frontend; pnpm run build;` 
  - `./gradlew quarkusRun`
  - open http://localhost:8080/some-page and you should get "production mode not implemented" alert

🚩 You can commit and push if it works

