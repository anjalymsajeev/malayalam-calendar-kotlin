package io.github.anjalymsajeev.kollavarsham.festivals

import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import io.github.anjalymsajeev.kollavarsham.panchangam.Nakshatra
import io.github.anjalymsajeev.kollavarsham.panchangam.Tithi
import java.time.Month

/**
 * When during a civil day a nakshatra or tithi is read. Traditional rules differ by festival:
 * most read at sunrise, some at sunset, mid-day or midnight.
 */
public sealed interface ReadingTime {
    /** Local sunrise. */
    public data object Sunrise : ReadingTime

    /** Local sunset. */
    public data object Sunset : ReadingTime

    /** Local midnight at the end of the civil day (the night that follows the day). */
    public data object Midnight : ReadingTime

    /** A fraction of daylight after sunrise, for example 0.5 for mid-day or 0.8 for late afternoon. */
    public data class FractionOfDaylight(
        val fraction: Double,
    ) : ReadingTime {
        init {
            require(fraction in 0.0..1.0) { "fraction must be in 0..1, was $fraction" }
        }
    }

    /** A fixed number of minutes before sunrise, for example 96 minutes (two muhurtas) for dawn. */
    public data class BeforeSunrise(
        val minutes: Long,
    ) : ReadingTime {
        init {
            require(minutes >= 0) { "minutes must not be negative, was $minutes" }
        }
    }
}

/** Which day to take when a rule matches more than one day in its window. */
public enum class Occurrence { FIRST, LAST }

/**
 * How a festival's date is found each year. Rules never store dates; they are evaluated against
 * the calendar and the panchangam for each Kollavarsham (ME) year.
 *
 * A rule's window is one ME year, Chingam 1 to the last day of Karkidakam, unless stated otherwise.
 */
public sealed interface FestivalRule {
    /**
     * A fixed Malayalam date, for example Chingam 1 (Malayalam New Year).
     *
     * ```kotlin
     * FestivalRule.OnMalayalamDate(MalayalamMonth.Chingam, 1)
     * ```
     */
    public data class OnMalayalamDate(
        val month: MalayalamMonth,
        val day: Int,
    ) : FestivalRule {
        init {
            require(day in 1..32) { "day must be in 1..32, was $day" }
        }
    }

    /**
     * A fixed Gregorian date that falls within the ME year, for example 1 November (Kerala Piravi).
     *
     * ```kotlin
     * FestivalRule.OnGregorianDate(Month.NOVEMBER, 1)
     * ```
     */
    public data class OnGregorianDate(
        val month: Month,
        val day: Int,
    ) : FestivalRule {
        init {
            require(day in 1..month.maxLength()) { "$month has no day $day" }
        }
    }

    /**
     * The day of the sankranti of [month] if the sankranti is at or before that day's sunrise,
     * otherwise the next day: the first sunrise after the Sun enters the rashi (Vishu for Medam).
     */
    public data class FirstSunriseAfterSankranti(
        val month: MalayalamMonth,
    ) : FestivalRule

    /**
     * The day in [month] whose [readingTime] falls in [nakshatra], for example Thiruvonam at
     * sunrise in Chingam (Onam). If several days match, [occurrence] chooses.
     */
    public data class NakshatraInMonth(
        val month: MalayalamMonth,
        val nakshatra: Nakshatra,
        val readingTime: ReadingTime = ReadingTime.Sunrise,
        val occurrence: Occurrence = Occurrence.FIRST,
    ) : FestivalRule

    /**
     * The day in Malayalam [month] whose tithi is [tithi] at the first of [readingTimes] that
     * matches any day (later reading times are fallbacks), for example Karkidaka Vavu.
     */
    public data class TithiInMonth(
        val month: MalayalamMonth,
        val tithi: Tithi,
        val readingTimes: List<ReadingTime> = listOf(ReadingTime.Sunrise),
        val occurrence: Occurrence = Occurrence.FIRST,
    ) : FestivalRule {
        init {
            require(readingTimes.isNotEmpty()) { "at least one reading time is required" }
        }
    }

    /**
     * The day whose tithi at [readingTime] is [tithi], within the lunar month (new moon to new
     * moon) that contains the sankranti of [sankrantiMonth]. Lunar months are named this way,
     * so Sivaratri (the lunar month holding Kumbham's sankranti) can fall in Makaram.
     */
    public data class TithiInLunarMonth(
        val sankrantiMonth: MalayalamMonth,
        val tithi: Tithi,
        val readingTime: ReadingTime = ReadingTime.Sunrise,
        val occurrence: Occurrence = Occurrence.FIRST,
    ) : FestivalRule

    /**
     * The first day on or after [month] [day] whose tithi at [readingTime] is [tithi], for
     * example the first Shukla Ekadashi at sunrise from Vrischikam 1 (Guruvayur Ekadashi).
     */
    public data class FirstTithiOnOrAfter(
        val month: MalayalamMonth,
        val day: Int,
        val tithi: Tithi,
        val readingTime: ReadingTime = ReadingTime.Sunrise,
    ) : FestivalRule

    /** [days] days after (or before, if negative) another festival in the same ME year. */
    public data class DaysFrom(
        val festivalId: String,
        val days: Int,
    ) : FestivalRule

    /** [days] days after (or before) Western Easter (Gregorian computus) in the ME year. */
    public data class DaysFromEaster(
        val days: Int,
    ) : FestivalRule
}
