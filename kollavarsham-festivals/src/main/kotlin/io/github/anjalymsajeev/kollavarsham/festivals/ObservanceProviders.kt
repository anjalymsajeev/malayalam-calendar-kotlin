package io.github.anjalymsajeev.kollavarsham.festivals

import java.time.LocalDate

/**
 * A source of observances for a date range. Apps combine several: rule-based festivals, a
 * government holiday list, a temple's calendar, personal events.
 *
 * ```kotlin
 * val provider = ObservanceProvider.combine(
 *     RuleBasedObservances(FestivalCalendar.create()),
 *     DatedObservances.fromCsv(holidaysCsv),
 * )
 * provider.observances(LocalDate.of(2026, 1, 1)..LocalDate.of(2026, 12, 31))
 * ```
 */
public fun interface ObservanceProvider {
    /** Observances on dates within [range], sorted by date then id. */
    public fun observances(range: ClosedRange<LocalDate>): List<Observance>

    /** Observances on [date]. */
    public fun observancesOn(date: LocalDate): List<Observance> = observances(date..date)

    public companion object {
        /**
         * A provider that merges [providers]. When two give the same id on the same date, the
         * first provider wins, so a dated entry can override a computed festival.
         */
        public fun combine(vararg providers: ObservanceProvider): ObservanceProvider =
            ObservanceProvider { range ->
                providers
                    .flatMap { it.observances(range) }
                    .distinctBy { it.date to it.id }
                    .sortedWith(ORDER)
            }

        internal val ORDER: Comparator<Observance> = compareBy({ it.date }, { it.id })
    }
}

/**
 * Observances computed from the rules of a [FestivalCalendar].
 *
 * Only Kollavarsham years inside the calendar's supported range are evaluated; parts of
 * [observances]' range outside it yield nothing.
 */
public class RuleBasedObservances(
    private val festivals: FestivalCalendar,
) : ObservanceProvider {
    override fun observances(range: ClosedRange<LocalDate>): List<Observance> {
        val supported = festivals.calendar.supportedRange
        val from = maxOf(range.start, supported.start)
        val to = minOf(range.endInclusive, supported.endInclusive)
        if (from > to) return emptyList()
        val years = festivals.calendar.fromGregorian(from).year..festivals.calendar.fromGregorian(to).year
        return years.flatMap { festivals.festivalsIn(it) }.filter { it.date in from..to }.sortedWith(ObservanceProvider.ORDER)
    }
}

/**
 * Observances given as explicit dates: data supplied by the app, never by the calculation
 * engine. Typical uses are a published government holiday list, moon-sighted Islamic festivals,
 * a parish calendar or personal events.
 *
 * ```kotlin
 * val holidays = DatedObservances.fromCsv(
 *     """
 *     date,id,english_name,malayalam_name,category
 *     2026-03-20,eid-ul-fitr,Eid ul-Fitr,ചെറിയ പെരുന്നാൾ,ISLAMIC
 *     """.trimIndent(),
 * )
 * ```
 */
public class DatedObservances(
    entries: List<Observance>,
) : ObservanceProvider {
    private val entries: List<Observance> = entries.sortedWith(ObservanceProvider.ORDER)

    override fun observances(range: ClosedRange<LocalDate>): List<Observance> = entries.filter { it.date in range }

    public companion object {
        private const val HEADER = "date,id,english_name,malayalam_name,category"

        /**
         * Parses CSV with the header `date,id,english_name,malayalam_name,category`. Blank lines
         * and lines starting with `#` are ignored. Names must not contain commas.
         *
         * @throws IllegalArgumentException with the line number if a row is malformed.
         */
        public fun fromCsv(csv: String): DatedObservances {
            val lines = csv.lines().withIndex().filter { (_, l) -> l.isNotBlank() && !l.trimStart().startsWith("#") }
            require(lines.firstOrNull()?.value?.trim() == HEADER) { "CSV must start with the header \"$HEADER\"" }
            val entries =
                lines.drop(1).map { (index, line) ->
                    val cells = line.split(',').map { it.trim() }
                    require(cells.size == 5) { "Line ${index + 1}: expected 5 columns, found ${cells.size}" }
                    try {
                        Observance(
                            date = LocalDate.parse(cells[0]),
                            id = cells[1].also { require(it.isNotEmpty()) { "empty id" } },
                            englishName = cells[2],
                            malayalamName = cells[3],
                            category = ObservanceCategory.valueOf(cells[4]),
                            source = ObservanceSource.DATED_ENTRY,
                        )
                    } catch (e: RuntimeException) {
                        throw IllegalArgumentException("Line ${index + 1}: ${e.message}", e)
                    }
                }
            return DatedObservances(entries)
        }
    }
}
