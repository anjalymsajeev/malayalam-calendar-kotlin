package io.github.anjalymsajeev.kollavarsham.festivals

import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import io.github.anjalymsajeev.kollavarsham.panchangam.Nakshatra
import io.github.anjalymsajeev.kollavarsham.panchangam.NakshatraProvider
import io.github.anjalymsajeev.kollavarsham.panchangam.Panchangam
import io.github.anjalymsajeev.kollavarsham.panchangam.Span
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FestivalReferenceTest {
    private val festivals = FestivalCalendar.create()

    @Test fun `every reference festival date is reproduced`() {
        val byId = KeralaFestivals.all.associateBy { it.id }
        val rows =
            javaClass
                .getResourceAsStream("/reference/festival-dates.csv")!!
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .drop(1)
        assertTrue(rows.size >= 120, "reference dataset unexpectedly small: ${rows.size}")
        val misses =
            rows.mapNotNull { row ->
                val (year, id, date) = row.split(',')
                val got = festivals.dateOf(byId.getValue(id), year.toInt())
                if (got == LocalDate.parse(date)) null else "$id $year: expected $date, got $got"
            }
        // Documented disagreements (VALIDATION_STRATEGY.md): the test fails if this list changes in either direction.
        val knownMisses = rows.filter { "KNOWN-MISS" in it }.map { row -> row.split(',').let { "${it[1]} ${it[0]}" } }
        assertEquals(knownMisses, misses.map { it.substringBefore(':') })
    }

    @Test fun `candidates expose both days when a festival's star occurs twice`() {
        // Thrissur Pooram: Pooram at midnight twice in Medam 2019 and 2027. Kerala kept the later day
        // in 2019 and announced the earlier day for 2027, so no first/last rule fits both.
        assertEquals(
            listOf(LocalDate.of(2019, 4, 16), LocalDate.of(2019, 5, 13)),
            festivals.candidates(KeralaFestivals.THRISSUR_POORAM, 1194),
        )
        assertEquals(
            listOf(LocalDate.of(2027, 4, 17), LocalDate.of(2027, 5, 14)),
            festivals.candidates(KeralaFestivals.THRISSUR_POORAM, 1202),
        )
        assertEquals(listOf(LocalDate.of(2026, 8, 26)), festivals.candidates(KeralaFestivals.ONAM, 1202))
        assertEquals(listOf(LocalDate.of(2026, 11, 1)), festivals.candidates(KeralaFestivals.KERALA_PIRAVI, 1202))
    }

    @Test fun `Easter by the Gregorian computus`() {
        mapOf(
            2000 to "2000-04-23",
            2019 to "2019-04-21",
            2024 to "2024-03-31",
            2025 to "2025-04-20",
            2038 to "2038-04-25",
            2285 to "2285-03-22",
        ).forEach { (year, date) -> assertEquals(LocalDate.parse(date), FestivalCalendar.easter(year), "$year") }
    }

    @Test fun `festivals of a year are sorted, complete and inside the year`() {
        val year = 1202
        val list = festivals.festivalsIn(year)
        val months = KollavarshamCalendar.create().monthsOf(year)
        assertEquals(list.sortedBy { it.date }, list)
        assertTrue(list.all { it.date >= months.first().firstDay && it.date <= months.last().lastDay })
        assertEquals(KeralaFestivals.all.map { it.id }.toSet(), list.map { it.id }.toSet())
        assertEquals(LocalDate.of(2026, 8, 17), list.first { it.id == "malayalam-new-year" }.date)
        assertEquals(LocalDate.of(2026, 11, 1), list.first { it.id == "kerala-piravi" }.date)
        assertEquals("കേരളപ്പിറവി", list.first { it.id == "kerala-piravi" }.name(CalendarLocale.MALAYALAM))
        // Atham is nine days before Thiruvonam; Mandala Pooja is the 41st day from Vrischikam 1.
        assertEquals(
            9,
            java.time.temporal.ChronoUnit.DAYS
                .between(list.first { it.id == "atham" }.date, list.first { it.id == "onam" }.date),
        )
    }
}

