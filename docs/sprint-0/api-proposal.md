# Domain model and public API proposal (Sprint 0)

Status: **Approved, with Sprint 1 amendments** (see §6). `API_REFERENCE.md` will be generated from the real code in Sprint 1 and then maintained.

## 1. Review of the illustrative API in the brief

| Brief | Proposal | Why |
|---|---|---|
| `KollavarshamCalendar.create()` | `KollavarshamCalendar.create()` and `KollavarshamCalendar.create(configuration)` | Keeps the brief's name and adds a configured overload. A Kotlin factory, not a public constructor, so internals can change. |
| `calendar.fromGregorian(LocalDate)` | Keep it | Clear |
| `calendar.toGregorian(MalayalamDate(1202, Vrischikam, 1))` | Keep it. `MalayalamDate(...)` performs **structural** validation only. | Whether day 31 exists depends on the astronomically computed month length, which a plain value object can't know. See §3. |
| `malayalamDate.format(CalendarLocale.MALAYALAM)` | Keep it as an extension function in `kollavarsham-locale` | Keeps strings out of core. Callers import one extra artifact. |
| Example date: 14 Nov 2026 ↔ Vrischikam 1, 1202 | **The example is wrong.** 14 Nov 2026 is **Thulam 28, 1202**, and Vrischikam 1, 1202 is **17 Nov 2026**. | Measured this sprint. The Vrischika sankranti falls on 16 Nov 2026 at about 19:50 IST, after the cut-off. kollavarsham-js gives the same result. This is exactly why we won't assume dates. |
| Models `GregorianDate`, `CalendarDateResult`, `CalendarConversionResult` | Dropped. Use `LocalDate`, plus one sealed `DateValidation` | Avoids parallel types that do the same job (see ARCHITECTURE §2). |

## 2. Domain model (Sprint 1–2 scope)

```kotlin
package io.github.anjalymsajeev.kollavarsham.core

/** The 12 solar months in Chingam-first order. `number` is 1..12. */
public enum class MalayalamMonth(public val number: Int) {
    Chingam(1), Kanni(2), Thulam(3), Vrischikam(4), Dhanu(5), Makaram(6),
    Kumbham(7), Meenam(8), Medam(9), Edavam(10), Mithunam(11), Karkidakam(12);

    public val next: MalayalamMonth get() = entries[number % 12]
    public val previous: MalayalamMonth get() = entries[(number + 10) % 12]

    public companion object {
        public fun of(number: Int): MalayalamMonth  // throws IllegalArgumentException outside 1..12
    }
}

/** Mirrors java.time.DayOfWeek. Names live in kollavarsham-locale. */
public enum class MalayalamWeekday {
    Njayar, Thinkal, Chovva, Budhan, Vyazham, Velli, Shani;
    public val dayOfWeek: java.time.DayOfWeek
    public companion object { public fun of(dayOfWeek: java.time.DayOfWeek): MalayalamWeekday }
}

/**
 * A Kollavarsham calendar date. Immutable and comparable.
 * The constructor checks structure only: day in 1..32 and year in the supported range.
 * Whether the day exists in that month is checked by [KollavarshamCalendar].
 */
public data class MalayalamDate(
    val year: Int,              // Kollavarsham (ME) year, Chingam-first numbering
    val month: MalayalamMonth,
    val day: Int,
) : Comparable<MalayalamDate>

/** Where the boundary came from. */
public enum class BoundarySource { Computed, VerifiedOverride, UserOverride }

/** One month's boundaries and the evidence behind them. */
public data class MonthBoundary(
    val year: Int,
    val month: MalayalamMonth,
    val firstDay: java.time.LocalDate,
    val lastDay: java.time.LocalDate,
    val sankranti: java.time.Instant,             // computed sankranti moment
    val cutoffMargin: java.time.Duration,         // cut-off minus sankranti; negative means after the cut-off
    val source: BoundarySource,
) { val length: Int }                             // 29..32

public data class GeoLocation(val latitude: Double, val longitude: Double, val name: String? = null) {
    public companion object { public val Thiruvananthapuram: GeoLocation }
}

public data class CalendarConfiguration(
    val referenceLocation: GeoLocation = GeoLocation.Thiruvananthapuram,
    val monthStartOverrides: Map<MonthKey, java.time.LocalDate> = emptyMap(),
    val applyVerifiedOverrides: Boolean = true,
)
// Deliberately absent until there is evidence: an ayanamsa switch (Q1) and Kanni year start (Sprint 3).

public sealed interface DateValidation {
    public data object Valid : DateValidation
    public data class DayOutOfRange(val day: Int, val monthLength: Int) : DateValidation
    public data class OutOfSupportedRange(val supported: ClosedRange<java.time.LocalDate>) : DateValidation
}

public sealed class KollavarshamException(message: String) : IllegalArgumentException(message)
public class InvalidMalayalamDateException(public val validation: DateValidation) : KollavarshamException(...)
public class DateOutOfSupportedRangeException(...) : KollavarshamException(...)
```

