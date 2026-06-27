# KMP Fullstack Playground

A public, spec-driven **Kotlin Multiplatform** playground and interactive knowledge base for
Kotlin, DSA, KMM, and backend development — built to learn in the open and to demonstrate clean,
production-grade architecture across every target Kotlin can reach.

Its headline feature: **animated, interactive visualizations of data-structure & algorithm
challenges**, written once in Compose Multiplatform and runnable on Android, iOS, Desktop, and the
Web (Kotlin/Wasm).

> Status: early days. The pure algorithm/trace kernel and the DSA use cases are built and tested;
> the Compose visualizer, the showcase shell, and the Spring Boot + Spring AI MCP backend are in
> progress. See the roadmap below.

## Why this exists

- **Learn by building** — every PL/Kotlin course topic becomes runnable, tested code.
- **One capability, many faces** — each DSA challenge is a single use case, surfaced through a
  Compose UI (for humans), a REST API, and an **MCP tool** (for AI agents). Textbook ports & adapters.
- **Show, don't tell** — algorithms render as animated traces instead of static prose.

## Architecture

Clean Architecture with a strict dependency rule — everything points inward to the domain, which
depends on nothing.

```
Compose UI ─┐
MCP tool   ─┼─▶  use cases  ──▶  domain  ◀──  data (catalog / Ktor / Spring)
REST API   ─┘     (the capabilities, UI-free)        ▲
                                                core: algorithms + trace engine
```

The **trace engine** separates *what an algorithm does* (a pure, testable `Trace`) from *how it's
drawn* (Compose). Algorithms accept an optional `Tracer` defaulting to `NoOpTracer`, so the
canonical run stays clean while visualization passes a `RecordingTracer`.

## Tech stack

- **Kotlin 2.4.0**, Gradle 9.4, AGP 9.2
- **Compose Multiplatform** (Android · iOS · Desktop · Web/Wasm)
- **Spring Boot 4.1** + **Spring AI** MCP server (backend & AI tools)
- Koin (DI) · kotlinx-coroutines · kotlin.test
- Jetpack Navigation (multiplatform) · MVI presentation

## Modules

| Module | Role |
|--------|------|
| `:core` | Pure DSA kernel (trace engine + algorithms) and the DSA use cases — UI-free, multiplatform |
| `:app:shared` | Compose Multiplatform showcase shell + feature UI |
| `:app:{androidApp, desktopApp, webApp, iosApp}` | Per-target entry points |
| `:server` | Backend (Spring Boot) + Spring AI MCP tools over the same use cases |

## Running

- Desktop: `./gradlew :app:desktopApp:run`
- Web (Wasm): `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
- Android: `./gradlew :app:androidApp:assembleDebug`
- Server: `./gradlew :server:run`
- iOS: open `app/iosApp` in Xcode and run
- Tests: `./gradlew :core:jvmTest` (and per-module test tasks)

## Roadmap

- [x] Trace engine + first algorithm (Two Sum II, two pointers), fully tested
- [x] DSA domain + use cases (`ListChallenges`, `GetChallenge`, `GenerateTrace`)
- [ ] Compose visualizer (bars renderer, timeline controls) + hand-rolled & Orbit MVI
- [ ] Showcase shell (Jetpack Navigation + Koin)
- [ ] Spring Boot backend + Spring AI MCP server
- [ ] Android App Functions integration
- [ ] Live DSA gallery deployed to GitHub Pages (Kotlin/Wasm)
- [ ] More challenges across two-pointers, sliding window, prefix sum, hashmaps, sorting

## License

[MIT](./LICENSE) © 2026 Vinicius Coscia