class FestivalRuleTest {
    private val calendar = KollavarshamCalendar.create()
    private val panchangam = Panchangam.create()

    /** A nakshatra provider that reports [star] on [dates] (IST) and Ashwathi otherwise. */
    private fun fakeStars(
        star: Nakshatra,
        vararg dates: LocalDate,
    ) = object : NakshatraProvider {
        override fun nakshatraAt(instant: Instant) =
            if (instant.atZone(KollavarshamCalendar.INDIAN_STANDARD_TIME).toLocalDate() in dates) star else Nakshatra.Ashwathi

        override fun nakshatraSpan(instant: Instant) = Span(nakshatraAt(instant), instant, instant.plusSeconds(1))
    }

    private fun calendarWith(
        stars: NakshatraProvider,
        vararg definitions: Festival,
    ) = FestivalCalendar(calendar, stars, panchangam, panchangam, definitions = definitions.toList())

    @Test fun `first and last occurrence are chosen as configured`() {
        val a = LocalDate.of(2026, 8, 18)
        val b = LocalDate.of(2026, 9, 14)
        val first =
            Festival(
                "x-first",
                "X",
                "X",
                ObservanceCategory.CUSTOM,
                FestivalRule.NakshatraInMonth(MalayalamMonth.Chingam, Nakshatra.Rohini),
            )
        val last =
            first.copy(
                id = "x-last",
                rule = FestivalRule.NakshatraInMonth(MalayalamMonth.Chingam, Nakshatra.Rohini, occurrence = Occurrence.LAST),
            )
        val fc = calendarWith(fakeStars(Nakshatra.Rohini, a, b), first, last)
        assertEquals(a, fc.dateOf(first, 1202))
        assertEquals(b, fc.dateOf(last, 1202))
        // No matching day: null, never a guess.
        assertNull(calendarWith(fakeStars(Nakshatra.Bharani, a), first).dateOf(first, 1202))
    }

    @Test fun `days-from rules chain and reject unknown ids and cycles`() {
        val base = Festival("base", "Base", "Base", ObservanceCategory.CUSTOM, FestivalRule.OnMalayalamDate(MalayalamMonth.Thulam, 1))
        val after = Festival("after", "After", "After", ObservanceCategory.CUSTOM, FestivalRule.DaysFrom("base", 10))
        val fc = calendarWith(panchangam, base, after)
        assertEquals(LocalDate.of(2026, 10, 28), fc.dateOf(after, 1202))
        assertFailsWith<IllegalArgumentException> { calendarWith(panchangam, after) }
        val a = Festival("a", "A", "A", ObservanceCategory.CUSTOM, FestivalRule.DaysFrom("b", 1))
        val b = Festival("b", "B", "B", ObservanceCategory.CUSTOM, FestivalRule.DaysFrom("a", 1))
        assertFailsWith<IllegalStateException> { calendarWith(panchangam, a, b).dateOf(a, 1202) }
        assertFailsWith<IllegalArgumentException> { calendarWith(panchangam, base, base) }
    }

    @Test fun `Gregorian rules pick the occurrence inside the ME year`() {
        val jan = Festival("jan", "Jan", "Jan", ObservanceCategory.CIVIC, FestivalRule.OnGregorianDate(Month.JANUARY, 26))
        val fc = calendarWith(panchangam, jan)
        assertEquals(LocalDate.of(2027, 1, 26), fc.dateOf(jan, 1202))
        assertFailsWith<IllegalArgumentException> { FestivalRule.OnGregorianDate(Month.FEBRUARY, 30) }
    }

    @Test fun `Vishu follows the sunrise after the Mesha sankranti`() {
        val fc = FestivalCalendar.create()
        // 2025: sankranti 03:30 IST, before sunrise -> same day. 2026: 09:39 IST, after sunrise -> next day (Medam 2).
        assertEquals(LocalDate.of(2025, 4, 14), fc.dateOf(KeralaFestivals.VISHU, 1200))
        assertEquals(LocalDate.of(2026, 4, 15), fc.dateOf(KeralaFestivals.VISHU, 1201))
        assertEquals(LocalDate.of(2026, 4, 14), calendar.monthBoundary(1201, MalayalamMonth.Medam).firstDay)
    }

