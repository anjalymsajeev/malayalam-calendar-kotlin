package io.github.anjalymsajeev.kollavarsham.android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.anjalymsajeev.kollavarsham.core.MalayalamWeekday
import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import io.github.anjalymsajeev.kollavarsham.locale.displayName
import io.github.anjalymsajeev.kollavarsham.locale.format
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * A month calendar showing each Gregorian day with its Malayalam date beneath it.
 *
 * This Composable holds no calendar logic: pass a [MonthGrid] built from the core SDK.
 *
 * ```kotlin
 * var selected by remember { mutableStateOf(LocalDate.now()) }
 * KollavarshamMonthView(
 *     grid = MonthGrid.ofGregorianMonth(YearMonth.from(selected)),
 *     selectedDate = selected,
 *     onDateSelected = { selected = it },
 *     markedDates = festivalDates,
 * )
 * ```
 *
 * @param grid the cells to show.
 * @param selectedDate the highlighted day, if any.
 * @param onDateSelected called when the user taps a day.
 * @param locale language of weekday names.
 * @param markedDates days to mark with a dot, for example festivals.
 * @param today the day to outline; defaults to the device date.
 */
@Composable
public fun KollavarshamMonthView(
    grid: MonthGrid,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    locale: CalendarLocale = CalendarLocale.MALAYALAM,
    markedDates: Set<LocalDate> = emptySet(),
    today: LocalDate = LocalDate.now(),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            for (i in 0L until 7L) {
                val weekday = MalayalamWeekday.of(grid.firstDayOfWeek.plus(i))
                Text(
                    text = weekday.displayName(locale),
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color =
                        if (weekday.dayOfWeek ==
                            DayOfWeek.SUNDAY
                        ) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        }
        for (week in grid.weeks) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (cell in week) {
                    DayCell(
                        cell = cell,
                        selected = cell.date == selectedDate,
                        isToday = cell.date == today,
                        marked = cell.date in markedDates,
                        locale = locale,
                        onClick = { onDateSelected(cell.date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    cell: MonthGrid.Cell,
    selected: Boolean,
    isToday: Boolean,
    marked: Boolean,
    locale: CalendarLocale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)
    val description = "${cell.date}, ${cell.malayalamDate.format(locale)}"
    Box(
        modifier =
            modifier
                .aspectRatio(0.85f)
                .padding(vertical = 2.dp)
                .background(if (selected) colors.primaryContainer else colors.surface, shape)
                .then(if (isToday) Modifier.border(1.dp, colors.primary, shape) else Modifier)
                .clickable(onClick = onClick)
                .semantics {
                    contentDescription = description
                    this.selected = selected
                },
        contentAlignment = Alignment.Center,
    ) {
        val alpha = if (cell.inMonth) 1f else 0.38f
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = cell.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = (if (cell.date.dayOfWeek == DayOfWeek.SUNDAY) colors.error else colors.onSurface).copy(alpha = alpha),
            )
            Text(
                text = cell.malayalamDate.day.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = alpha),
            )
            Box(Modifier.size(4.dp).background(if (marked) colors.tertiary else colors.surface.copy(alpha = 0f), CircleShape))
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun KollavarshamMonthViewPreview() {
    MaterialTheme {
        KollavarshamMonthView(
            grid = MonthGrid.ofGregorianMonth(YearMonth.of(2026, 11)),
            selectedDate = LocalDate.of(2026, 11, 14),
            onDateSelected = {},
            markedDates = setOf(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 17)),
            today = LocalDate.of(2026, 11, 14),
        )
    }
}
