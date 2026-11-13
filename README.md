# FieldBrief — Kotlin Android training demo

A native Android incident briefing board inspired by the need for clear, reliable field interfaces. All incidents and locations are fictional. This is a portfolio exercise, not a dispatch system or a Motorola product.

## Try it

Download the installable demo APK from [Releases](https://github.com/gvkishorereddy/fieldbrief-android/releases). Android 8.0 (API 26) or later. This is a debug-signed demo distributed through GitHub, not Google Play. No account or sensitive permissions required. Uninstall to clear locally saved data.

## Features

- Kotlin and native Android Views; priority-ordered incident cards with severity filters.
- HTTPS JSON fixture feed, loaded off the UI thread with timeout and payload limits.
- Bundled first-launch data and a validated private cache for offline operation.
- Local New → Acknowledged → En route → Resolved workflow, persisted across recreation and refresh.
- Visible loading, offline, and data-source states; content descriptions and scalable text.
- No location, microphone, Bluetooth, or account access. Cleartext HTTP disabled; backup disabled. Only the Internet permission is requested.

## Architecture

`Incident.kt` is a platform-independent domain model, sort/filter policy, and status merge. `IncidentJson.kt` validates feed boundaries, identifiers, required fields, and severities. `IncidentRepository.kt` owns HTTPS transport, cached fixture data, and SharedPreferences status overrides. `MainActivity.kt` renders native Views and marshals background results to the UI thread. A single background executor is shut down with the Activity. A late refresh re-reads local statuses so it cannot revert an acknowledgement.

The hosted endpoint is static JSON on GitHub, not a production dispatch backend. Refresh downloads that fixture over HTTPS. Status changes stay on the device and are never uploaded. Private app storage is not an encrypted vault; no sensitive data should be used.

## Build and verify

Open in Android Studio with JDK 17 and Android SDK 35. The build pins AGP 8.9.2, Kotlin 2.1.20, and Gradle 8.11.1.

```sh
gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
gradle :app:connectedDebugAndroidTest
```

CI runs JVM tests and Android lint, builds an APK, then exercises an API 35 emulator. Tests cover priority ordering, filtering, terminal status behavior, override merging, invalid feeds, offline launch, and state preservation through Activity recreation. CI artifacts include reports and an emulator screenshot.

## Development provenance

Created with Codex assistance as a new Kotlin learning and portfolio project. Generated implementation and tests are available for review in this repository; this project does not imply prior professional Kotlin experience or deployment to public safety customers. See [LEARNING.md](LEARNING.md) for an implementation walkthrough and extension ideas.
