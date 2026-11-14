package io.github.anjalymsajeev.kollavarsham.locale

import io.github.anjalymsajeev.kollavarsham.core.InvalidMalayalamDateException
import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.core.MalayalamDate
import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DateFormattingTest {
    private val date = MalayalamDate(1202, MalayalamMonth.Thulam, 28) // 2026-11-14, a Saturday (Shani)
    private val calendar = KollavarshamCalendar.create()

    @Test fun `predefined styles in English`() {
        assertEquals("28/03/1202", date.format(CalendarLocale.ENGLISH, DateStyle.SHORT))
        assertEquals("28 Thulam 1202", date.format(CalendarLocale.ENGLISH))
        assertEquals("28 Thulam 1202 ME", date.format(CalendarLocale.ENGLISH, DateStyle.LONG))
        assertEquals("Shani, 28 Thulam 1202 ME", date.format(CalendarLocale.ENGLISH, DateStyle.FULL, calendar))
    }

    @Test fun `predefined styles in Malayalam`() {
        assertEquals("28/03/1202", date.format(CalendarLocale.MALAYALAM, DateStyle.SHORT))
        assertEquals("1202 തുലാം 28", date.format(CalendarLocale.MALAYALAM))
        assertEquals("കൊല്ലവർഷം 1202 തുലാം 28", date.format(CalendarLocale.MALAYALAM, DateStyle.LONG))
        assertEquals("കൊല്ലവർഷം 1202 തുലാം 28, ശനി", date.format(CalendarLocale.MALAYALAM, DateStyle.FULL, calendar))
    }

    @Test fun `Malayalam digits and ISO transliteration`() {
        assertEquals("൧൨൦൨ തുലാം ൨൮", date.format(CalendarLocale.MALAYALAM, numerals = NumeralSystem.MALAYALAM))
        val iso = KollavarshamDateFormatter.ofStyle(DateStyle.FULL, CalendarLocale.ENGLISH, transliteration = Transliteration.ISO_15919)
        assertEquals("Śani, 28 Tulāṁ 1202 ME", iso.format(date, calendar))
    }

    @Test fun `custom patterns, padding and literals`() {
        val d = MalayalamDate(1202, MalayalamMonth.Chingam, 5)
        assertEquals("05-01-1202", KollavarshamDateFormatter.ofPattern("dd-MM-y").format(d))
        assertEquals("5/1/1202", KollavarshamDateFormatter.ofPattern("d/M/yyyy").format(d))
        assertEquals("01202", KollavarshamDateFormatter.ofPattern("yyyyy").format(d))
        assertEquals("Day 5 of Chingam, year 1202", KollavarshamDateFormatter.ofPattern("'Day' d 'of' MMMM, 'year' y").format(d))
        assertEquals("It's 5", KollavarshamDateFormatter.ofPattern("'It''s' d").format(d))
        assertEquals("'5", KollavarshamDateFormatter.ofPattern("''d").format(d))
        assertEquals(
            "൫ ചിങ്ങം",
            KollavarshamDateFormatter.ofPattern("d MMMM", CalendarLocale.MALAYALAM).withNumerals(NumeralSystem.MALAYALAM).format(d),
        )
    }

    @Test fun `invalid patterns are rejected`() {
        for (bad in listOf("MMM", "ddd", "EEE", "GG", "h:mm", "'open")) {
            assertFailsWith<IllegalArgumentException>(bad) { KollavarshamDateFormatter.ofPattern(bad) }
        }
    }

    @Test fun `weekday requires a calendar and a valid date`() {
        val full = KollavarshamDateFormatter.ofStyle(DateStyle.FULL, CalendarLocale.ENGLISH)
        assertTrue(full.needsWeekday)
        assertFalse(KollavarshamDateFormatter.ofStyle(DateStyle.LONG, CalendarLocale.ENGLISH).needsWeekday)
        assertFailsWith<IllegalStateException> { full.format(date) }
        assertFailsWith<InvalidMalayalamDateException> { full.format(MalayalamDate(1202, MalayalamMonth.Thulam, 31), calendar) }
        // Without a weekday token, the calendar is not consulted and any structurally valid date formats.
        assertEquals(
            "31 Thulam 1202",
            KollavarshamDateFormatter.ofPattern("d MMMM y").format(MalayalamDate(1202, MalayalamMonth.Thulam, 31), calendar),
        )
    }
}
