package io.github.anjalymsajeev.kollavarsham.core

import io.github.anjalymsajeev.kollavarsham.core.boundary.VerifiedOverrides
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OverridesTest {
    private val key = MonthKey(1197, MalayalamMonth.Thulam)
    private val computed = SyntheticBoundarySource.calendar().monthBoundary(key.year, key.month).firstDay

    @Test fun `verified override moves the boundary and shortens the previous month`() {
        val calendar = SyntheticBoundarySource.calendar(verified = mapOf(key to computed.minusDays(1)))
        val thulam = calendar.monthBoundary(1197, MalayalamMonth.Thulam)
        assertEquals(computed.minusDays(1), thulam.firstDay)
        assertEquals(BoundarySource.VerifiedOverride, thulam.source)
        val kanni = calendar.monthBoundary(1197, MalayalamMonth.Kanni)
        assertEquals(thulam.firstDay.minusDays(1), kanni.lastDay)
        assertEquals(BoundarySource.Computed, kanni.source)
        assertEquals(MalayalamDate(1197, MalayalamMonth.Thulam, 1), calendar.fromGregorian(computed.minusDays(1)))
    }

    @Test fun `user override wins over verified override`() {
        val calendar = SyntheticBoundarySource.calendar(
            configuration = CalendarConfiguration(monthStartOverrides = mapOf(key to computed.plusDays(1))),
            verified = mapOf(key to computed.minusDays(1)),
        )
        val thulam = calendar.monthBoundary(1197, MalayalamMonth.Thulam)
        assertEquals(computed.plusDays(1), thulam.firstDay)
        assertEquals(BoundarySource.UserOverride, thulam.source)
    }

    @Test fun `verified overrides can be switched off`() {
        val calendar = SyntheticBoundarySource.calendar(
            configuration = CalendarConfiguration(applyVerifiedOverrides = false),
            verified = mapOf(key to computed.minusDays(1)),
        )
        assertEquals(computed, calendar.monthBoundary(1197, MalayalamMonth.Thulam).firstDay)
    }

    @Test fun `overrides producing impossible month lengths fail at construction`() {
        val error = assertFailsWith<IllegalArgumentException> {
            SyntheticBoundarySource.calendar(
                configuration = CalendarConfiguration(monthStartOverrides = mapOf(key to computed.plusDays(10))),
            )
        }
        assertTrue(error.message!!.contains("29..32"))
    }

    @Test fun `bundled overrides file loads and every row needs a citation`() {
        assertEquals(LocalDate.of(2021, 10, 17), VerifiedOverrides.bundled[key])
        val parsed = VerifiedOverrides.parse(
            "# comment\nme_year,month,first_day,source_citation\n1197,Thulam,2021-10-17,Example almanac p. 3\n",
        )
        assertEquals(mapOf(key to LocalDate.of(2021, 10, 17)), parsed)
        assertFailsWith<IllegalArgumentException> {
            VerifiedOverrides.parse("me_year,month,first_day,source_citation\n1197,Thulam,2021-10-17,\n")
        }
        assertFailsWith<IllegalArgumentException> { VerifiedOverrides.parse("year,month\n") }
    }
}
