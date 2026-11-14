# Kollavarsham Kotlin SDK

A Kotlin SDK for the Malayalam (Kollavarsham) calendar. It converts between Gregorian and Malayalam dates following the Kerala solar-month rule, works offline, is thread-safe, and keeps Android out of the core.

> **Status: `0.1.0-alpha01` is ready for release** (the first preview; see the [release notes](docs/release-notes/0.1.0-alpha01.md)).
> It appears on Maven Central once the first release has been published ([RELEASING.md](RELEASING.md)).
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

// Panchangam (kollavarsham-panchangam)
val day = Panchangam.create().day(LocalDate.of(2025, 9, 5))
day.nakshatraAtSunrise.displayName(CalendarLocale.MALAYALAM)              // "തിരുവോണം" (Onam)
day.tithiAtSunrise                                                         // Tithi at sunrise

// Festivals (kollavarsham-festivals): rules, never stored dates
FestivalCalendar.create().dateOf(KeralaFestivals.ONAM, 1202)               // 2026-08-26

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
| `kollavarsham-panchangam` | Nakshatra, tithi, sunrise/sunset for a location; daily panchangam |
| `kollavarsham-festivals` | Kerala festivals as rules (Onam, Vishu, Sivaratri, …); pluggable observance sources |
| `kollavarsham-android` | Optional Jetpack Compose month calendar (`KollavarshamMonthView`) and `MonthGrid` model |
| `sample-app` | Demo Android app: monthly calendar, daily details, festivals; offline (not published) |

## Installation

Add **one** dependency; the modules it needs come with it automatically.

```kotlin
// build.gradle.kts
repositories { mavenCentral() }

dependencies {
    // Everything except UI: conversion, names and formatting, panchangam, festivals.
    implementation("io.github.anjalymsajeev:kollavarsham-festivals:0.1.0-alpha01")
}
```

If you need less (or more), use one of these instead:

| You need | Add | It brings |
|---|---|---|
| Date conversion only | `kollavarsham-core` | core, astronomy |
| + Malayalam names, numerals, formatting | `kollavarsham-locale` | + core |
| + nakshatra, tithi, sunrise/sunset | `kollavarsham-panchangam` | + locale, core |
| + festivals | `kollavarsham-festivals` | + panchangam, locale, core |
| Compose month calendar (Android) | `kollavarsham-android` *in addition* | + locale, core, Compose |

An Android app that wants everything, including the calendar view, needs just two lines:
`kollavarsham-festivals` and `kollavarsham-android`.

`0.1.0-alpha01` is an early preview: the API may change before 1.0. It becomes available once the first release has been published (see [RELEASING.md](RELEASING.md)).

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
| [docs/adr/](docs/adr/) | Architecture decision records (0007: API stability policy) |
| [docs/release-notes/](docs/release-notes/) | Release notes |
| [RELEASING.md](RELEASING.md) | How to publish to Maven Central |
| [CONTRIBUTING.md](CONTRIBUTING.md), [CHANGELOG.md](CHANGELOG.md), [ACKNOWLEDGEMENTS.md](ACKNOWLEDGEMENTS.md) | |

## Contributors

- [@anjalymsajeev](https://github.com/anjalymsajeev): project owner and maintainer
- Claude (Anthropic): co-author

Contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[Apache License 2.0](LICENSE)
