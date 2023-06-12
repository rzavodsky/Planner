package rzavodsky.planner.database

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import kotlin.random.Random

@Entity(tableName = "plan_blocks")
data class PlanBlock(
    @PrimaryKey(autoGenerate = true) var id: Long,
    var hour: Int,
    var duration: Int,
    val date: LocalDate,
    @ColumnInfo(name = "is_task_plan") val isTaskPlan: Boolean,
    @ColumnInfo(name = "task_id") val taskId: String?,
    val title: String?,
) {
    fun getBackgroundColor(): Int {
        var seed = 0L
        val text = if (isTaskPlan) taskId!! else title!!
        for (c in text) {
            seed = seed * 31L + c.code
        }
        val rand = Random(seed)
        return Color.HSVToColor(floatArrayOf(rand.nextFloat() * 360, 0.6f, 1f))
    }
}
