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

