package rzavodsky.planner.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Object, which formats dates into a human readable format
 */
object DateFormatter {
    /**
     * Formats a LocalDate into a human readable format
     */
    @JvmStatic
    fun formatDate(date: LocalDate?): String? {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.")
        return date?.format(formatter)
    }

    /**
     * Formats an Instant into a human readable format, in the device timezone
     */
    @JvmStatic
    fun formatInstant(instant: Instant?): String? {
        val formatter = DateTimeFormatter.ofPattern("dd.MM. HH:mm")
        return instant?.atZone(ZoneId.systemDefault())?.format(formatter)
    }
}