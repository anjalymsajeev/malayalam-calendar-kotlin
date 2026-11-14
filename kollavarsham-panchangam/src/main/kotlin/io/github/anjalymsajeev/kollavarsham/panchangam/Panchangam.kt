package io.github.anjalymsajeev.kollavarsham.panchangam

import io.github.anjalymsajeev.kollavarsham.astronomy.AstroTime
import io.github.anjalymsajeev.kollavarsham.astronomy.Ayanamsa
import io.github.anjalymsajeev.kollavarsham.astronomy.Moon
import io.github.anjalymsajeev.kollavarsham.astronomy.Sun
import io.github.anjalymsajeev.kollavarsham.astronomy.SunriseSunset
import io.github.anjalymsajeev.kollavarsham.core.GeoLocation
import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.floor

/**
 * Where panchangam elements are observed.
 *
 * @property geo coordinates used for sunrise and sunset.
 * @property zone the civil time zone that defines calendar days.
 */
public data class PanchangamLocation(
    val geo: GeoLocation = GeoLocation.Thiruvananthapuram,
    val zone: ZoneId = KollavarshamCalendar.INDIAN_STANDARD_TIME,
) {
    public companion object {
        /** Thiruvananthapuram in Indian Standard Time: the default. */
        public val Default: PanchangamLocation = PanchangamLocation()
    }
}

/**
 * One civil day's panchangam, using the Kerala convention of reading the day's elements at
 * sunrise. All elements that occur between this sunrise and the next are listed as well, so
 * other conventions (for example the "6 nazhika" rule for nakshatra) can be applied by the caller.
 *
 * @property date the civil date.
 * @property sunrise sunrise on [date].
 * @property sunset sunset on [date].
 * @property nextSunrise sunrise on the following date; the Hindu day runs from [sunrise] to here.
 * @property nakshatraAtSunrise the nakshatra in force at [sunrise].
 * @property tithiAtSunrise the tithi in force at [sunrise].
 * @property nakshatras every nakshatra overlapping [sunrise]..[nextSunrise], in order.
 * @property tithis every tithi overlapping [sunrise]..[nextSunrise], in order.
 */
public data class PanchangamDay(
    val date: LocalDate,
    val sunrise: Instant,
    val sunset: Instant,
    val nextSunrise: Instant,
    val nakshatraAtSunrise: Nakshatra,
    val tithiAtSunrise: Tithi,
    val nakshatras: List<Span<Nakshatra>>,
    val tithis: List<Span<Tithi>>,
)

/**
 * Nakshatra, tithi and sunrise/sunset calculations. Immutable and thread-safe.
 *
 * Nakshatras use the Moon's sidereal longitude with the same Lahiri convention as the calendar
 * (ADR-0002). Tithis use the Moon–Sun elongation, which does not depend on the ayanamsa.
 * Boundary times are accurate to about half a minute (ELP-2000/82 abridged and VSOP87).
 *
 * ```kotlin
 * val panchangam = Panchangam.create()
 * val day = panchangam.day(LocalDate.of(2025, 9, 5))
 * day.nakshatraAtSunrise           // Thiruvonam (Onam 2025)
 * panchangam.tithiAt(Instant.now())
 * ```
 *
 * @property location where sunrise and sunset are computed.
 */
