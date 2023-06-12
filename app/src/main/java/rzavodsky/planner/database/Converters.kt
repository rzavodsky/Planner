package rzavodsky.planner.database

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun convertLocalDateToLong(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun convertLongToLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
}