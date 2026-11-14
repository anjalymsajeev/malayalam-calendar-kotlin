# Kollavarsham Kotlin SDK

A Kotlin SDK for the Malayalam (Kollavarsham) calendar. It converts between Gregorian and Malayalam dates following the Kerala solar-month rule, works offline, is thread-safe, and keeps Android out of the core.

> **Status: Sprint 3 (localisation and developer experience) complete. Not yet released (`0.1.0-alpha01`).**
> Real conversion works for 1900–2100. It reproduces all 21 cited month starts in the reference dataset;
> see [VALIDATION_STRATEGY.md](VALIDATION_STRATEGY.md) for what that evidence does and doesn't prove.

## Usage

```kotlin
val calendar = KollavarshamCalendar.create()

calendar.fromGregorian(LocalDate.of(2026, 11, 14))                         // 1202 Thulam 28
calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))    // 2026-11-17
calendar.validate(MalayalamDate(1202, MalayalamMonth.Thulam, 31))          // DayOutOfRange(31, 30)

MalayalamMonth.Vrischikam.displayName(CalendarLocale.MALAYALAM)            // "വൃശ്ചികം"

LocalDate.of(2026, 11, 14).toMalayalamDate()                               // 1202 Thulam 28
    .format(CalendarLocale.MALAYALAM, DateStyle.FULL, calendar)            // "കൊല്ലവർഷം 1202 തുലാം 28, ശനി"

// Months where almanacs may disagree by a day:
val b = calendar.monthBoundary(1199, MalayalamMonth.Chingam)
b.cutoffMargin                                                             // ~ +1 minute: borderline
```

## Accuracy

- Month starts follow Kerala's rule. Day 1 is the day of the sidereal sankranti if it falls before 3/5 of the daytime at Thiruvananthapuram; otherwise it is the next day.
- The Sun's position is accurate to about 1″, roughly 25 s at a sankranti.
- 30 months between 1900 and 2100 fall within 10 minutes of the cut-off. Almanacs may differ by a day on these; `MonthBoundary.cutoffMargin` flags them.
- **We don't claim 100 % accuracy.** Details are in [CALENDAR_ALGORITHM.md](CALENDAR_ALGORITHM.md) §7.

## Modules

| Module | Purpose |
|---|---|
| `kollavarsham-astronomy` | Sun (VSOP87), ΔT, sunrise and sankranti (opt-in API) |
| `kollavarsham-core` | Models, conversion, validation, arithmetic |
| `kollavarsham-locale` | Names, ISO 15919 transliteration, Malayalam numerals, date formatting |

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
| [docs/guide.md](docs/guide.md) | Developer guide: using the SDK in an app |
| [API_REFERENCE.md](API_REFERENCE.md) | Public API with examples |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Modules and principles |
| [CALENDAR_ALGORITHM.md](CALENDAR_ALGORITHM.md) | Calendar rules and computation |
| [VALIDATION_STRATEGY.md](VALIDATION_STRATEGY.md) | How accuracy is established |
| [SPRINT_PLAN.md](SPRINT_PLAN.md) | Sprints, risks, open questions, reports |
| [docs/sprint-0/](docs/sprint-0/) | Library research and API proposal |
| [docs/adr/](docs/adr/) | Architecture decision records |
| [CONTRIBUTING.md](CONTRIBUTING.md), [CHANGELOG.md](CHANGELOG.md), [ACKNOWLEDGEMENTS.md](ACKNOWLEDGEMENTS.md) | |

## Contributors

- [@anjalymsajeev](https://github.com/anjalymsajeev): project owner and maintainer
- Claude (Anthropic): co-author

Contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[Apache License 2.0](LICENSE)
