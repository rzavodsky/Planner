package rzavodsky.planner.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
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

    @Delete
    suspend fun delete(block: PlanBlock)

    @Query("SELECT * FROM plan_blocks WHERE date = :date")
    fun getAllPlansForDay(date: LocalDate): LiveData<List<PlanBlock>>

    @Query("SELECT * FROM plan_blocks WHERE date = :date ORDER BY hour DESC LIMIT 1")
    suspend fun getLastBlockForDay(date: LocalDate): PlanBlock?

    @Query("SELECT * FROM plan_blocks WHERE id = :id")
    suspend fun getPlan(id: Long): PlanBlock
}