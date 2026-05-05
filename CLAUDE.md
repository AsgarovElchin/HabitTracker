# Habit Tracker

Single-module Jetpack Compose app · `com.plcoding.habittracker`

## Resources

| What | Where |
|------|-------|
| Requirements | `specs/habit-tracker-design.md` — read when you need project rules or behavior details |
| UI mockups | `specs/Habit Tracker — Design System & Screens.html` — read only the section for the screen you're building |
| Fonts | `specs/Fonts/` — Inter and Manrope variable fonts |

Always lazy-load from specs when needed. Do not inline requirements or design details into code.

## Skills

Load the matching skill before working on each layer.

| Layer | Skill |
|-------|-------|
| Presentation / MVI | `android-presentation-mvi` |
| Compose screen architecture | `android-compose-architecture` |
| Compose UI components | `android-compose-components` |
| Data layer (repos, data sources) | `android-data-layer` |
| Dependency injection (Koin) | `android-di-koin` |
| Navigation | `android-navigation` |
| Error handling / Result types | `android-error-handling` |
| Background work / WorkManager | `android-background` |
| Coroutines | `kotlin-coroutines` |
| Flows | `kotlin-flows` |
| Version catalog / Gradle | `android-version-catalog` |
| Testing | `android-testing` |

## Rules

- **Single module, layered packages.** Feature code lives under `com.plcoding.habittracker.feature.<name>/{data,domain,presentation}`; shared code under `core/`.
- **No tests unless asked.**
- **Git hygiene.** Run `git add <file>` immediately after creating every new file — no exceptions. Create meaningful, modular commits at logical checkpoints (e.g. after completing a screen, a data layer change, or a DI wiring); never batch unrelated changes into one commit.
