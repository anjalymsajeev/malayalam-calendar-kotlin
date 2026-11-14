# API reference

Version: `0.1.0-alpha01` (unreleased) · Package root: `io.github.anjalymsajeev.kollavarsham`

The authoritative list of public declarations is in `*/api/*.api`, which CI checks on every build. This page
explains the API with examples.

> **Sprint 5 status:** conversion, localisation, panchangam and the festival engine are complete. For a task-oriented introduction, see [docs/guide.md](docs/guide.md).

## Artifacts

| Artifact | Contents |
|---|---|
| `io.github.anjalymsajeev:kollavarsham-core` | Models, conversion, validation, arithmetic |
| `io.github.anjalymsajeev:kollavarsham-locale` | Names, transliteration, numerals, formatting |
| `io.github.anjalymsajeev:kollavarsham-panchangam` | Nakshatra, tithi, sunrise/sunset, daily panchangam |
| `io.github.anjalymsajeev:kollavarsham-festivals` | Festival rules, Kerala festivals, observance providers |
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

### Names

```kotlin
MalayalamMonth.Vrischikam.displayName(CalendarLocale.MALAYALAM)                         // "വൃശ്ചികം"
MalayalamMonth.Vrischikam.displayName(CalendarLocale.ENGLISH)                           // "Vrischikam"
MalayalamMonth.Vrischikam.displayName(CalendarLocale.ENGLISH, Transliteration.ISO_15919) // "Vr̥ścikaṁ"
MalayalamWeekday.Njayar.displayName(CalendarLocale.MALAYALAM)                           // "ഞായർ"
```

Malayalam strings are NFC-normalised and use atomic chillu letters.

### `MalayalamNumerals` and `NumeralSystem`

```kotlin
MalayalamNumerals.format(1202)                 // "൧൨൦൨"
MalayalamNumerals.toMalayalamDigits("28/03")   // "൨൮/൦൩"
MalayalamNumerals.toWesternDigits("൨൮")        // "28"
MalayalamNumerals.parse("൧൨൦൨")                // 1202 (accepts Western digits too)
```

`NumeralSystem.WESTERN` is the default and `NumeralSystem.MALAYALAM` gives ൦–൯. The traditional signs ൰ ൱ ൲ aren't used.

### `MalayalamTransliterator` and `Transliteration`

```kotlin
MalayalamTransliterator.toIso15919("ചിങ്ങം")    // "ciṅṅaṁ"
MalayalamTransliterator.toIso15919("കേരളം")    // "kēraḷaṁ"
```

`Transliteration.COMMON` gives familiar spellings (Chingam); `Transliteration.ISO_15919` gives scholarly romanisation (Ciṅṅaṁ).
Known simplification: the dental and alveolar ന are both written "n".

### Formatting: `DateStyle`, `KollavarshamDateFormatter`, `MalayalamDate.format`

| Style | ENGLISH | MALAYALAM |
|---|---|---|
| SHORT | `28/03/1202` | `28/03/1202` |
| MEDIUM | `28 Thulam 1202` | `1202 തുലാം 28` |
| LONG | `28 Thulam 1202 ME` | `കൊല്ലവർഷം 1202 തുലാം 28` |
| FULL (needs a calendar) | `Shani, 28 Thulam 1202 ME` | `കൊല്ലവർഷം 1202 തുലാം 28, ശനി` |

```kotlin
date.format(CalendarLocale.MALAYALAM)                                  // MEDIUM
date.format(CalendarLocale.ENGLISH, DateStyle.LONG)
date.format(CalendarLocale.MALAYALAM, DateStyle.FULL, calendar)
date.format(CalendarLocale.MALAYALAM, numerals = NumeralSystem.MALAYALAM)

val f = KollavarshamDateFormatter.ofPattern("d MMMM y G", CalendarLocale.ENGLISH)
f.format(date)
f.withNumerals(NumeralSystem.MALAYALAM).withTransliteration(Transliteration.ISO_15919).format(date)
f.needsWeekday   // true if the pattern contains EEEE
```

| Pattern | Meaning |
|---|---|
| `y`… | year, at least as many digits as letters |
| `M`, `MM` | month number, plain or 2 digits |
| `MMMM` | month name |
| `d`, `dd` | day, plain or 2 digits |
| `EEEE` | weekday name (use `format(date, calendar)`) |
| `G` | era: `ME` / `കൊല്ലവർഷം` |
| `'text'` | literal; `''` is a quote |

Other ASCII letters are rejected, so typos fail fast instead of printing garbage.

## kollavarsham-panchangam

### `Panchangam` and `PanchangamLocation`

Immutable and thread-safe. Implements `NakshatraProvider`, `TithiProvider` and `SunTimesProvider`.

```kotlin
val panchangam = Panchangam.create()                                       // Thiruvananthapuram, IST
val kozhikode = Panchangam.create(PanchangamLocation(GeoLocation(11.2588, 75.7804, "Kozhikode")))

panchangam.nakshatraAt(Instant.now())                                      // Nakshatra
panchangam.nakshatraSpan(Instant.now())                                    // Span(value, start, end)
panchangam.tithiAt(Instant.now())                                          // Tithi
panchangam.tithiSpan(Instant.now())
panchangam.sunrise(LocalDate.of(2026, 11, 14))                             // Instant?
panchangam.sunset(LocalDate.of(2026, 11, 14))

val day: PanchangamDay = panchangam.day(LocalDate.of(2025, 9, 5))
day.sunrise; day.sunset; day.nextSunrise
day.nakshatraAtSunrise                                                     // Thiruvonam
day.tithiAtSunrise
day.nakshatras                                                             // all spans sunrise -> next sunrise
day.tithis
```

