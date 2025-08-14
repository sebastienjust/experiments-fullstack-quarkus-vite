# seij-experiments fullstack-vite-quarkus

## Prerequisites

- Java 21 installed (using https://sdkman.io/ for example)
- NodeJS LTS (20 or more) installed (using https://github.com/nvm-sh/nvm for example)
- pnpm (used here because we want to spare disk space, but you can use npm or yarn, doesn't matter)

## Installation process

### Java/Kotlin + Quarkus + Gradle

1. Create a Quarkus project using https://code.quarkus.io/
2. Choose Gradle with Kotlin DSL, Java 21
3. Select 
   - quarkus-rest (because we have a REST API)
   - quarkus-rest-qute (to generate HTML pages with templates)
   - quarkus-kotlin (because we like it)
4. Download and unzip

Test that it works: `./gradlew quarkusDev`

- http://localhost:8080/ Quarkus dev UI
- http://localhost:8080/some-page your first HTML page made with Qute template
- http://localhost:8080/hello your first API that returns text/plain

### Typescript + Vite + pnpm 

