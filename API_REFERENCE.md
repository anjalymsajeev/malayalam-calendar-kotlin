# API reference

Version: `0.1.0-alpha01` (unreleased) · Package root: `io.github.anjalymsajeev.kollavarsham`

The authoritative list of public declarations is in `*/api/*.api`, which CI checks on every build. This page
explains the API with examples.

> **Sprint 2 status:** conversion is backed by the astronomical engine. Formatting and numerals arrive in Sprint 3.

## Artifacts

| Artifact | Contents |
|---|---|
| `io.github.anjalymsajeev:kollavarsham-core` | Models, conversion, validation, arithmetic |
| `io.github.anjalymsajeev:kollavarsham-locale` | Display names (Sprint 1), formatting and numerals (Sprint 3) |
| `io.github.anjalymsajeev:kollavarsham-astronomy` | Astronomy, opt-in (`@KollavarshamAstronomyApi`), used by core |

The artifacts are not yet published.

## kollavarsham-core

### `MalayalamMonth`

Chingam-first enum: `Chingam`, `Kanni`, `Thulam`, `Vrischikam`, `Dhanu`, `Makaram`, `Kumbham`, `Meenam`,
`Medam`, `Edavam`, `Mithunam`, `Karkidakam`.

```kotlin
MalayalamMonth.of(4)                 // Vrischikam
MalayalamMonth.Vrischikam.number     // 4
MalayalamMonth.Karkidakam.next       // Chingam
MalayalamMonth.Chingam.previous      // Karkidakam
```

### `MalayalamWeekday`

```kotlin
MalayalamWeekday.of(DayOfWeek.SUNDAY)          // Njayar
MalayalamWeekday.Velli.dayOfWeek               // FRIDAY
LocalDate.of(2026, 11, 14).malayalamWeekday    // Shani
```

### `MalayalamDate`

An immutable, comparable value. The constructor checks structure only (year ≥ 1, day 1..32).

```kotlin
val date = MalayalamDate(year = 1202, month = MalayalamMonth.Vrischikam, day = 1)
date < MalayalamDate(1202, MalayalamMonth.Dhanu, 1)   // true
date.toString()                                        // "1202-04-01"
MalayalamDate(1202, MalayalamMonth.Thulam, 33)         // IllegalArgumentException
```

### `MonthKey`

```kotlin
val key = MonthKey(1202, MalayalamMonth.Karkidakam)
key.next()        // MonthKey(1203, Chingam)
key.plus(-13)     // MonthKey(1201, Edavam)
```

### `KollavarshamCalendar`

Immutable and thread-safe. The supported range is 1900-01-01 to 2100-12-31.

```kotlin
// Creation
val calendar = KollavarshamCalendar.create()                 // shared default (Thiruvananthapuram)
val kozhikode = KollavarshamCalendar.create(
    CalendarConfiguration(referenceLocation = GeoLocation(11.2588, 75.7804, "Kozhikode")),
)

// Conversion
calendar.fromGregorian(LocalDate.of(2026, 11, 14))                         // MalayalamDate
calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))    // LocalDate, or throws
calendar.toGregorianOrNull(MalayalamDate(1202, MalayalamMonth.Thulam, 31)) // null if invalid

// Validation
calendar.validate(MalayalamDate(1202, MalayalamMonth.Thulam, 31))          // DayOutOfRange(31, 30)
calendar.isValid(MalayalamDate(1202, MalayalamMonth.Thulam, 30))           // true

// Month structure
calendar.monthBoundary(1202, MalayalamMonth.Vrischikam)                    // MonthBoundary
calendar.monthLength(1202, MalayalamMonth.Thulam)                          // 29..32
calendar.monthsOf(1202)                                                    // 12 boundaries

// Arithmetic: results are always valid dates
calendar.plusDays(date, 41)
calendar.plusMonths(MalayalamDate(1202, MalayalamMonth.Mithunam, 32), 1)   // day clamped
calendar.daysBetween(start, endExclusive)

calendar.today()                        // Indian Standard Time, system clock
calendar.today(Clock.fixed(...))        // deterministic
```

### Extensions

```kotlin
LocalDate.of(2026, 11, 14).toMalayalamDate()                        // 1202 Thulam 28 (default calendar)
MalayalamDate(1202, MalayalamMonth.Vrischikam, 1).toLocalDate()     // 2026-11-17
LocalDate.of(2026, 11, 14).toMalayalamDate(kozhikode)               // with a specific calendar
```

### `MonthBoundary` and `BoundarySource`

```kotlin
val b = calendar.monthBoundary(1202, MalayalamMonth.Vrischikam)
b.firstDay; b.lastDay; b.length
b.sankranti                         // Instant the Sun entered the rashi
b.cutoffMargin                      // cut-off minus sankranti; small values mean almanacs may differ
b.source                            // Computed | VerifiedOverride | UserOverride
LocalDate.of(2026, 11, 20) in b     // contains
```

### `CalendarConfiguration` and `GeoLocation`

```kotlin
CalendarConfiguration(
    referenceLocation = GeoLocation.Thiruvananthapuram,
    monthStartOverrides = mapOf(MonthKey(1197, MalayalamMonth.Thulam) to LocalDate.of(2021, 10, 17)),
    applyVerifiedOverrides = true,
)
```

An override that would make any month shorter than 29 or longer than 32 days fails when the calendar
is constructed.

### Errors

| Type | When |
|---|---|
| `DateValidation.Valid` / `DayOutOfRange` / `OutOfSupportedRange` | Result of `validate()`; never thrown |
| `InvalidMalayalamDateException` | The day doesn't exist in that month (e.g. Thulam 31 in a 30-day Thulam) |
| `DateOutOfSupportedRangeException` | The input or result is outside 1900–2100 |

Both exceptions extend `KollavarshamException`, which extends `IllegalArgumentException`.

## kollavarsham-astronomy (opt-in)

The astronomy API is unstable until 1.0. Opt in with `@OptIn(KollavarshamAstronomyApi::class)`.

```kotlin
@OptIn(KollavarshamAstronomyApi::class)
fun example() {
    val jde = AstroTime.julianEphemerisDay(AstroTime.julianDayUt(Instant.now()))
    Sun.position(jde).longitude                                              // apparent tropical longitude, degrees
    Sankranti.siderealLongitude(Instant.now(), Ayanamsa.LahiriTrue)          // nirayana longitude
    Sankranti.find(rashi = 7, estimate = Instant.parse("2026-11-15T00:00:00Z"), ayanamsa = Ayanamsa.LahiriTrue)
    SunriseSunset.sunrise(LocalDate.of(2026, 11, 14), 8.5241, 76.9366, ZoneId.of("Asia/Kolkata"))
    DeltaT.seconds(2026.9)
}
```

## kollavarsham-locale

```kotlin
MalayalamMonth.Vrischikam.displayName(CalendarLocale.MALAYALAM)   // "വൃശ്ചികം"
MalayalamMonth.Vrischikam.displayName(CalendarLocale.ENGLISH)     // "Vrischikam"
MalayalamWeekday.Njayar.displayName(CalendarLocale.MALAYALAM)     // "ഞായർ"
```

Malayalam strings are NFC-normalised and use atomic chillu letters.