public class Panchangam private constructor(
    public val location: PanchangamLocation,
) : NakshatraProvider,
    TithiProvider,
    SunTimesProvider {
    override fun nakshatraAt(instant: Instant): Nakshatra =
        Nakshatra.atSiderealLongitude(siderealMoonLongitude(AstroTime.julianDayUt(instant)))

    override fun nakshatraSpan(instant: Instant): Span<Nakshatra> {
        val (index, start, end) = span(instant, ::siderealMoonLongitude, Nakshatra.ARC_DEGREES, Nakshatra.COUNT, MOON_DEGREES_PER_DAY)
        return Span(Nakshatra.entries[index], start, end)
    }

    override fun tithiAt(instant: Instant): Tithi = Tithi.atElongation(elongation(AstroTime.julianDayUt(instant)))

    override fun tithiSpan(instant: Instant): Span<Tithi> {
        val (index, start, end) = span(instant, ::elongation, Tithi.ARC_DEGREES, Tithi.COUNT, ELONGATION_DEGREES_PER_DAY)
        return Span(Tithi.entries[index], start, end)
    }

    override fun sunrise(date: LocalDate): Instant? =
        SunriseSunset.sunrise(date, location.geo.latitude, location.geo.longitude, location.zone)

    override fun sunset(date: LocalDate): Instant? =
        SunriseSunset.sunset(date, location.geo.latitude, location.geo.longitude, location.zone)

    /**
     * The panchangam of the civil [date], read at sunrise.
     *
     * @throws IllegalStateException if the Sun does not rise or set at this location on [date].
     */
    public fun day(date: LocalDate): PanchangamDay {
        val rise = checkNotNull(sunrise(date)) { "No sunrise on $date at ${location.geo}" }
        val set = checkNotNull(sunset(date)) { "No sunset on $date at ${location.geo}" }
        val nextRise = checkNotNull(sunrise(date.plusDays(1))) { "No sunrise on ${date.plusDays(1)} at ${location.geo}" }
        return PanchangamDay(
            date = date,
            sunrise = rise,
            sunset = set,
            nextSunrise = nextRise,
            nakshatraAtSunrise = nakshatraAt(rise),
            tithiAtSunrise = tithiAt(rise),
            nakshatras = spansBetween(rise, nextRise, ::nakshatraSpan),
            tithis = spansBetween(rise, nextRise, ::tithiSpan),
        )
    }

    private fun <T> spansBetween(
        from: Instant,
        until: Instant,
        span: (Instant) -> Span<T>,
    ): List<Span<T>> {
        val result = mutableListOf<Span<T>>()
        var current = span(from)
        result += current
        while (current.end.isBefore(until)) {
            // Probe just past the end, so millisecond rounding can never return the same span,
            // then join the spans exactly (their boundaries agree to within milliseconds).
            val next = span(current.end.plusSeconds(1))
            current = if (Duration.between(current.end, next.start).abs() < Duration.ofSeconds(1)) next.copy(start = current.end) else next
            result += current
        }
        return result
    }

    private fun siderealMoonLongitude(jdUt: Double): Double {
        val jde = AstroTime.julianEphemerisDay(jdUt)
        val moon = Moon.position(jde)
        return normalize(moon.longitude - AYANAMSA.degrees(jde, moon.nutationInLongitude))
    }

    private fun elongation(jdUt: Double): Double {
        val jde = AstroTime.julianEphemerisDay(jdUt)
        return normalize(Moon.position(jde).longitude - Sun.position(jde).longitude)
    }

    /**
     * The division (of [arc] degrees, [count] per circle) of the increasing angle [f] in force at
     * [instant], with its exact start and end. Near a boundary, rounding can place [instant] just
     * outside the computed span; the neighbouring division is then used instead.
     */
    private fun span(
        instant: Instant,
        f: (Double) -> Double,
        arc: Double,
        count: Int,
        rate: Double,
    ): Triple<Int, Instant, Instant> {
        val jd = AstroTime.julianDayUt(instant)
        var index = floor(f(jd) / arc).toInt().coerceIn(0, count - 1)
        repeat(3) {
            val start = AstroTime.instantOf(crossing(f, index * arc, jd, rate))
            val end = AstroTime.instantOf(crossing(f, ((index + 1) % count) * arc, jd, rate))
            when {
                instant.isBefore(start) -> index = (index + count - 1) % count
                !instant.isBefore(end) -> index = (index + 1) % count
                else -> return Triple(index, start, end)
            }
        }
        error("Could not bracket $instant")
    }

    /**
     * Solves `f(jd) = target` (an angle) near [estimate] by the secant method, for a function
     * increasing at roughly [rate] degrees per day. Converges in about five evaluations.
     */
    private fun crossing(
        f: (Double) -> Double,
        target: Double,
        estimate: Double,
        rate: Double,
    ): Double {
        var x0 = estimate
        var e0 = signed(f(x0) - target)
        var x1 = x0 - e0 / rate
        repeat(MAX_ITERATIONS) {
            val e1 = signed(f(x1) - target)
            if (abs(e1) < ANGLE_TOLERANCE || e1 == e0) return x1
            val x2 = x1 - e1 * (x1 - x0) / (e1 - e0)
            x0 = x1
            e0 = e1
            x1 = x2
        }
        error("Crossing of $target° near JD $estimate did not converge")
    }

    private fun normalize(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0

    private fun signed(degrees: Double): Double = normalize(degrees + 180.0) - 180.0

    public companion object {
        private val AYANAMSA = Ayanamsa.LahiriTrue
        private const val MOON_DEGREES_PER_DAY = 13.176
        private const val ELONGATION_DEGREES_PER_DAY = 12.19

        // 1e-8° is under a millisecond of lunar motion, so a boundary comes out the same (to the ms)
        // whatever probe instant it was searched from.
        private const val ANGLE_TOLERANCE = 1e-8
        private const val MAX_ITERATIONS = 30

        private val default: Panchangam by lazy { Panchangam(PanchangamLocation.Default) }

        /**
         * A panchangam for [location] (default: Thiruvananthapuram, IST).
         *
         * ```kotlin
         * val kozhikode = Panchangam.create(PanchangamLocation(GeoLocation(11.2588, 75.7804, "Kozhikode")))
         * ```
         */
        public fun create(location: PanchangamLocation = PanchangamLocation.Default): Panchangam =
            if (location == PanchangamLocation.Default) default else Panchangam(location)
    }
}
