package rzavodsky.planner.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate

/**
 * Interface used to communicate with the database
 */
@Dao
interface PlanBlockDao {
    /**
     * Inserts a block to the database
     * @param block Block to insert
     */
    @Insert
    suspend fun insert(block: PlanBlock)

    /**
     * Updates a block in the database
     * @param block Block to update
     */
    @Update
    suspend fun update(block: PlanBlock)

    /**
     * Deletes a block in the database
     * @param block Block to delete
     */
    @Delete
    suspend fun delete(block: PlanBlock)

    /**
     * Returns a live data of all plans for a given day
     * @param date Date for which plans should be returned
     */
    @Query("SELECT * FROM plan_blocks WHERE date = :date")
    fun getAllPlansForDay(date: LocalDate): LiveData<List<PlanBlock>>

    /**
     * Returns the last block for a day. Returns null if there are no blocks in the day.
     * @param date Date for which block should be returned
     */
    @Query("SELECT * FROM plan_blocks WHERE date = :date ORDER BY hour DESC LIMIT 1")
    suspend fun getLastBlockForDay(date: LocalDate): PlanBlock?

    /**
     * Returns a plan based on its id
     * @param id Id of the plan to return
     */
    @Query("SELECT * FROM plan_blocks WHERE id = :id")
    suspend fun getPlan(id: Long): PlanBlock

    /**
     * Returns either the current or next plan for a day and hour.
     * Returns null if there are no plans left in this day.
     * @param date Day for which block should be returned
     * @param hour Hour at which block should be returned
     */
    @Query("SELECT * FROM plan_blocks WHERE date = :date AND hour + duration > :hour ORDER BY hour ASC LIMIT 1")
    suspend fun getNextPlan(date: LocalDate, hour: Int): PlanBlock?
}