package rzavodsky.planner.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate

@Dao
interface PlanBlockDao {
    @Insert
    suspend fun insert(block: PlanBlock)

    @Update
    suspend fun update(block: PlanBlock)

    @Query("SELECT * FROM plan_blocks WHERE date = :date")
    fun getAllPlansForDay(date: LocalDate): LiveData<List<PlanBlock>>
}