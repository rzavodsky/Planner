package rzavodsky.planner.database

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import rzavodsky.planner.R
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

    @SuppressLint("DiscouragedApi")
    fun getBackgroundColor(context: Context): Int {
        val typedValue = TypedValue()
        if (!isTaskPlan) {
            context.theme.resolveAttribute(R.attr.plan_temp, typedValue, true)
            return typedValue.data
        }
        var seed = 0L
        val text = taskId!!
        for (c in text) {
            seed = seed * 31L + c.code
        }
        val rand = Random(seed)
        val value = rand.nextInt(10)

        val ident = context.resources.getIdentifier("plan$value", "attr", context.packageName)
        context.theme.resolveAttribute(ident, typedValue, true)
        return typedValue.data
    }

    val displayName: String?
        get() = if (isTaskPlan) {
            Tasks.getInstance().getTask(taskId!!)?.title
        } else title!!

    val orgTask: OrgTask?
        get() = if (isTaskPlan) Tasks.getInstance().getTask(taskId!!) else null
}
