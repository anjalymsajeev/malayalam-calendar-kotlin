package io.github.anjalymsajeev.kollavarsham.astronomy

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val IST: ZoneId = ZoneId.of("Asia/Kolkata")

private fun resourceLines(name: String): List<String> =
    AstronomyTest::class.java
        .getResourceAsStream("/reference/$name")!!
        .bufferedReader()
        .readLines()
        .drop(1)
        .filter { it.isNotBlank() }

class AstronomyTest {
    @Test fun `Meeus example 25b - apparent solar position on 1992 Oct 13 0h TD`() {
        val sun = Sun.position(2448908.5)
        // Meeus (full VSOP87 and full IAU 1980 nutation): lambda = 199°54'21.818", alpha = 198.378178°, delta = -7.783872°.
        // Our abridged nutation differs from the full series by ~0.1" here.
        val lambda = 199.0 + 54.0 / 60 + 21.818 / 3600
        assertTrue(abs(sun.longitude - lambda) * 3600 < 1.0, "longitude off by ${(sun.longitude - lambda) * 3600}\"")
        assertTrue(abs(sun.rightAscension - 198.378178) * 3600 < 1.5, "RA ${sun.rightAscension}")
        assertTrue(abs(sun.declination + 7.783872) * 3600 < 1.0, "dec ${sun.declination}")
    }

    @Test fun `Delta T matches published values`() {
        // Observed ΔT (seconds): 1900 -2.7, 1950 29.1, 1980 50.5, 2000 63.8, 2020 69.4. The fits are
        // within a few seconds; a 3-second error moves a sankranti by 3 seconds.
        mapOf(1900.0 to -2.7, 1950.0 to 29.1, 1980.0 to 50.5, 2000.0 to 63.8, 2020.0 to 69.4).forEach { (year, observed) ->
            assertTrue(abs(DeltaT.seconds(year) - observed) < 3.0, "ΔT($year) = ${DeltaT.seconds(year)}")
        }
        assertFailsWith<IllegalArgumentException> { DeltaT.seconds(1800.0) }
    }

    @Test fun `Julian day conversions round-trip`() {
        assertEquals(J2000, AstroTime.julianDayUt(Instant.parse("2000-01-01T12:00:00Z")))
        val instant = Instant.parse("2026-11-14T06:30:15.250Z")
        assertEquals(instant, AstroTime.instantOf(AstroTime.julianDayUt(instant)))
    }

    /*
     * The astronomy-engine reference scatters by about ±40 s (≈1.7") around precise ephemerides: a
     * one-off comparison of this engine with a Swiss Ephemeris (JPL-based) sankranti list for
     * 1900–2025 showed a standard deviation of about 1 s (VALIDATION_STRATEGY.md, Sprint 2 report).
     * The bounds below therefore test agreement with that reference's accuracy, not ours.
     */
    @Test fun `every sankranti 1899-2101 agrees with the independent reference, both conventions`() {
        var worst = Duration.ZERO
        var sumSquares = 0.0
        var count = 0
        resourceLines("sankranti-1899-2101.csv").forEach { line ->
            val (rashi, trueUtc, drikUtc) = line.split(',')
            for ((ayanamsa, expected) in listOf(
                Ayanamsa.LahiriTrue to Instant.parse(trueUtc),
                Ayanamsa.LahiriDrikCalibrated to Instant.parse(drikUtc),
            )) {
                val found = Sankranti.find(rashi.toInt(), expected.plus(Duration.ofDays(3)), ayanamsa)
                val diff = Duration.between(expected, found).abs()
                if (diff > worst) worst = diff
                sumSquares += (diff.toMillis() / 1000.0).let { it * it }
                count++
                assertTrue(diff < Duration.ofSeconds(90), "$ayanamsa rashi $rashi: expected $expected, got $found")
            }
        }
        val rms = kotlin.math.sqrt(sumSquares / count)
        println("sankranti vs reference: worst $worst, rms ${"%.1f".format(rms)} s")
        assertTrue(rms < 30.0, "rms $rms s")
    }

    @Test fun `sankranti longitude is exactly on the rashi boundary`() {
        val s = Sankranti.find(4, Instant.parse("2026-08-16T00:00:00Z"), Ayanamsa.LahiriTrue)
        val lon = Sankranti.siderealLongitude(s, Ayanamsa.LahiriTrue)
        assertTrue(abs(signedDegrees(lon - 120.0)) < 1e-5, "longitude $lon")
        assertFailsWith<IllegalArgumentException> { Sankranti.find(12, s, Ayanamsa.LahiriTrue) }
    }

    @Test fun `sunrise and sunset at Thiruvananthapuram agree with the reference within 1 minute`() {
        var worst = Duration.ZERO
        resourceLines("sunrise-sunset-tvm.csv").forEach { line ->
            val (date, rise, set) = line.split(',')
            val d = LocalDate.parse(date)
            val myRise = SunriseSunset.sunrise(d, 8.5241, 76.9366, IST)!!
            val mySet = SunriseSunset.sunset(d, 8.5241, 76.9366, IST)!!
            listOf(Duration.between(Instant.parse(rise), myRise).abs(), Duration.between(Instant.parse(set), mySet).abs()).forEach {
                if (it > worst) worst = it
                assertTrue(it < Duration.ofMinutes(1), "$date: $it")
            }
        }
        println("worst sunrise/sunset difference vs reference: $worst")
    }

    @Test fun `no sunrise during polar night`() {
        assertNull(SunriseSunset.sunrise(LocalDate.of(2026, 12, 21), 80.0, 0.0, ZoneId.of("UTC")))
    }
}

class MoonTest {
    @Test fun `Meeus example 47a - Moon on 1992 April 12 0h TD`() {
        val moon = Moon.position(2448724.5)
        // Meeus: apparent longitude 133.167265° (geometric 133.162655° + nutation 0.004610°),
        // latitude -3.229126°, distance 368409.7 km. Our abridged nutation differs by ~0.1".
        assertTrue(abs(moon.longitude - 133.167265) * 3600 < 1.0, "longitude ${moon.longitude}")
        assertTrue(abs(moon.latitude + 3.229126) * 3600 < 0.5, "latitude ${moon.latitude}")
        assertTrue(abs(moon.distanceKm - 368409.7) < 0.5, "distance ${moon.distanceKm}")
    }
}
