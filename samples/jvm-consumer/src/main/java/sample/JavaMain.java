package sample;

import io.github.anjalymsajeev.kollavarsham.core.KollavarshamCalendar;
import io.github.anjalymsajeev.kollavarsham.core.MalayalamDate;
import io.github.anjalymsajeev.kollavarsham.core.MalayalamMonth;
import java.time.LocalDate;

/** Gregorian ↔ Malayalam conversion from plain Java, using only the public API. */
public final class JavaMain {
    public static void main(String[] args) {
        // Kotlin companion members are reached through `Companion` from Java.
        KollavarshamCalendar calendar = KollavarshamCalendar.Companion.create();

        MalayalamDate ml = calendar.fromGregorian(LocalDate.of(2026, 11, 14));
        System.out.println("2026-11-14 = ME " + ml.getYear() + " " + ml.getMonth() + " " + ml.getDay()); // ME 1202 Thulam 28

        LocalDate greg = calendar.toGregorian(new MalayalamDate(1202, MalayalamMonth.Vrischikam, 1));
        System.out.println("Vrischikam 1, 1202 = " + greg); // 2026-11-17

        // Thulam 1202 has 30 days: toGregorianOrNull returns null instead of throwing.
        System.out.println("Thulam 31, 1202 = " + calendar.toGregorianOrNull(new MalayalamDate(1202, MalayalamMonth.Thulam, 31)));
    }
}
