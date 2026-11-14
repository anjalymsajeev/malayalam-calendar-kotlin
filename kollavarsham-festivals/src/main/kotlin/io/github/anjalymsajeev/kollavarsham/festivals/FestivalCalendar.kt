package io.github.anjalymsajeev.kollavarsham.festivals

import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.core.MalayalamDate
import io.github.anjalymsajeev.kollavarsham.core.MonthBoundary
import io.github.anjalymsajeev.kollavarsham.panchangam.NakshatraProvider
import io.github.anjalymsajeev.kollavarsham.panchangam.Panchangam
import io.github.anjalymsajeev.kollavarsham.panchangam.SunTimesProvider
import io.github.anjalymsajeev.kollavarsham.panchangam.Tithi
import io.github.anjalymsajeev.kollavarsham.panchangam.TithiProvider
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Evaluates [FestivalRule]s for Kollavarsham years. Immutable and thread-safe.
 *
 * The panchangam inputs are interfaces, so rules can be tested with fakes and an alternative
 * panchangam can be supplied.
 *
 * ```kotlin
 * val festivals = FestivalCalendar.create()
 * festivals.dateOf(KeralaFestivals.ONAM, 1201)        // 2025-09-05
 * festivals.festivalsIn(1202).forEach { println("${it.date} ${it.englishName}") }
 * ```
 *
 * @property calendar the Kollavarsham calendar used for month boundaries.
 * @property definitions the festivals this calendar knows; [DaysFrom][FestivalRule.DaysFrom] rules refer to these by id.
 */
