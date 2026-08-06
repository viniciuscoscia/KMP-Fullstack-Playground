# Implementation Plan

## Architecture

- `:features:substanceCatalog:contract` owns wire models and enums.
- `:features:substanceCatalog:client` owns Ktor-based repository adapters and a Swift-friendly facade.
- `:app:shared` owns adaptive Compose presentation and state.
- `app/iosApp` owns native SwiftUI presentation.
- `:server` is a Spring Boot modular monolith with `catalog`, `research`, `reporting`, and `assistant` application modules.
- PostgreSQL/Flyway owns durable state; no in-memory production fallback exists.

## Delivery sequence

1. Freeze contracts and synthetic fixtures.
2. Add modules and compile all KMP targets.
3. Build Spring/PostgreSQL walking skeleton and REST contract tests.
4. Add research job orchestration, review/versioning, and deterministic source adapters.
5. Add Compose MVI-Lite catalog plus simple MVVM dashboard/detail screens.
6. Add native SwiftUI shell and repository facade integration.
7. Add MCP and workbook export.
8. Run the private import, Docker smoke, accessibility, and security gates.

## Runtime profiles

- `test`: H2/PostgreSQL-compatible tests and synthetic fixtures; no network.
- `local`: PostgreSQL, optional local template, source adapters disabled unless explicitly enabled.
- `docker`: PostgreSQL service, loopback server, same-origin web proxy.