    @Test fun `reading-time arguments are validated`() {
        assertFailsWith<IllegalArgumentException> { ReadingTime.FractionOfDaylight(1.5) }
        assertFailsWith<IllegalArgumentException> { ReadingTime.BeforeSunrise(-1) }
        assertFailsWith<IllegalArgumentException> {
            FestivalRule.TithiInMonth(
                MalayalamMonth.Karkidakam,
                io.github.anjalymsajeev.kollavarsham.panchangam.Tithi.KrishnaAmavasya,
                emptyList(),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            Festival(
                "bad id",
                "x",
                "x",
                ObservanceCategory.CUSTOM,
                FestivalRule.DaysFromEaster(0),
            )
        }
    }
}

class ObservanceProviderTest {
    private val csv =
        """
        # Example list
        date,id,english_name,malayalam_name,category
        2026-03-20,eid-ul-fitr,Eid ul-Fitr,ചെറിയ പെരുന്നാൾ,ISLAMIC

        2026-11-01,kerala-piravi,Kerala Piravi (state list),കേരളപ്പിറവി,CIVIC
        """.trimIndent()

    @Test fun `dated observances parse from CSV and filter by range`() {
        val dated = DatedObservances.fromCsv(csv)
        val all = dated.observances(LocalDate.of(2026, 1, 1)..LocalDate.of(2026, 12, 31))
        assertEquals(listOf("eid-ul-fitr", "kerala-piravi"), all.map { it.id })
        assertEquals(ObservanceSource.DATED_ENTRY, all.first().source)
        assertEquals(ObservanceCategory.ISLAMIC, all.first().category)
        assertEquals(1, dated.observancesOn(LocalDate.of(2026, 3, 20)).size)
    }

    @Test fun `malformed CSV reports the line`() {
        assertFailsWith<IllegalArgumentException> { DatedObservances.fromCsv("date,id\n") }
        val e =
            assertFailsWith<IllegalArgumentException> {
                DatedObservances.fromCsv("date,id,english_name,malayalam_name,category\n2026-02-30,x,X,X,CIVIC")
            }
        assertTrue(e.message!!.startsWith("Line 2"), e.message)
        assertFailsWith<IllegalArgumentException> {
            DatedObservances.fromCsv("date,id,english_name,malayalam_name,category\n2026-02-01,x,X,X,UNKNOWN")
        }
    }

    @Test fun `combined providers merge, sort, and let the first provider win`() {
        val combined = ObservanceProvider.combine(DatedObservances.fromCsv(csv), RuleBasedObservances(FestivalCalendar.create()))
        val nov = combined.observances(LocalDate.of(2026, 11, 1)..LocalDate.of(2026, 11, 30))
        val piravi = nov.filter { it.id == "kerala-piravi" }
        assertEquals(1, piravi.size)
        assertEquals(ObservanceSource.DATED_ENTRY, piravi.single().source)
        assertEquals(nov.sortedWith(compareBy({ it.date }, { it.id })), nov)
        assertTrue(nov.any { it.id == "mandala-kalam-start" && it.date == LocalDate.of(2026, 11, 17) })
    }

    @Test fun `rule-based provider covers ranges across ME years and ignores unsupported dates`() {
        val rules = RuleBasedObservances(FestivalCalendar.create())
        val span = rules.observances(LocalDate.of(2026, 7, 1)..LocalDate.of(2026, 9, 30))
        assertTrue(span.any { it.id == "karkidaka-vavu" && it.date == LocalDate.of(2026, 8, 12) }) // ME 1201
        assertTrue(span.any { it.id == "onam" && it.date == LocalDate.of(2026, 8, 26) }) // ME 1202
        assertTrue(rules.observances(LocalDate.of(1800, 1, 1)..LocalDate.of(1850, 1, 1)).isEmpty())
        assertTrue(rules.observances(LocalDate.of(2100, 12, 1)..LocalDate.of(2101, 3, 1)).all { it.date <= LocalDate.of(2100, 12, 31) })
    }
}
