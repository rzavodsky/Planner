package rzavodsky.planner.database

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import rzavodsky.planner.Tasks
import rzavodsky.planner.orgmode.OrgTask
import java.time.LocalDate
import kotlin.random.Random

@Entity(tableName = "plan_blocks")
data class PlanBlock(
    var hour: Int,
    var duration: Int,
    val date: LocalDate,
    @ColumnInfo(name = "is_task_plan") val isTaskPlan: Boolean,
    @ColumnInfo(name = "task_id") val taskId: String?,
    val title: String?,
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0
    fun getBackgroundColor(): Int {
        var seed = 0L
        val text = if (isTaskPlan) taskId!! else title!!
        for (c in text) {
            seed = seed * 31L + c.code
        }
        val rand = Random(seed)
        return Color.HSVToColor(floatArrayOf(rand.nextFloat() * 360, 0.6f, 1f))
    }

    val displayName: String?
        get() = if (isTaskPlan) {
            Tasks.getInstance().getTask(taskId!!)?.title
        } else title!!

    val orgTask: OrgTask?
        get() = if (isTaskPlan) Tasks.getInstance().getTask(taskId!!) else null
}
