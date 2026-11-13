# Walkthrough

1. Start with `Incident.kt`: Kotlin data classes, enums, immutable copies, and deterministic sorting.
2. Trace `MainActivity.onCreate`: load bundled/cache data immediately, render cards, then optionally refresh asynchronously.
3. Follow a status button: write the local override, copy the incident with its next status, re-render. Resolved is terminal.
4. Inspect `IncidentRepository.refresh`: HTTPS-only endpoint, finite timeouts, bounded response, validation before cache replacement, disconnect in finally.
5. Inspect lifecycle handling: save the severity filter in the instance-state bundle; statuses outlive the Activity in app-private preferences; ignore background results after destruction.
6. Run tests and deliberately break severity ordering or JSON validation to see meaningful failures.

## Honest limitations and next steps

- Native Views demo, no Compose, Bluetooth integration, foreground service, authentication, or production backend.
- No real-world field evaluation, safety certification, or customer deployment.
- Cache and preferences are intentionally small. A larger product would use a database, structured concurrency/ViewModel, dependency injection, richer accessibility testing, and account-aware synchronization.
- Automated device tests use bundled fixtures for deterministic offline behavior. Manual refresh uses the hosted fixture endpoint.

Before discussing this in an interview, build it yourself, explain each layer, change a feature, and run the tests. Suggested exercise: add a resolved-only filter and test it, then replace the static fixture feed with your own authenticated backend.
