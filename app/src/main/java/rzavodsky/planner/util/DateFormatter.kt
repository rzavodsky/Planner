package rzavodsky.planner.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateFormatter {
    @JvmStatic
    fun formatDate(date: LocalDate?): String? {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.")
        return date?.format(formatter)
    }

    @JvmStatic
    fun formatInstant(instant: Instant?): String? {
        val formatter = DateTimeFormatter.ofPattern("dd.MM. HH:mm")
        return instant?.atZone(ZoneId.systemDefault())?.format(formatter)
    }
}