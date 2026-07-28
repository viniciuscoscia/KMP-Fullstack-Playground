# AGENTS.md — Repository Instructions

Guidance for AI assistants working in `KMP-Fullstack-Playground`.

> [!IMPORTANT]
> This is Vini's public learning playground. Keep examples educational and production-minded, but
> do not turn small lessons into framework-heavy abstractions. Never commit or push without explicit
> permission.

## 1. Project Purpose and Current Shape

This repository currently has two related tracks:

1. **Kotlin Multiplatform DSA playground** — a pure algorithm and trace kernel intended to power
   interactive Compose visualizations across Android, iOS, Desktop, and Web.
2. **Android Basics showcase** — Android-only, Activity-based demos for lifecycle, launch modes,
   ViewModels, resources, intents, broadcasts, foreground services, WorkManager, URIs, and content
   providers.

The DSA kernel and its use cases are implemented and tested. The cross-platform visualizer is not:
the shared Compose UI is still the generated greeting sample. The server is a minimal Ktor app with
one `GET /` greeting route. Spring Boot, Spring AI/MCP, Koin, Navigation, and MVI remain roadmap
items and must not be described as current dependencies.

When documentation and code disagree, treat `settings.gradle.kts`, the module build files, the
version catalog, and source code as the source of truth.

### Collaboration Style

- Use English for technical work.
- Lead with the direct answer and stay concise unless Vini asks for depth.
- Challenge questionable assumptions and recommend one default path.
- Explain changed behavior and verification without narrating routine tool usage.
- For learning-oriented implementation, agree on the approach before making code changes.

## 2. Verified Toolchain

| Concern | Current value |
|---|---|
| Kotlin | 2.4.10 |
| Gradle wrapper | 9.6.1 |
| Android Gradle Plugin | 9.3.1 |
| Compose Multiplatform | 1.11.1 |
| Android SDK | compile 37, target 36, min 28 |
| JVM bytecode target | Java 11 for Android/KMP Android |
| Ktor | 3.5.1 |
| Package root | `com.viniciuscoscia.kmpfullstackplayground` |

Dependencies and plugins belong in `gradle/libs.versions.toml`. Gradle configuration cache and
build cache are enabled; Gradle uses 4 GB and the Kotlin daemon uses 3 GB.

## 3. Modules and Dependency Direction

```text
:core <── :app:shared <── :app:desktopApp
   ▲             ├─────── :app:webApp
   │             ├─────── :app:androidApp
   │             └─────── app/iosApp (Xcode host)
   └──── :server
```

| Module | Current responsibility |
|---|---|
| `:core` | Pure multiplatform greeting utility, DSA domain, trace engine, algorithms, repository, and use cases |
| `:app:shared` | Shared Compose greeting UI plus platform `expect`/`actual` implementations |
| `:app:androidApp` | Android launcher and the Android Basics demo catalog; depends on `:app:shared` but does not currently render `App()` |
| `:app:desktopApp` | JVM window hosting shared `App()` |
| `:app:webApp` | JS and Wasm browser entry point hosting shared `App()` |
| `app/iosApp` | Swift/Xcode host for the static `Shared` framework and `MainViewController()` |
| `:server` | Ktor/Netty JVM server depending on `:core`; currently returns `Hello, Ktor!` |

`:core` must remain free of Compose, Android, Ktor, Spring, and other platform-specific APIs.
Dependencies point inward to `:core`; never make `:core` depend on an adapter module.

## 4. Source Layout

```text
core/src/commonMain/.../
├── GreetingUtil.kt
└── dsa/
    ├── algorithm/     # Pure algorithms with optional tracing
    ├── data/          # In-memory ChallengeRepository adapter
    ├── domain/        # Challenge models and repository port
    ├── trace/         # Trace, AlgoStep, Metrics, Tracer implementations
    └── usecase/       # ListChallenges, GetChallenge, GenerateTrace

app/shared/src/
├── commonMain/        # Shared App(), Greeting, Platform contract, resources
├── androidMain/       # Android actual
├── iosMain/           # iOS actual and Compose UIViewController
├── jvmMain/           # Desktop actual
├── jsMain/            # Browser actual
└── wasmJsMain/        # Wasm actual

app/androidApp/src/main/.../
├── MainActivity.kt    # Table of contents, one section per course
├── common/            # BasicsTopic registry (+ Course), theme, shared demo scaffold
├── lifecycle/
├── launchmodes/
├── viewmodels/
├── contextdemo/
├── resourcesdemo/
├── intents/
├── broadcastreceiver/
├── foregroundservices/
├── workmanager/
├── uris/
├── contentproviders/
├── osarchitecture/       # Android Internals §2 (template — in progress)
├── activitiesinternals/  # Android Internals §3 (template)
├── viewmodelinternals/   # Android Internals §4 (template)
├── viewsystem/           # Android Internals §5 (template)
├── ipc/                  # Android Internals §6 (template)
├── security/             # Android Internals §7 (template)
├── memory/               # Android Internals §8 (template)
└── battery/              # Android Internals §9 (template)
```

