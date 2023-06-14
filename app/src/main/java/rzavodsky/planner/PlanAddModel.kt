package rzavodsky.planner

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import rzavodsky.planner.orgmode.OrgTask

class PlanAddModel: ViewModel() {
    var selectedTasks = mutableListOf<TaskModel>()
}

class TaskModel(val isTempTask: Boolean, index: Int?): ViewModel() {
    val index = MutableLiveData<Int>(index)
    var orgTask: OrgTask? = null
    var name: String? = null
    var onClick: (() -> Unit)? = null

    fun getSummary(context: Context): String {
        if (isTempTask) return ""

        if (orgTask!!.priority != null) return context.getString(R.string.priority, orgTask!!.priority)
        if (orgTask!!.deadline != null) return context.getString(R.string.deadline, DateFormatter.formatInstant(orgTask!!.deadline))
        if (orgTask!!.scheduled != null) return context.getString(R.string.scheduled, DateFormatter.formatInstant(orgTask!!.scheduled))
        return ""
    }
}
