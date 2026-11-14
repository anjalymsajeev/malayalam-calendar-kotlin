package io.github.anjalymsajeev.kollavarsham.sample

import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalendarPresenterTest {
    private val presenter = CalendarPresenter()

    @Test
    fun onamDetails() {
        val d = presenter.details(LocalDate.of(2026, 8, 26), CalendarLocale.ENGLISH)
        assertEquals("Thiruvonam", d.nakshatra)
        assertTrue("Onam (Thiruvonam)" in d.festivals, d.festivals.toString())
        assertTrue(d.malayalamDate.endsWith("1202 ME"), d.malayalamDate)
    }

    @Test
    fun malayalamTitleAndFestivalDots() {
        assertEquals("തുലാം – വൃശ്ചികം 1202", presenter.malayalamMonthsTitle(YearMonth.of(2026, 11), CalendarLocale.MALAYALAM))
        val dots = presenter.festivalDates(YearMonth.of(2026, 11))
        assertTrue(LocalDate.of(2026, 11, 1) in dots) // Kerala Piravi
        assertTrue(LocalDate.of(2026, 11, 17) in dots) // Mandala season begins
    }
}