Only the root `AGENTS.md` currently exists, so these instructions apply to the entire repository.

## 5. DSA and Trace Conventions

- Follow `TwoSumSorted` and `TwoSumSortedTest` as the canonical algorithm slice.
- Algorithms remain deterministic and UI-free.
- Accept `Tracer = NoOpTracer` when an algorithm supports visualization.
- Emit snapshots through `Tracer.step`; update comparison/read/swap counters before the step that
  should display those metrics.
- Keep use cases thin. They select or orchestrate domain behavior; rendering and transport logic
  stay in adapters.
- Unknown challenge IDs currently return `null` from `GetChallenge` and `Trace.EMPTY` from
  `GenerateTrace`; preserve that contract unless the task explicitly changes it.
- Put multiplatform tests in `core/src/commonTest`; avoid spaces in Kotlin test function names
  because JS/Wasm test targets do not support them reliably.

For a new challenge:

1. Add the algorithm under `dsa/algorithm`.
2. Add or extend its domain metadata.
3. Register it in `InMemoryChallengeRepository`.
4. Wire trace generation in `GenerateTrace` or introduce a registry when that is the requested
   scope.
5. Add algorithm and use-case coverage in `commonTest`.

## 6. Android Demo Conventions

The catalog covers more than one PL-Coding course. `BasicsTopic.Course` (`BASICS`, `INTERNALS`)
groups the entries; `MainActivity` renders one section per course in enum declaration order.

- Each topic is a focused `ComponentActivity` with Compose content.
- Reuse `PlaygroundTheme` and `DemoScaffold`.
- Add a topic to `BasicsTopic.all` and declare every Activity/service/provider/receiver in
  `AndroidManifest.xml`.
- `BasicsTopic.number` is the number **within its course**, so it is not unique across the catalog;
  always set `course` for anything outside Android Basics 2023.
- Keep topic numbering aligned across `BasicsTopic`, KDoc, UI titles, and manifest comments.
- Prefer Activity Result APIs, lifecycle-aware state collection, coroutines/Flow, and explicit
  intents where they reinforce the lesson.
- Android-specific code stays in `:app:androidApp`; do not move framework demonstrations into
  `:core`.
- Keep providers and receivers non-exported unless cross-app access is the lesson and the exposure
  is protected with the narrowest suitable permission or URI grant.
- For file sharing, use `FileProvider` and temporary URI permissions. Never expose raw private file
  paths or make a provider broadly exported.

Current caveats to preserve or fix intentionally:

- Topic 8 is incomplete: `RunningService.formatElapsed` is a `TODO`, so START reaches a runtime
  failure while building the first notification.
- Topic 9's photo-compression worker/UI is partial and relies on receiving an `ACTION_SEND` image
  intent; the manifest does not currently expose an intent filter for that flow.
- Every Android Internals section (§3–§9) is a template Activity showing a placeholder; Vini fills
  each one in as he watches the corresponding video. §2 (`osarchitecture/`) covers 2 of its 11
  lessons (Main Thread/Looper/MessageQueue and Handlers); the other 9 (OS job, architecture overview,
  Linux kernel, processes, Zygote, process lifecycle, system services, Dalvik/ART, native code/JNI)
  are still unimplemented, so the topic stays `Status.TEMPLATE` overall. §2's implemented lessons
  deliberately mirror Philipp Lackner's reference repo
  (github.com/philipplackner/AndroidInternals, branch `system-architecture/thread-looper`) — see
  `OsArchitectureActivity`'s KDoc — rather than an independently designed UI; when implementing a
  new Internals lesson, check the matching branch/repo in `pl-coding-map.md` (Akira vault) first and
  track its actual demonstrated behavior instead of inventing new UI. Do not "finish" any Internals
  section as part of unrelated work — they are deliberate learning exercises.
