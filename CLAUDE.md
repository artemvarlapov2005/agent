# CLAUDE.md

This file provides guidance for AI assistants working in this repository.

## Project Overview

This is a **WireGuard/AmneziaWG VPN Agent** — a lightweight HTTP service built with Kotlin and the Kora microframework. It manages VPN interfaces, registers clients with dynamically allocated IPs, and controls systemd services on Linux hosts.

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9.25 (JVM 21) |
| Build | Gradle 8.8 (Kotlin DSL) |
| Framework | Kora 1.2.5 (Tinkoff) |
| HTTP Server | Kora + Undertow |
| Config | HOCON (application.conf) |
| Caching | Caffeine (via Kora) |
| Serialization | Apache Avro 1.11.3, Kora JSON |
| Crypto | BouncyCastle 1.78 (X25519) |
| Logging | Logback 1.4.8 |

## Repository Structure

```
.
├── build.gradle.kts          # Gradle build with Kotlin DSL
├── settings.gradle.kts       # Project name and settings
├── gradle.properties         # Kotlin code style = official
├── gradlew / gradlew.bat     # Gradle wrapper
├── gradle/wrapper/           # Gradle wrapper binaries
└── src/main/
    ├── kotlin/               # All Kotlin source files
    │   ├── Main.kt           # Application entry point
    │   ├── controller/
    │   │   ├── AgentController.kt    # HTTP endpoints
    │   │   └── ErrorInterceptor.kt   # Global error → HTTP status mapping
    │   ├── action/
    │   │   ├── PutClientAction.kt    # Client registration business logic
    │   │   └── PutConfigAction.kt    # Config update business logic
    │   ├── service/
    │   │   └── InterfaceStore.kt     # Config storage, lifecycle hooks
    │   └── shared/
    │       ├── AgentData.kt          # Config source interface
    │       ├── Dtos.kt               # ClientResult DTO
    │       ├── IpAllocator.kt        # CIDR-aware IP allocation
    │       ├── Keys.kt               # X25519 key derivation
    │       └── OS.kt                 # OS commands (systemctl, apt)
    └── resources/
        └── application.conf          # HOCON runtime config
```

## Architecture

The project follows a **layered architecture** with constructor-based dependency injection provided by Kora's KSP annotation processor.

```
HTTP Request
    ↓
AgentController  (HTTP routing, @HttpController)
    ↓
PutClientAction / PutConfigAction  (@Component, business logic)
    ↓
InterfaceStore   (@Component, stateful config storage)
    ↓
OS / IpAllocator / Keys  (shared utilities)
```

### Layers

- **Controllers** — map HTTP verbs/paths to actions, validate raw input.
- **Actions** — orchestrate business logic; each handles one use-case.
- **Services** — stateful, long-lived components with lifecycle methods (`@KoraApp` init).
- **Shared** — pure utility functions and data classes, no DI annotations.

### Error Handling

`ErrorInterceptor` maps exceptions to HTTP status codes:

| Exception | HTTP Status |
|-----------|-------------|
| `IllegalArgumentException` | 400 Bad Request |
| `IllegalStateException` | 422 Unprocessable Entity |
| `TimeoutException` | 408 Request Timeout |
| Service unavailable (IP exhaustion) | 503 |

## REST API

### `PUT /client`

Registers a new VPN client.

**Request body** (JSON):
```json
{
  "interfaceName": "<wg interface>",
  "publicKey": "<44-char base64 WireGuard public key>"
}
```

**Response** (`ClientResult`):
```json
{
  "assignedIp": "10.x.x.x/32",
  "serverPublicKey": "<server public key>",
  "endpoint": "..."
}
```

### `POST /config`

Replaces the configuration of an existing interface and reloads it.

**Request body**: raw config file content (text).

## Build & Run

### Build

```bash
./gradlew build        # Compile + run tests + create fatJar and distTar
./gradlew fatJar       # Build standalone jar: build/libs/application-standalone.jar
```

The `fatJar` task creates a self-contained executable jar with all dependencies bundled.

### Run

```bash
java -jar build/libs/application-standalone.jar
```

### Environment Variables (Required)

| Variable | Description |
|----------|-------------|
| `CONFIG_FILE_FOLDER` | Directory path for WireGuard config files |
| `SERVER_MASTER_KEY` | Master password used to decrypt config files |
| `INTERFACES_NAMES` | Comma-separated list of WireGuard interface names |

These are mapped in `src/main/resources/application.conf`.

## Code Conventions

### Naming

- **Classes**: PascalCase (`PutClientAction`, `InterfaceStore`)
- **Functions**: camelCase (`findFreeIp`, `getPublic`)
- **Class names** reflect their single responsibility clearly (prefer `PutClientAction` over `ClientService`)

### Kotlin Idioms

- Prefer extension functions for domain operations (e.g., `ConfigFile.privateKey()`, `IpAddress.ipToInt()`)
- Use `runCatching` for operations that may fail with a recoverable error
- Use `data class` for DTOs and value objects
- Use `ConcurrentHashMap` for shared mutable state in services

### Dependency Injection

This project uses **Kora's compile-time DI** via KSP. Key annotations:

- `@Component` — marks a class as a DI-managed bean
- `@HttpController` — marks a Kora HTTP controller
- `@HttpRoute` — defines path and HTTP method on controller methods

Do **not** use Spring or Guice annotations — Kora has its own annotation set.

### Logging

Use `KoraLogger` (or Logback-backed logger). Existing log messages are in Russian — maintain consistency with the existing language in log statements within a given file.

## External Dependency: AWG-API

The project depends on `org.matkini:awg-api:1.0-SNAPSHOT` (local Maven). This provides:

- `ConfigFile` — WireGuard config file model
- `IpAddress` — IP address with CIDR support
- `PeerSection` — Peer configuration section
- `Reader` / `Writer` — Encrypted file I/O
- `toDecoded()` — Decryption extension function

This library is not in the repository; it must be available in the local Maven cache (`~/.m2`).

## Testing

There are currently **no tests** in the repository (`src/test` does not exist). When adding tests:

- Place them under `src/test/kotlin/`
- Use JUnit 5 (add dependency to `build.gradle.kts`)
- Mirror the `src/main/kotlin` package structure

## Key Implementation Details

### IP Allocation (`IpAllocator.kt`)

Converts the WireGuard subnet (e.g., `10.0.0.0/24`) to integer range and finds the first IP not already in use by existing peers.

### Config Reload (`PutConfigAction.kt`)

After writing a new config file:
1. Run `awg-quick down <interface>`
2. Run `awg-quick up <interface>`
3. On failure: write back the previous config and restart (rollback)

### System Initialization (`InterfaceStore.kt`)

On startup:
1. Ensure AmneziaWG packages are installed (`OS.installPackages()`)
2. Read all config files from `CONFIG_FILE_FOLDER`
3. Bring up all configured interfaces via `awg-quick up`

### Cryptography (`Keys.kt`)

Uses BouncyCastle X25519 to derive the server public key from the private key stored in config files.

## Git Workflow

- Active development branch: `claude/add-claude-documentation-JWpOr`
- Main branch: `master` / `main`
- Commit messages should be clear and descriptive in English
