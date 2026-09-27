# AI Hub PC (Desktop)
A Kotlin/JVM desktop port of AI Hub for Windows/Linux/Mac.

This is a fork of [SilentCoderHere/aihub](https://github.com/SilentCoderHere/aihub)
configured for desktop use with Compose for Desktop.

## Features
- Tabbed browsing of AI assistants
- Persistent sessions
- Material Design 3
- Desktop mode (full desktop user agent)
- Auto-sync with upstream repository
- Built-in release automation via GitHub Actions

## Requirements
- JDK 17+
- Gradle wrapper included

## Build
```
./gradlew desktopBuild
```

## Syncing with Upstream
A GitHub Action (`sync.yml`) automatically merges changes from
`SilentCoderHere/aihub` into this fork every 6 hours.