- There are no Android unit or instrumentation tests in the current tree.
- Topic 10 depends on the bundled `sample_photo.png`, its `FileProvider`, and
  `res/xml/file_paths.xml`; keep those pieces aligned when changing the URI lesson.

Do not silently “finish” these lessons during unrelated work.

## 7. Build, Run, and Test

Run commands from the repository root.

| Goal | Command |
|---|---|
| Inspect modules | `./gradlew projects` |
| Android debug APK | `./gradlew :app:androidApp:assembleDebug` |
| Desktop app | `./gradlew :app:desktopApp:run` |
| Web (Wasm) | `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun` |
| Web (JS) | `./gradlew :app:webApp:jsBrowserDevelopmentRun` |
| Ktor server | `./gradlew :server:run` |
| Core JVM tests | `./gradlew :core:jvmTest` |
| Shared JVM tests | `./gradlew :app:shared:jvmTest` |
| Server tests | `./gradlew :server:test` |
| All configured checks | `./gradlew check` |
| iOS | Open `app/iosApp` in Xcode and run the `iosApp` scheme |

A useful host-side verification set is:

```bash
./gradlew \
  :core:jvmTest \
  :server:test \
  :app:shared:jvmTest \
  :app:androidApp:assembleDebug \
  :app:desktopApp:compileKotlin \
  :app:webApp:compileKotlinWasmJs
```

Use `<module>:tasks --all` before guessing a target-specific task. Compilation or assembly success
is not test success; report exactly which tasks completed. Browser, device, and iOS tests may need
target-specific local tooling.

## 8. Coding and Testing Rules

- Use official idiomatic Kotlin and preserve existing package/naming conventions.
- Prefer self-documenting code and minimal comments, but retain lesson KDoc and explanatory comments
  unless the task changes the concept they document.
- Use explicit errors (`Result`/sealed outcomes) when adding fallible domain behavior; do not add an
  error abstraction to code that cannot fail.
- Add no framework, architecture layer, compatibility shim, or abstraction beyond the requested
  scope.
- For complex UI, prefer MVI; for simple lessons/screens, MVVM or local Compose state is sufficient.
- Design new behavior for testing. Use `kotlin.test` in multiplatform code and focused Android tests
  for Android framework behavior.
- Check performance and OWASP-relevant security concerns at platform boundaries: intents,
  providers, file/URI grants, deep links, exported components, backend input, and sensitive logs.
- Do not refactor unrelated code while fixing a bug.

## 9. Working Safely in This Repository

Before editing:

1. Read the relevant source and module build file.
2. Run `git status --short`.
3. Treat all existing staged, unstaged, and untracked files as user work.

While editing:

- Keep changes within the requested files and modules.
- Do not overwrite, restore, stage, or format unrelated changes.
- Preserve current documentation/KDoc unless directly affected.
- Never edit generated Compose resources or build output.

Before handing off:

1. Run the narrowest relevant tests plus compilation/assembly for affected targets.
2. Inspect `git diff --check` and the scoped diff.
3. Explain what changed, what was verified, and any remaining limitation.
4. Do not commit or push unless Vini explicitly asks. If the result is ready to publish, offer that
   as the next step.

## 10. Roadmap vs. Implemented Code

The intended direction is a Compose Multiplatform DSA gallery backed by the same pure use cases
through UI, REST, and MCP adapters. Planned items include the visualizer/timeline, Navigation,
Koin, MVI presentation, additional algorithms, a richer backend, Spring AI/MCP, Android App
Functions, and a GitHub Pages Wasm deployment.

Treat roadmap language as design intent only. Before using a planned technology, verify that the
task requires it and add it deliberately rather than coding as if it already exists.

## 11. Repository References

- Project overview: `README.md` (may lag behind build files)
- Version catalog: `gradle/libs.versions.toml`
- Module graph: `settings.gradle.kts`
- License: `LICENSE`
- Broader personal workflow context, when available: `~/Akira/vault/_system/akira/workflows/init.md`
