# AGENTS.md — AI Assistant Instructions

Repository-level instructions for AI assistants (Codex, Claude Code, Antigravity) working in this workspace.

> [!IMPORTANT]
> **Owner context**: This project belongs to **Vini** (Senior Mobile Engineer, 7+ years Android/Kotlin/KMP).
> For full identity, preferences, and AI tool roles, see the Akira knowledge base at `~/Akira/vault/_system/akira/workflows/init.md`.

---

## 1. Project Overview

**KMP Fullstack Playground** is a public, spec-driven **Kotlin Multiplatform** playground and interactive knowledge base for Kotlin, DSA, KMM, and backend development.

**Headline feature**: animated, interactive visualizations of data-structure & algorithm challenges, written once in Compose Multiplatform and runnable on Android, iOS, Desktop, and Web (Kotlin/Wasm).

**Status**: early days — the pure algorithm/trace kernel and DSA use cases are built and tested; the Compose visualizer, showcase shell, and backend are in progress.

---

## 2. Architecture

Clean Architecture with a strict dependency rule — everything points inward to the domain, which depends on nothing.

```
Compose UI ─┐
MCP tool   ─┼─▶  use cases  ──▶  domain  ◀──  data (catalog / Ktor / Spring)
REST API   ─┘     (the capabilities, UI-free)        ▲
                                                core: algorithms + trace engine
```

**Key design patterns**:
- **Trace engine**: separates *what an algorithm does* (a pure, testable `Trace`) from *how it's drawn* (Compose). Algorithms accept an optional `Tracer` defaulting to `NoOpTracer`; visualization passes a `RecordingTracer`.
- **Ports & Adapters**: each DSA challenge is a single use case, surfaced through a Compose UI (for humans), a REST API, and an MCP tool (for AI agents).
- **MVI presentation** on the Compose side.
- **Koin** for dependency injection.

---

## 3. Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | **Kotlin 2.4.0** |
| Build | Gradle 9.4 · AGP 9.2 · Version Catalog |
| UI | Compose Multiplatform (Android · iOS · Desktop · Web/Wasm) |
| Backend | Spring Boot 4.1 + Spring AI MCP server (planned); currently Ktor server skeleton |
| DI | Koin |
| Navigation | Jetpack Navigation (multiplatform) |
| Concurrency | kotlinx-coroutines |
| Testing | kotlin.test · Ktor test host |

---

## 4. Module Map

```
KMP-Fullstack-Playground/
├── core/                  # Pure DSA kernel — trace engine + algorithms + use cases
│   └── src/commonMain/    #   Multiplatform, UI-free, no platform dependencies
│       └── .../dsa/
│           ├── algorithm/    # Algorithm implementations (e.g., TwoSumSorted)
│           ├── domain/       # Domain models (Challenge)
│           ├── trace/        # Trace engine (Tracer, Trace)
│           ├── usecase/      # Use cases (ListChallenges, GetChallenge, GenerateTrace)
│           └── data/         # In-memory repository
├── app/
│   ├── shared/            # Compose Multiplatform showcase shell + feature UI
│   ├── androidApp/        # Android entry point
│   ├── desktopApp/        # Desktop (JVM) entry point
│   ├── webApp/            # Web (Kotlin/Wasm) entry point
│   └── iosApp/            # iOS entry point (open in Xcode)
├── server/                # Backend (Ktor now, Spring Boot planned)
├── gradle/                # Version catalog and wrapper
└── AGENTS.md              # ← you are here
```

**Package root**: `com.viniciuscoscia.kmpfullstackplayground`

---

## 5. Build & Run Commands

| Target | Command |
|--------|---------|
| Desktop | `./gradlew :app:desktopApp:run` |
| Web (Wasm) | `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun` |
| Android | `./gradlew :app:androidApp:assembleDebug` |
| Server | `./gradlew :server:run` |
| iOS | Open `app/iosApp` in Xcode and run |
| Core tests | `./gradlew :core:jvmTest` |
| All tests | `./gradlew check` |

