package rzavodsky.planner.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import rzavodsky.planner.util.DateFormatter
import rzavodsky.planner.R
import rzavodsky.planner.orgmode.OrgTask

/**
 * ViewModel, which stores information for the PlanAddFragment
 */
class PlanAddModel: ViewModel() {
    var selectedTasks = mutableListOf<TaskModel>()
}

/**
 * ViewModel, which stores information for a task_item
 */
class TaskModel(val isTempTask: Boolean, index: Int?): ViewModel() {
    val index = MutableLiveData<Int>(index)
    var orgTask: OrgTask? = null
    var name: String? = null
    var onClick: (() -> Unit)? = null

    /**
     * Returns a summary, which is displayed under the name of the task
     * Summary shows information about the sorting criterion of this task
     */
    fun getSummary(context: Context): String {
        if (isTempTask) return ""

        if (orgTask!!.priority != null) return context.getString(R.string.priority, orgTask!!.priority)
        if (orgTask!!.deadline != null) return context.getString(
            R.string.deadline,
            DateFormatter.formatInstant(orgTask!!.deadline)
        )
        if (orgTask!!.scheduled != null) return context.getString(
            R.string.scheduled,
            DateFormatter.formatInstant(orgTask!!.scheduled)
        )
        return ""
    }
}
