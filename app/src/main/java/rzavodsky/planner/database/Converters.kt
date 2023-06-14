package rzavodsky.planner.database

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Converters, which are used to store LocalDate values in a database
 */
class Converters {
    /**
     * Converts a LocalDate object to an integer for storage in a database
     * @param date Date, which will be converted to an integer
     */
    @TypeConverter
    fun convertLocalDateToLong(date: LocalDate): Long = date.toEpochDay()

    /**
     * Converts an integer to a LocalDate object for retrieval from a database
     * @param epochDay Integer to convert, number of days since the Unix Epoch
     */
    @TypeConverter
    fun convertLongToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
}