Nakshatra and tithi are geocentric, so they are the same instant everywhere; only sunrise and sunset depend on the location.

### Provider interfaces

`NakshatraProvider`, `TithiProvider` and `SunTimesProvider` let observance rules be tested with fakes, and let an alternative calculation be supplied later.

```kotlin
val fixed = object : NakshatraProvider {
    override fun nakshatraAt(instant: Instant) = Nakshatra.Thiruvonam
    override fun nakshatraSpan(instant: Instant) = Span(Nakshatra.Thiruvonam, instant.minusSeconds(3600), instant.plusSeconds(3600))
}
```

### `Nakshatra`, `Tithi`, `Paksha`, `Span`

```kotlin
Nakshatra.of(22)                                             // Thiruvonam
Nakshatra.Thiruvonam.next                                    // Avittam
Nakshatra.Thiruvonam.displayName(CalendarLocale.MALAYALAM)   // "തിരുവോണം"

Tithi.of(26)                                                 // KrishnaEkadashi
Tithi.KrishnaEkadashi.paksha                                 // Krishna
Tithi.KrishnaEkadashi.dayOfPaksha                            // 11
Tithi.KrishnaEkadashi.displayName(CalendarLocale.MALAYALAM)  // "ഏകാദശി"
Paksha.Krishna.displayName(CalendarLocale.MALAYALAM)         // "കൃഷ്ണപക്ഷം"

span.contains(instant)                                       // start inclusive, end exclusive
```

## kollavarsham-festivals

### `FestivalCalendar`

```kotlin
val festivals = FestivalCalendar.create()                    // Kerala defaults
festivals.dateOf(KeralaFestivals.ONAM, 1202)                 // 2026-08-26, or null if no day matches
festivals.candidates(KeralaFestivals.THRISSUR_POORAM, 1202)  // [2027-04-17, 2027-05-14]: rule ambiguity made visible
festivals.festivalsIn(1202)                                  // List<Observance>, sorted
FestivalCalendar.easter(2026)                                // 2026-04-05

// Custom definitions or panchangam (the providers are interfaces, so fakes work in tests)
FestivalCalendar(calendar, panchangam, panchangam, panchangam, definitions = KeralaFestivals.all + myFestivals)
```

### `Festival` and `FestivalRule`

| Rule | Example |
|---|---|
| `OnMalayalamDate(month, day)` | Chingam 1, Vrischikam 1, Makaram 1 |
| `OnGregorianDate(month, day)` | Kerala Piravi (1 Nov), Christmas |
| `FirstSunriseAfterSankranti(month)` | Vishu (Medam) |
| `NakshatraInMonth(month, nakshatra, readingTime, occurrence)` | Onam: Thiruvonam at sunrise in Chingam, the later day |
| `TithiInMonth(month, tithi, readingTimes, occurrence)` | Karkidaka Vavu: Amavasya at 4/5 of daylight, else at sunrise |
| `TithiInLunarMonth(sankrantiMonth, tithi, readingTime, occurrence)` | Sivaratri, Vinayaka Chathurthi, Vijayadasami, Deepavali |
| `FirstTithiOnOrAfter(month, day, tithi, readingTime)` | Guruvayur Ekadashi |
| `DaysFrom(festivalId, days)` | Atham (Onam − 9), Mandala Pooja (Vrischikam 1 + 40) |
| `DaysFromEaster(days)` | Easter, Good Friday, Maundy Thursday |

`ReadingTime`: `Sunrise`, `Sunset`, `Midnight`, `FractionOfDaylight(f)`, `BeforeSunrise(minutes)`. `Occurrence`: `FIRST`, `LAST`.

```kotlin
val myTempleFestival = Festival(
    id = "temple-utsavam",
    englishName = "Temple Utsavam",
    malayalamName = "ഉത്സവം",
    category = ObservanceCategory.TEMPLE,
    rule = FestivalRule.NakshatraInMonth(MalayalamMonth.Meenam, Nakshatra.Uthram),
)
```

### `KeralaFestivals`

The built-in definitions:

- Malayalam New Year, Atham, Onam, Vishu, Karkidaka Vavu, Sivaratri, Vinayaka Chathurthi.
- Durgashtami, Mahanavami, Vijayadasami, Deepavali.
- Thrissur Pooram, Attukal Pongala, Guruvayur Ekadashi.
- Mandala season start, Mandala Pooja, Makaravilakku.
- Kerala Piravi, Republic Day, Independence Day, Gandhi Jayanti.
- Christmas, Easter, Good Friday, Maundy Thursday.

### Observance providers

```kotlin
val provider = ObservanceProvider.combine(
    DatedObservances.fromCsv(governmentHolidaysCsv),         // dated data from the app; wins on (date, id) clashes
    RuleBasedObservances(FestivalCalendar.create()),
)
provider.observances(LocalDate.of(2026, 1, 1)..LocalDate.of(2026, 12, 31))
provider.observancesOn(LocalDate.of(2026, 8, 26))
```

The CSV header is `date,id,english_name,malayalam_name,category`. `ObservanceCategory` is `HINDU`, `CHRISTIAN`, `ISLAMIC`, `CIVIC`, `TEMPLE` or `CUSTOM`.
