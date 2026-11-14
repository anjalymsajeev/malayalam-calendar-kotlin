package io.github.anjalymsajeev.kollavarsham.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.anjalymsajeev.kollavarsham.android.KollavarshamMonthView
import io.github.anjalymsajeev.kollavarsham.android.MonthGrid
import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar
import io.github.anjalymsajeev.kollavarsham.locale.CalendarLocale
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { CalendarScreen() }
            }
        }
    }
}

@Composable
fun CalendarScreen(today: LocalDate = LocalDate.now(KollavarshamCalendar.INDIAN_STANDARD_TIME)) {
    val presenter = remember { CalendarPresenter() }
    var selected by rememberSaveable { mutableStateOf(today) }
    var month by rememberSaveable { mutableStateOf(YearMonth.from(today)) }
    var locale by rememberSaveable { mutableStateOf(CalendarLocale.MALAYALAM) }

    val grid = remember(month) { MonthGrid.ofGregorianMonth(month) }
    val marked = remember(month) { presenter.festivalDates(month) }
    val details = remember(selected, locale) { presenter.details(selected, locale) }

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { month = month.minusMonths(1) }) { Text("‹") }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), style = MaterialTheme.typography.titleLarge)
                Text(presenter.malayalamMonthsTitle(month, locale), style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { month = month.plusMonths(1) }) { Text("›") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = locale == CalendarLocale.MALAYALAM,
                onClick = { locale = CalendarLocale.MALAYALAM },
                label = { Text("മലയാളം") },
            )
            FilterChip(
                selected = locale == CalendarLocale.ENGLISH,
                onClick = { locale = CalendarLocale.ENGLISH },
                label = { Text("English") },
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                selected = today
                month = YearMonth.from(today)
            }) { Text(if (locale == CalendarLocale.MALAYALAM) "ഇന്ന്" else "Today") }
        }
        KollavarshamMonthView(
            grid = grid,
            selectedDate = selected,
            onDateSelected = { selected = it },
            locale = locale,
            markedDates = marked,
            today = today,
        )
        DayDetailsCard(details, locale)
    }
}

@Composable
private fun DayDetailsCard(
    details: DayDetails,
    locale: CalendarLocale,
) {
    val ml = locale == CalendarLocale.MALAYALAM
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(details.malayalamDate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${details.weekday} · ${details.gregorian}", style = MaterialTheme.typography.bodyMedium)
            Row2(if (ml) "നക്ഷത്രം" else "Nakshatra", details.nakshatra)
            Row2(if (ml) "തിഥി" else "Tithi", details.tithi)
            Row2(if (ml) "സൂര്യോദയം" else "Sunrise", details.sunrise)
            Row2(if (ml) "സൂര്യാസ്തമയം" else "Sunset", details.sunset)
            if (details.festivals.isNotEmpty()) {
                Row2(if (ml) "വിശേഷം" else "Festivals", details.festivals.joinToString(", "))
            }
            if (details.borderlineMonthNote) {
                Text(
                    if (ml) BORDERLINE_NOTE_ML else BORDERLINE_NOTE_EN,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun Row2(
    label: String,
    value: String,
) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(0.4f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, Modifier.weight(0.6f), style = MaterialTheme.typography.bodyMedium)
    }
}

private const val BORDERLINE_NOTE_ML = "ഈ മാസത്തിന്റെ ആരംഭം ചില പഞ്ചാംഗങ്ങളിൽ ഒരു ദിവസം വ്യത്യാസപ്പെടാം."
private const val BORDERLINE_NOTE_EN = "Some almanacs may start this month a day differently."
