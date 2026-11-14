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
    implementation("io.github.anjalymsajeev:kollavarsham-festivals:0.1.0-alpha01") // brings panchangam, locale, core
}
```

One dependency is enough: each module brings the ones below it (festivals → panchangam → locale → core → astronomy). Choose a lower module if you need less, for example `kollavarsham-core` for conversion only. For the Compose calendar view, add `kollavarsham-android` as well.

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

## 8. Panchangam: nakshatra, tithi, sunrise

Add `io.github.anjalymsajeev:kollavarsham-panchangam`.

```kotlin
val panchangam = Panchangam.create()            // or Panchangam.create(PanchangamLocation(GeoLocation(...)))
val today = panchangam.day(LocalDate.now(KollavarshamCalendar.INDIAN_STANDARD_TIME))

println("Sunrise ${today.sunrise}, sunset ${today.sunset}")
println("Star: ${today.nakshatraAtSunrise.displayName(CalendarLocale.MALAYALAM)}")
println("Tithi: ${today.tithiAtSunrise.paksha.displayName(CalendarLocale.MALAYALAM)} ${today.tithiAtSunrise.displayName(CalendarLocale.MALAYALAM)}")

// When does the current star end?
val star = panchangam.nakshatraSpan(Instant.now())
println("${star.value} until ${star.end.atZone(KollavarshamCalendar.INDIAN_STANDARD_TIME)}")
```

To list a person's birth-star days, for example every Atham in 2027:

```kotlin
panchangam.nakshatraOccurrences(Nakshatra.Atham, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31))
    .forEach { println("${it.observanceDate}  (${it.span.start} – ${it.span.end})") }
```

Elements are read at sunrise, following the Kerala convention. `today.nakshatras` and `today.tithis` list every span until the next sunrise, so you can apply other rules yourself.

## 9. Festivals and observances

Add `io.github.anjalymsajeev:kollavarsham-festivals`.

```kotlin
val festivals = FestivalCalendar.create()
festivals.festivalsIn(1202).forEach { println("${it.date}  ${it.name(CalendarLocale.MALAYALAM)}") }

// Where sources may disagree, show the alternatives:
val days = festivals.candidates(KeralaFestivals.THRISSUR_POORAM, 1202)
if (days.size > 1) println("Thrissur Pooram: ${days.first()} (some calendars: ${days.last()})")

// Add a government holiday list, moon-sighted Eid dates or family events as data:
val provider = ObservanceProvider.combine(
    DatedObservances.fromCsv(myCsv),
    RuleBasedObservances(festivals),
)
provider.observancesOn(LocalDate.of(2026, 8, 26))
```

Islamic festivals aren't computed, because their dates follow moon sighting. Supply announced dates through `DatedObservances`.

## 10. Android: Compose month calendar

Add `io.github.anjalymsajeev:kollavarsham-android`. For minSdk below 26, also enable desugaring:

```kotlin
android { compileOptions { isCoreLibraryDesugaringEnabled = true } }
dependencies { coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3") }
```

```kotlin
@Composable
fun MyCalendar(festivals: ObservanceProvider) {
    var selected by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val month = YearMonth.from(selected)
    val grid = remember(month) { MonthGrid.ofGregorianMonth(month) }
    val dots = remember(month) { festivals.observances(month.atDay(1)..month.atEndOfMonth()).map { it.date }.toSet() }
    KollavarshamMonthView(grid, selected, onDateSelected = { selected = it }, markedDates = dots)
}
```

Keep calculations outside Composables, as the sample app's `CalendarPresenter` does.

## 11. A complete example

- [`samples/jvm-consumer`](../samples/jvm-consumer): a standalone JVM project that consumes the published artifacts.
- [`sample-app`](../sample-app): the Android demo. It has a monthly calendar with Malayalam dates, a Malayalam/English toggle, daily nakshatra, tithi, sunrise/sunset and festivals, and a note on borderline months. It requests no internet permission.

```bash
./gradlew :sample-app:assembleDebug
```
