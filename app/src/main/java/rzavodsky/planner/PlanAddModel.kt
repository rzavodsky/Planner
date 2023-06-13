package rzavodsky.planner

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
}
