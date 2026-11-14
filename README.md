# Kollavarsham Kotlin SDK

A Kotlin SDK for the Malayalam (Kollavarsham) calendar. It converts between Gregorian and Malayalam dates following the Kerala solar-month rule, works offline, is thread-safe, and keeps Android out of the core.

> **Status: Sprint 1 (core SDK foundation) complete. Not yet released.**
> The models, validation, conversion mechanics, arithmetic and names are implemented and tested. The
> astronomical engine that computes real month boundaries, along with `KollavarshamCalendar.create()`,
> arrives in Sprint 2.

## Target usage

```kotlin
val calendar = KollavarshamCalendar.create()                               // Sprint 2

calendar.fromGregorian(LocalDate.of(2026, 11, 14))                         // 1202 Thulam 28
calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))    // 2026-11-17
calendar.validate(MalayalamDate(1202, MalayalamMonth.Thulam, 31))          // DayOutOfRange(31, 30)

MalayalamMonth.Vrischikam.displayName(CalendarLocale.MALAYALAM)            // "വൃശ്ചികം"
```

The expected dates above come from the Sprint 0 accuracy spike. They are not yet produced by this SDK.

## Modules

| Module | Purpose |
|---|---|
| `kollavarsham-astronomy` | Sun, ΔT, sunrise and sankranti (Sprint 2) |
| `kollavarsham-core` | Models, conversion, validation, arithmetic |
| `kollavarsham-locale` | Malayalam and English names; formatting and numerals in Sprint 3 |

## Requirements

- JVM 11+ bytecode.
- Android: minSdk 26, or lower with [core library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) for `java.time`.

## Build

```bash
./gradlew build
```

## Documentation

| Document | Contents |
|---|---|
| [API_REFERENCE.md](API_REFERENCE.md) | Public API with examples |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Modules and principles |
| [CALENDAR_ALGORITHM.md](CALENDAR_ALGORITHM.md) | Calendar rules and computation |
| [VALIDATION_STRATEGY.md](VALIDATION_STRATEGY.md) | How accuracy is established |
| [SPRINT_PLAN.md](SPRINT_PLAN.md) | Sprints, risks, open questions, reports |
| [docs/sprint-0/](docs/sprint-0/) | Library research and API proposal |
| [docs/adr/](docs/adr/) | Architecture decision records |
| [CONTRIBUTING.md](CONTRIBUTING.md), [CHANGELOG.md](CHANGELOG.md), [ACKNOWLEDGEMENTS.md](ACKNOWLEDGEMENTS.md) | |

## License

[Apache License 2.0](LICENSE)
