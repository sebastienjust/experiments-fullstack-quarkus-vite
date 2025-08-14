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
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1>Vite + React</h1>
      <FirstComponent message={"Initial message !!!"} />
    </>
  )
}

export default App
```

🚩 You can commit and push if it works



