package io.github.anjalymsajeev.kollavarsham.core.boundary

import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth
import io.github.anjalymsajeev.kollavarsham.core.MonthKey
import java.time.LocalDate

/**
 * Loads the bundled, evidence-backed almanac corrections (ADR-0005).
 *
 * Format (UTF-8 CSV, `#` comments allowed):
 * `me_year,month,first_day,source_citation`
 * where `month` is a [MalayalamMonth] name and `first_day` an ISO date.
 */
internal object VerifiedOverrides {
    // Absolute path: a path relative to this class's package would break when R8/ProGuard
    // renames or repackages the class in a minified Android app.
    private const val RESOURCE = "/io/github/anjalymsajeev/kollavarsham/core/boundary/verified-overrides.csv"

    val bundled: Map<MonthKey, LocalDate> by lazy {
        val stream =
            VerifiedOverrides::class.java.getResourceAsStream(RESOURCE)
                ?: error("Missing bundled resource $RESOURCE")
        stream.bufferedReader(Charsets.UTF_8).use { parse(it.readText()) }
    }

    fun parse(csv: String): Map<MonthKey, LocalDate> {
        val rows =
            csv
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .toList()
        require(rows.firstOrNull() == "me_year,month,first_day,source_citation") {
            "Unexpected header in $RESOURCE: ${rows.firstOrNull()}"
        }
        return rows.drop(1).associate { row ->
            val cells = row.split(',', limit = 4)
            require(cells.size == 4 && cells[3].isNotBlank()) { "Override row needs 4 columns and a citation: $row" }
            MonthKey(cells[0].toInt(), MalayalamMonth.valueOf(cells[1])) to LocalDate.parse(cells[2])
        }
    }
}