public class FestivalCalendar(
    public val calendar: KollavarshamCalendar,
    private val nakshatras: NakshatraProvider,
    private val tithis: TithiProvider,
    private val sunTimes: SunTimesProvider,
    private val zone: ZoneId = KollavarshamCalendar.INDIAN_STANDARD_TIME,
    public val definitions: List<Festival> = KeralaFestivals.all,
) {
    private val byId: Map<String, Festival> = definitions.associateBy { it.id }

    init {
        require(byId.size == definitions.size) { "Duplicate festival ids in definitions" }
        definitions.forEach { festival ->
            val rule = festival.rule
            if (rule is FestivalRule.DaysFrom) {
                require(rule.festivalId in byId) { "${festival.id} refers to unknown festival ${rule.festivalId}" }
            }
        }
    }

    /**
     * The date of [festival] in Kollavarsham year [year] (Chingam 1 to the end of Karkidakam), or
     * `null` if the rule matches no day that year (for example a tithi that is skipped).
     *
     * @throws io.github.anjalymsajeev.kollavarsham.core.DateOutOfSupportedRangeException outside the supported years.
     */
    public fun dateOf(
        festival: Festival,
        year: Int,
    ): LocalDate? = evaluate(festival.rule, year, depth = 0)

    /**
     * Every day in Kollavarsham [year] that satisfies [festival]'s rule before the rule's
     * [Occurrence] picks one. Usually a single day; two when, for example, a nakshatra occurs
     * twice in the month, which is where sources sometimes disagree. Apps can use this to show
     * "may also be observed on …".
     *
     * ```kotlin
     * festivals.candidates(KeralaFestivals.THRISSUR_POORAM, 1202)   // [2027-04-17, 2027-05-14]
     * ```
     */
    public fun candidates(
        festival: Festival,
        year: Int,
    ): List<LocalDate> = candidates(festival.rule, year, depth = 0)

    private fun candidates(
        rule: FestivalRule,
        year: Int,
        depth: Int,
    ): List<LocalDate> {
        check(depth < MAX_RULE_DEPTH) { "Festival rules refer to each other in a cycle" }
        return when (rule) {
            is FestivalRule.NakshatraInMonth ->
                days(calendar.monthBoundary(year, rule.month)).filter {
                    nakshatras.nakshatraAt(instant(it, rule.readingTime)) ==
                        rule.nakshatra
                }
            is FestivalRule.TithiInMonth -> {
                val days = days(calendar.monthBoundary(year, rule.month))
                rule.readingTimes
                    .asSequence()
                    .map { time -> days.filter { tithis.tithiAt(instant(it, time)) == rule.tithi } }
                    .firstOrNull { it.isNotEmpty() } ?: emptyList()
            }
            is FestivalRule.TithiInLunarMonth ->
                lunarMonthDays(year, rule).filter { tithis.tithiAt(instant(it, rule.readingTime)) == rule.tithi }
            is FestivalRule.DaysFrom ->
                candidates(
                    byId.getValue(rule.festivalId).rule,
                    year,
                    depth + 1,
                ).map { it.plusDays(rule.days.toLong()) }
            else -> listOfNotNull(evaluate(rule, year, depth))
        }
    }

    private fun lunarMonthDays(
        year: Int,
        rule: FestivalRule.TithiInLunarMonth,
    ): List<LocalDate> {
        val (newMoon, nextNewMoon) = lunarMonthContaining(calendar.monthBoundary(year, rule.sankrantiMonth).sankranti)
        return generateSequence(newMoon.atZone(zone).toLocalDate()) { it.plusDays(1) }
            .takeWhile { !it.isAfter(nextNewMoon.atZone(zone).toLocalDate()) }
            .filter {
                val t = instant(it, rule.readingTime)
                !t.isBefore(newMoon) && t.isBefore(nextNewMoon)
            }.toList()
    }

    /**
     * All [definitions] that occur in Kollavarsham [year], sorted by date.
     *
     * ```kotlin
     * festivals.festivalsIn(1202).first()   // Malayalam New Year, 2026-08-17
     * ```
     */
    public fun festivalsIn(year: Int): List<Observance> =
        definitions.mapNotNull { f -> dateOf(f, year)?.let { observance(f, it) } }.sortedWith(compareBy({ it.date }, { it.id }))

    /** The observance of [festival] on [date]. */
    internal fun observance(
        festival: Festival,
        date: LocalDate,
    ): Observance = Observance(date, festival.id, festival.englishName, festival.malayalamName, festival.category, ObservanceSource.RULE)

    private fun evaluate(
        rule: FestivalRule,
        year: Int,
        depth: Int,
    ): LocalDate? {
        check(depth < MAX_RULE_DEPTH) { "Festival rules refer to each other in a cycle" }
        val yearRange = calendar.monthsOf(year).let { it.first().firstDay..it.last().lastDay }
        return when (rule) {
            is FestivalRule.OnMalayalamDate -> calendar.toGregorianOrNull(MalayalamDate(year, rule.month, rule.day))
            is FestivalRule.OnGregorianDate ->
                listOf(year + 824, year + 825).map { LocalDate.of(it, rule.month, rule.day) }.firstOrNull { it in yearRange }
            is FestivalRule.FirstSunriseAfterSankranti -> {
                val boundary = calendar.monthBoundary(year, rule.month)
                val day = boundary.sankranti.atZone(zone).toLocalDate()
                val sunrise = sunrise(day)
                if (boundary.sankranti <= sunrise) day else day.plusDays(1)
            }
            is FestivalRule.NakshatraInMonth ->
                pick(days(calendar.monthBoundary(year, rule.month)), rule.occurrence) {
                    nakshatras.nakshatraAt(instant(it, rule.readingTime)) ==
                        rule.nakshatra
                }
            is FestivalRule.TithiInMonth -> {
                val days = days(calendar.monthBoundary(year, rule.month))
                rule.readingTimes.firstNotNullOfOrNull { time ->
                    pick(days, rule.occurrence) { tithis.tithiAt(instant(it, time)) == rule.tithi }
                }
            }
            is FestivalRule.TithiInLunarMonth ->
                pick(lunarMonthDays(year, rule), rule.occurrence) { tithis.tithiAt(instant(it, rule.readingTime)) == rule.tithi }
            is FestivalRule.FirstTithiOnOrAfter -> {
                val start = calendar.toGregorian(MalayalamDate(year, rule.month, rule.day))
                generateSequence(start) { it.plusDays(1) }
                    .take(LUNAR_MONTH_DAYS + 1)
                    .firstOrNull { tithis.tithiAt(instant(it, rule.readingTime)) == rule.tithi }
            }
            is FestivalRule.DaysFrom ->
                evaluate(byId.getValue(rule.festivalId).rule, year, depth + 1)?.plusDays(rule.days.toLong())
            is FestivalRule.DaysFromEaster -> easter(year + 825).plusDays(rule.days.toLong())
        }
    }

    private fun days(boundary: MonthBoundary): List<LocalDate> =
        generateSequence(boundary.firstDay) { it.plusDays(1) }.takeWhile { !it.isAfter(boundary.lastDay) }.toList()

    private fun pick(
        days: List<LocalDate>,
        occurrence: Occurrence,
        matches: (LocalDate) -> Boolean,
    ): LocalDate? = if (occurrence == Occurrence.FIRST) days.firstOrNull(matches) else days.lastOrNull(matches)

    private fun sunrise(day: LocalDate): Instant = checkNotNull(sunTimes.sunrise(day)) { "No sunrise on $day" }

    private fun instant(
        day: LocalDate,
        time: ReadingTime,
    ): Instant =
        when (time) {
            ReadingTime.Sunrise -> sunrise(day)
            ReadingTime.Sunset -> checkNotNull(sunTimes.sunset(day)) { "No sunset on $day" }
            ReadingTime.Midnight -> day.plusDays(1).atStartOfDay(zone).toInstant()
            is ReadingTime.FractionOfDaylight -> {
                val rise = sunrise(day)
                val set = checkNotNull(sunTimes.sunset(day)) { "No sunset on $day" }
                rise.plusMillis((Duration.between(rise, set).toMillis() * time.fraction).toLong())
            }
            is ReadingTime.BeforeSunrise -> sunrise(day).minus(Duration.ofMinutes(time.minutes))
        }

    /** The new moons (starts of Shukla Prathama) at or before and after [instant]. */
    private fun lunarMonthContaining(instant: Instant): Pair<Instant, Instant> {
        var span = tithis.tithiSpan(instant)
        var steps = 0
        while (span.value != Tithi.ShuklaPrathama) {
            check(++steps <= Tithi.COUNT) { "No new moon found before $instant" }
            span = tithis.tithiSpan(span.start.minusSeconds(1))
        }
        val start = span.start
        span = tithis.tithiSpan(span.end.plusSeconds(1))
        steps = 0
        while (span.value != Tithi.ShuklaPrathama) {
            check(++steps <= Tithi.COUNT) { "No new moon found after $instant" }
            span = tithis.tithiSpan(span.end.plusSeconds(1))
        }
        return start to span.start
    }

    public companion object {
        private const val MAX_RULE_DEPTH = 8
        private const val LUNAR_MONTH_DAYS = 30

        private val default: FestivalCalendar by lazy {
            val panchangam = Panchangam.create()
            FestivalCalendar(KollavarshamCalendar.create(), panchangam, panchangam, panchangam)
        }

        /**
         * The festival calendar for Kerala: the default Kollavarsham calendar, the default
         * panchangam (Thiruvananthapuram) and [KeralaFestivals.all].
         */
        public fun create(): FestivalCalendar = default

        /**
         * Western Easter Sunday in [gregorianYear], by the anonymous Gregorian computus
         * (Meeus, Astronomical Algorithms, ch. 8).
         */
        public fun easter(gregorianYear: Int): LocalDate {
            val y = gregorianYear
            val a = y % 19
            val b = y / 100
            val c = y % 100
            val d = b / 4
            val e = b % 4
            val f = (b + 8) / 25
            val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30
            val i = c / 4
            val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7
            val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31
            val day = (h + l - 7 * m + 114) % 31 + 1
            return LocalDate.of(y, month, day)
        }
    }
}