`CalendarLocale` (MALAYALAM, ENGLISH) and the numeral options live in `kollavarsham-locale` (Sprint 3).

### Naming decisions

- Enum constants use **UpperCamelCase**, as in the brief's `MalayalamMonth.Vrischikam`. Kotlin conventions allow this, and it reads naturally for proper nouns.
- Identifiers use one ASCII spelling: `Edavam`, `Karkidakam`, `Vrischikam`. Display spellings come from the locale module.
  - [OPEN Q9] Which transliterations do you prefer? kollavarsham-js uses `Idavam`/`Karkitakam`; the Dart package uses `Edavam`/`Karkidakam`.

## 3. `KollavarshamCalendar`

```kotlin
public class KollavarshamCalendar private constructor(public val configuration: CalendarConfiguration) {

    public val supportedRange: ClosedRange<java.time.LocalDate>     // 1900-01-01..2100-12-31

    // Conversion
    public fun fromGregorian(date: java.time.LocalDate): MalayalamDate
    public fun toGregorian(date: MalayalamDate): java.time.LocalDate       // throws InvalidMalayalamDateException
    public fun toGregorianOrNull(date: MalayalamDate): java.time.LocalDate?

    // Validation
    public fun validate(date: MalayalamDate): DateValidation
    public fun isValid(date: MalayalamDate): Boolean

    // Month structure
    public fun monthBoundary(year: Int, month: MalayalamMonth): MonthBoundary
    public fun monthLength(year: Int, month: MalayalamMonth): Int
    public fun monthsOf(year: Int): List<MonthBoundary>                     // Chingam..Karkidakam

    // Arithmetic. Results are always valid dates.
    public fun plusDays(date: MalayalamDate, days: Long): MalayalamDate
    public fun plusMonths(date: MalayalamDate, months: Long): MalayalamDate  // day is clamped to month length
    public fun daysBetween(start: MalayalamDate, endExclusive: MalayalamDate): Long

    public fun today(clock: java.time.Clock = java.time.Clock.system(IST)): MalayalamDate

    public companion object {
        public fun create(): KollavarshamCalendar                            // shared default instance
        public fun create(configuration: CalendarConfiguration): KollavarshamCalendar
    }
}

// Convenience extensions (core)
public fun java.time.LocalDate.toMalayalamDate(calendar: KollavarshamCalendar = KollavarshamCalendar.create()): MalayalamDate
public val java.time.LocalDate.malayalamWeekday: MalayalamWeekday
```

Why the arithmetic lives on the calendar rather than on `MalayalamDate`: it needs month lengths, which depend on the configuration (location and overrides). A `MalayalamDate.plusDays()` would have to hide a global default calendar, and that would quietly give wrong answers for custom configurations.

## 4. Usage examples (target, Sprint 2–3)

```kotlin
val calendar = KollavarshamCalendar.create()

val today = calendar.fromGregorian(LocalDate.of(2026, 11, 14))
// MalayalamDate(year=1202, month=Thulam, day=28)

val mandalamStart = calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))
// 2026-11-17

calendar.validate(MalayalamDate(1202, MalayalamMonth.Thulam, 31))
// DayOutOfRange(day=31, monthLength=30)

val boundary = calendar.monthBoundary(1197, MalayalamMonth.Thulam)
if (boundary.cutoffMargin.abs() < Duration.ofMinutes(10)) {
    // The app can show "almanacs may differ by a day"
}

// kollavarsham-locale (Sprint 3)
today.format(CalendarLocale.MALAYALAM)              // "൧൨൦൨ തുലാം ൨൮" (exact pattern TBD in Sprint 3)
today.format(CalendarLocale.ENGLISH)                // "28 Thulam 1202"
```

## 5. Compatibility rules

- `explicitApi()` is enabled. Every public declaration is intentional.
- The binary-compatibility-validator `.api` dump is checked in and verified in CI.
- No public `data class` copy or constructor changes after 1.0. `MalayalamDate` and `MonthBoundary` will be reviewed again in Sprint 7 to decide whether they should become regular classes for binary-compatibility safety.
- Sealed hierarchies may gain subtypes only in minor releases with release notes. Callers are advised to include an `else` branch in `when` expressions.

## 6. Amendments made in Sprint 1

- **`MonthKey(year, month)`** replaces `Pair<Int, MalayalamMonth>` for override keys. It is clearer at call sites and carries month arithmetic (`next()`, `previous()`, `plus()`).
- **The package root** is `io.github.anjalymsajeev.kollavarsham` (Q4 default).
- **`KollavarshamCalendar.create()` is deferred to Sprint 2.** Without the astronomical boundary source it would have nothing real to compute with.
- **`LocalDate.toMalayalamDate()`** is deferred to Sprint 2 for the same reason.
- **Validation order:** `validate()` checks the supported year, then the day against the month length, then the Gregorian supported range.
