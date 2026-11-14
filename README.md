# Kollavarsham Kotlin SDK

[![CI](https://github.com/anjalymsajeev/malayalam-calendar-kotlin/actions/workflows/ci.yml/badge.svg)](https://github.com/anjalymsajeev/malayalam-calendar-kotlin/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.anjalymsajeev/kollavarsham-core?label=Maven%20Central)](https://central.sonatype.com/namespace/io.github.anjalymsajeev)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/android-minSdk%2021-3DDC84.svg?logo=android)](kollavarsham-android)

A Kotlin library for the **Malayalam (Kollavarsham) calendar**: Gregorian ↔ Malayalam date conversion, Malayalam names and numerals, panchangam (nakshatra, tithi, sunrise/sunset) and Kerala festivals. It works offline, is thread-safe, and runs on any JVM or Android.

```kotlin
KollavarshamCalendar.create().fromGregorian(LocalDate.of(2026, 11, 14))   // 1202 Thulam 28
```

> **Status: preview (`0.1.0-alpha01`).** The API may change before 1.0. See the [release notes](docs/release-notes/0.1.0-alpha01.md) and [known limitations](#accuracy-and-limitations).

## Contents

- [Features](#features)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Modules](#modules)
- [Accuracy and limitations](#accuracy-and-limitations)
- [Requirements](#requirements)
- [Documentation](#documentation)
- [Building from source](#building-from-source)
- [Contributing](#contributing)
- [Versioning](#versioning)
- [License](#license)
- [Acknowledgements](#acknowledgements)

## Features

- **Date conversion** in both directions, computed from astronomy with Kerala's month-start rule. There are no lookup tables and no fixed year offsets.
- **Month structure:** month lengths (29–32 days), boundaries, and date arithmetic.
- **Validation:** invalid dates are rejected with clear errors, never rolled over silently.
- **Malayalam and English names**, ISO 15919 transliteration, and Malayalam numerals (൧൨൦൨).
- **Formatting:** preset styles and custom patterns, such as `കൊല്ലവർഷം 1202 തുലാം 28, ശനി`.
- **Panchangam:** nakshatra, tithi and paksha with start and end times, and sunrise and sunset for any location.
- **Festivals:** 25 Kerala festivals defined as rules (Onam, Vishu, Sivaratri, Thrissur Pooram, …), plus your own rules and dated lists.
- **Android:** an optional Jetpack Compose month calendar.
- **Offline and lightweight:** the core modules depend only on the Kotlin standard library.

## Installation

The library is published to Maven Central under the group `io.github.anjalymsajeev`.

> The first release is prepared but not yet published. Until it is, use `./gradlew publishToMavenLocal` and `mavenLocal()` (see [Building from source](#building-from-source)).

Add **one** dependency. The modules it needs are included automatically.

```kotlin
// build.gradle.kts
repositories {
    mavenCentral()
}

dependencies {
    // Conversion, names, formatting, panchangam and festivals
    implementation("io.github.anjalymsajeev:kollavarsham-festivals:0.1.0-alpha01")

    // Optional, Android only: Compose month calendar
    implementation("io.github.anjalymsajeev:kollavarsham-android:0.1.0-alpha01")
}
```

To keep your app smaller, depend on a lower module instead:

| You need | Dependency | Includes |
|---|---|---|
| Date conversion only | `kollavarsham-core` | core, astronomy |
| + names, numerals, formatting | `kollavarsham-locale` | core |
| + nakshatra, tithi, sunrise/sunset | `kollavarsham-panchangam` | locale, core |
| + festivals | `kollavarsham-festivals` | panchangam, locale, core |
| Compose month calendar | `kollavarsham-android` (in addition) | locale, core, Compose |

## Quick start

### Convert dates

```kotlin
val calendar = KollavarshamCalendar.create()

calendar.fromGregorian(LocalDate.of(2026, 11, 14))                        // MalayalamDate(1202, Thulam, 28)
calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))   // 2026-11-17
calendar.monthLength(1202, MalayalamMonth.Thulam)                         // 30

calendar.validate(MalayalamDate(1202, MalayalamMonth.Thulam, 31))         // DayOutOfRange(day=31, monthLength=30)
```

### Format in Malayalam or English

```kotlin
val date = LocalDate.of(2026, 11, 14).toMalayalamDate()

date.format(CalendarLocale.MALAYALAM, DateStyle.FULL, calendar)            // "കൊല്ലവർഷം 1202 തുലാം 28, ശനി"
date.format(CalendarLocale.ENGLISH, DateStyle.LONG)                        // "28 Thulam 1202 ME"
date.format(CalendarLocale.MALAYALAM, numerals = NumeralSystem.MALAYALAM)  // "൧൨൦൨ തുലാം ൨൮"
```

### Panchangam

```kotlin
val day = Panchangam.create().day(LocalDate.of(2025, 9, 5))

day.nakshatraAtSunrise.displayName(CalendarLocale.MALAYALAM)   // "തിരുവോണം"
day.tithiAtSunrise                                              // Tithi at sunrise
day.sunrise                                                     // Instant
```

### Festivals

```kotlin
val festivals = FestivalCalendar.create()

festivals.dateOf(KeralaFestivals.ONAM, 1202)    // 2026-08-26
festivals.festivalsIn(1202)                     // all festivals of ME 1202, sorted by date
```

### Android (Compose)

```kotlin
var selected by rememberSaveable { mutableStateOf(LocalDate.now()) }

KollavarshamMonthView(
    grid = MonthGrid.ofGregorianMonth(YearMonth.from(selected)),
    selectedDate = selected,
    onDateSelected = { selected = it },
)
```

More examples are in the [developer guide](docs/guide.md). A complete Android app is in [`sample-app`](sample-app).

## Modules

| Module | Description |
|---|---|
| [`kollavarsham-core`](kollavarsham-core) | Date models, conversion, validation, month boundaries, arithmetic |
| [`kollavarsham-astronomy`](kollavarsham-astronomy) | Sun and Moon positions, sankranti, sunrise/sunset (opt-in API) |
| [`kollavarsham-locale`](kollavarsham-locale) | Names, transliteration, Malayalam numerals, date formatting |
| [`kollavarsham-panchangam`](kollavarsham-panchangam) | Nakshatra, tithi, paksha, daily panchangam |
| [`kollavarsham-festivals`](kollavarsham-festivals) | Festival rules, Kerala festivals, observance providers |
| [`kollavarsham-android`](kollavarsham-android) | Jetpack Compose month calendar |
| [`sample-app`](sample-app) | Demo Android app (not published) |

## Accuracy and limitations

- **Month starts** follow Kerala's rule: day 1 is the day of the sidereal sankranti if it falls before 3/5 of the daytime at Thiruvananthapuram, otherwise the next day.
- **Astronomy:** sankranti times agree with a JPL-based ephemeris to about 1 second (1900–2025). Nakshatra and tithi boundaries agree with an independent reference to within seconds.
- **Validation:** all 21 cited month starts and 120 of 121 cited festival dates are reproduced. Details are in [VALIDATION_STRATEGY.md](VALIDATION_STRATEGY.md).
- **Borderline months:** in 30 months between 1900 and 2100, the sankranti falls within 10 minutes of the cut-off, and almanacs may differ by a day. `MonthBoundary.cutoffMargin` lets apps flag these.
- **Supported range:** 1900–2100. Dates outside it raise an error rather than a guess.
- **Not included:** Ashtami Rohini, Dhanu Thiruvathira, Thaipooyam, and Islamic festivals (moon sighting). Add them as dated observances.

No calendar calculation is 100 % certain. Please [report](https://github.com/anjalymsajeev/malayalam-calendar-kotlin/issues) any date that differs from a published almanac.

## Requirements

- **JVM:** Java 11 or newer.
- **Android:** minSdk 21. Below API 26, enable [core library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring), because the API uses `java.time`.
- **Kotlin:** 2.0 or newer is recommended. The library can also be used from Java.

## Documentation

| Document | Description |
|---|---|
| [Developer guide](docs/guide.md) | Task-based guide with examples |
| [API reference](API_REFERENCE.md) | The public API, with examples |
| [Calendar algorithm](CALENDAR_ALGORITHM.md) | Calendar rules, astronomy and measured accuracy |
| [Validation strategy](VALIDATION_STRATEGY.md) | Reference data and test strategy |
| [Architecture](ARCHITECTURE.md) | Module design and principles |
| [Decision records](docs/adr/) | Architecture decisions |
| [Changelog](CHANGELOG.md) and [release notes](docs/release-notes/) | Changes in each version |

Project history is in [SPRINT_PLAN.md](SPRINT_PLAN.md) and the [Sprint 0 research](docs/sprint-0/).

## Building from source

Requires JDK 21. Android modules also need the Android SDK (set `sdk.dir` in `local.properties`).

```bash
git clone https://github.com/anjalymsajeev/malayalam-calendar-kotlin.git
```

```bash
cd malayalam-calendar-kotlin
```

```bash
./gradlew build
```

```bash
./gradlew publishToMavenLocal
```

`build` compiles everything and runs the tests, ktlint and the API compatibility check. `publishToMavenLocal` installs the library into your local Maven repository, for use with `mavenLocal()`.

## Contributing

Contributions are welcome, especially cited almanac dates that improve validation. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

## Versioning

The project follows [Semantic Versioning](https://semver.org). During `0.x`, minor versions may contain breaking changes; each one is listed in the [changelog](CHANGELOG.md). The API stability policy is [ADR-0007](docs/adr/0007-api-stability-policy.md). Releases are made as described in [RELEASING.md](RELEASING.md).

## License

```
Copyright 2026 anjalymsajeev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Acknowledgements

Maintained by [@anjalymsajeev](https://github.com/anjalymsajeev), with Claude (Anthropic) as co-author.

The algorithms are based on R. Sewell & S. B. Dikshit's *The Indian Calendar* and J. Meeus's *Astronomical Algorithms*. Other projects that informed this one are credited in [ACKNOWLEDGEMENTS.md](ACKNOWLEDGEMENTS.md).
