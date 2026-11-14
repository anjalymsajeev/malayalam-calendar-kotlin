# Developer guide

This guide shows how to use the Kollavarsham SDK in an app, with Kotlin examples for each task. The full list of public declarations is in [API_REFERENCE.md](../API_REFERENCE.md); the generated API site is built with `./gradlew dokkaHtmlMultiModule`.

## 1. Add the dependency

The artifacts aren't on Maven Central yet; publishing comes in Sprint 7. Until then, publish them locally:

```bash
./gradlew publishToMavenLocal
```

Then, in your app:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement { repositories { mavenLocal(); mavenCentral() } }

// build.gradle.kts
dependencies {
    implementation("io.github.anjalymsajeev:kollavarsham-locale:0.1.0-alpha01") // includes kollavarsham-core
}
```

Use `kollavarsham-core` alone if you don't need names or formatting.

**Android:** the API uses `java.time`. It is built in from API 26; below that, enable [core library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring).

## 2. Convert dates

```kotlin
val calendar = KollavarshamCalendar.create()   // shared, thread-safe; Thiruvananthapuram reference

val ml: MalayalamDate = calendar.fromGregorian(LocalDate.of(2026, 11, 14))   // 1202 Thulam 28
val greg: LocalDate = calendar.toGregorian(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1))   // 2026-11-17

// Extension shortcuts using the default calendar
LocalDate.now(KollavarshamCalendar.INDIAN_STANDARD_TIME).toMalayalamDate()
MalayalamDate(1202, MalayalamMonth.Chingam, 1).toLocalDate()

calendar.today()   // today in Indian Standard Time
```

## 3. Handle invalid input

A Malayalam month has 29 to 32 days, and the length changes from year to year. The SDK never rolls an invalid date over into the next month.

```kotlin
val date = MalayalamDate(1202, MalayalamMonth.Thulam, 31)   // structurally fine (1..32)

when (val v = calendar.validate(date)) {
    DateValidation.Valid -> println("ok")
    is DateValidation.DayOutOfRange -> println("Thulam 1202 has only ${v.monthLength} days")
    is DateValidation.OutOfSupportedRange -> println("supported: ${v.supportedRange}")
}

calendar.toGregorianOrNull(date)   // null instead of an exception
calendar.toGregorian(date)         // throws InvalidMalayalamDateException
```

The supported range is 1900-01-01 to 2100-12-31. Outside it, the SDK throws `DateOutOfSupportedRangeException`.

## 4. Months and arithmetic

```kotlin
calendar.monthLength(1202, MalayalamMonth.Thulam)                  // 30
calendar.monthsOf(1202).forEach { println("${it.month}: ${it.firstDay}..${it.lastDay}") }

calendar.plusDays(MalayalamDate(1202, MalayalamMonth.Vrischikam, 1), 40)    // the 41st day of Mandalam
calendar.plusMonths(MalayalamDate(1202, MalayalamMonth.Edavam, 32), 1)      // clamped: Mithunam 31
calendar.daysBetween(start, endExclusive)
```

## 5. Show uncertain months honestly

If the sankranti falls within minutes of Kerala's cut-off, almanacs can disagree by a day. Each `MonthBoundary` reports this:

```kotlin
val b = calendar.monthBoundary(1199, MalayalamMonth.Chingam)
if (b.cutoffMargin.abs() < Duration.ofMinutes(10)) {
    showNote("Some almanacs may start ${b.month} a day later")
}
b.source   // Computed, VerifiedOverride (cited almanac correction) or UserOverride
```

If your community's almanac differs, add your own override:

```kotlin
val myCalendar = KollavarshamCalendar.create(
    CalendarConfiguration(monthStartOverrides = mapOf(MonthKey(1199, MalayalamMonth.Chingam) to LocalDate.of(2023, 8, 17))),
)
```

## 6. Names, transliteration and numerals

```kotlin
MalayalamMonth.Vrischikam.displayName(CalendarLocale.MALAYALAM)                        // "വൃശ്ചികം"
MalayalamMonth.Vrischikam.displayName(CalendarLocale.ENGLISH)                          // "Vrischikam"
MalayalamMonth.Vrischikam.displayName(CalendarLocale.ENGLISH, Transliteration.ISO_15919) // "Vr̥ścikaṁ"
LocalDate.of(2026, 11, 14).malayalamWeekday.displayName(CalendarLocale.MALAYALAM)       // "ശനി"

MalayalamNumerals.format(1202)          // "൧൨൦൨"
MalayalamNumerals.parse("൧൨൦൨")         // 1202
MalayalamTransliterator.toIso15919("കേരളം")   // "kēraḷaṁ"
```

## 7. Format dates

```kotlin
val d = MalayalamDate(1202, MalayalamMonth.Thulam, 28)

d.format(CalendarLocale.ENGLISH)                                  // "28 Thulam 1202"
d.format(CalendarLocale.MALAYALAM)                                // "1202 തുലാം 28"
d.format(CalendarLocale.MALAYALAM, DateStyle.LONG)                // "കൊല്ലവർഷം 1202 തുലാം 28"
d.format(CalendarLocale.MALAYALAM, DateStyle.FULL, calendar)      // "കൊല്ലവർഷം 1202 തുലാം 28, ശനി"
d.format(CalendarLocale.MALAYALAM, numerals = NumeralSystem.MALAYALAM)   // "൧൨൦൨ തുലാം ൨൮"

// Custom patterns: y, M, MM, MMMM, d, dd, EEEE, G, 'literal'
val f = KollavarshamDateFormatter.ofPattern("EEEE, d MMMM", CalendarLocale.ENGLISH)
f.format(d, calendar)                                             // "Shani, 28 Thulam"
```

Formatters are immutable and thread-safe, so keep them in a field and reuse them.

## 8. A complete example

[`samples/jvm-consumer`](../samples/jvm-consumer) is a standalone project that consumes the published artifacts. It prints a formatted date and the months of a year.