**Gradle properties**: configuration cache and build cache are enabled. JVM daemon uses 3 GB, Gradle uses 4 GB.

---

## 6. Code Conventions

- **Kotlin style**: official (`kotlin.code.style=official`)
- **Dependency rule**: `:core` has zero platform dependencies and zero UI dependencies. Never add Compose, Android, or platform-specific imports to `:core`.
- **New algorithms**: add to `core/src/commonMain/.../dsa/algorithm/`, implement `Tracer` support, register in `InMemoryChallengeRepository`, and add tests in `core/src/commonTest/`.
- **New use cases**: add to `core/src/commonMain/.../dsa/usecase/`, keep them pure and UI-free.
- **Tests**: write tests in `commonTest` for multiplatform coverage. Use `kotlin.test` assertions.
- **Naming**: PascalCase for classes/interfaces, camelCase for functions/properties, `SCREAMING_SNAKE` for constants.

---

## 7. Current Roadmap

- [x] Trace engine + first algorithm (Two Sum II, two pointers), fully tested
- [x] DSA domain + use cases (`ListChallenges`, `GetChallenge`, `GenerateTrace`)
- [ ] Compose visualizer (bars renderer, timeline controls) + MVI
- [ ] Showcase shell (Jetpack Navigation + Koin)
- [ ] Spring Boot backend + Spring AI MCP server
- [ ] Android App Functions integration
- [ ] Live DSA gallery deployed to GitHub Pages (Kotlin/Wasm)
- [ ] More challenges across two-pointers, sliding window, prefix sum, hashmaps, sorting

---

## 8. AI Assistant Guidelines

### General

- **English first**: all communication, code comments, and generated content in English.
- **Git safety**: never commit or push without Vini's explicit permission.
- **Preserve documentation**: keep existing comments and KDoc unless directly modifying that code.

### Gemini's Role (from Akira AI Tool Map)

Gemini / Antigravity is the **"Integrated Sidekick"** — tightly integrated with the IDE for:
- Quick terminal and code explanations
- Android Studio integration (JetBrains Companion MCP)
- Fast context bootstrapping and daily helper tasks
- Multimodal assistance (screenshots, diagrams)

For deep architecture design, use Claude. For repo-wide refactors, use Codex.

### When Modifying Code

1. **Respect the dependency rule** — `:core` must stay pure and platform-free.
2. **Run tests** before declaring work done: `./gradlew :core:jvmTest` at minimum.
3. **Follow existing patterns** — look at `TwoSumSorted` and its test as the canonical example for new algorithms.
4. **Keep use cases thin** — they orchestrate domain logic, they don't contain it.
5. **Trace engine convention** — algorithms should accept an optional `Tracer` parameter defaulting to `NoOpTracer`.

### JetBrains IDE Integration

This workspace has the **JetBrains Companion MCP** available for Android Studio integration:
- `ide_get_active_editor` — see the currently open file
- `ide_get_open_files` — list all open editor tabs
- `ide_get_diagnostics` — retrieve IDE errors, warnings, lint issues
- `ide_open_file` — open a file in the IDE

Use these tools to stay in sync with Vini's editing context.

---

## 9. Akira Cross-Reference

This project is part of Vini's broader learning and building ecosystem managed by Akira. Relevant Akira context:

- **Akira status board**: `~/Akira/vault/000-STATUS.md`
- **Project ideas**: `~/Akira/vault/projects/ideas-to-work-on.md`
- **AI tool roles decision**: `~/Akira/vault/brain/decision-log/2026-05-26-ai-tool-roles.md`
- **Stable preferences**: `~/Akira/vault/_system/akira/memory/stable-preferences.md`

This project appears in Akira's roadmap as the "Playground visualizer" — an evening/reward-block project.

---

## Navigation

- Home: [README.md](./README.md)
- License: [MIT](./LICENSE)
