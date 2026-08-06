# Local Quickstart

## Prerequisites

- Java 21, Xcode 26+, Android SDK, and Docker Desktop.
- No OpenAI key is required. Runtime AI remains disabled.

## Start the stack

```bash
cp .env.example .env
docker --context desktop-linux compose up --build
```

Open the web service through the Docker runtime URL. With OrbStack, use `http://substance-atlas-web-1.orb.local:8081`. The backend is available inside the Compose network as `http://server:8080`, and through OrbStack as `http://substance-atlas-server-1.orb.local:8080`.

The Compose file still declares loopback host ports for runtimes that publish them directly:

- PostgreSQL: `127.0.0.1:${SUBSTANCE_ATLAS_POSTGRES_PORT:-15432}`
- API: `127.0.0.1:${SUBSTANCE_ATLAS_SERVER_PORT:-18080}`
- Web: `127.0.0.1:${SUBSTANCE_ATLAS_WEB_PORT:-18081}`

## Native clients

```bash
./gradlew :app:desktopApp:run
./gradlew :app:androidApp:assembleDebug
open app/iosApp/iosApp.xcodeproj
```

Android Emulator resolves the host API through `http://10.0.2.2:8080` when the backend is run directly on the host. Desktop and iOS Simulator use `http://127.0.0.1:8080` for direct host runs, or the runtime-specific container URL when using Docker.

## Private inputs

Place the marketplace export at `.local/import/marketplace.txt` and the proprietary workbook at `.local/templates/substance-atlas-dashboard.xlsx`. Neither path is tracked by Git.
