package rzavodsky.planner

import rzavodsky.planner.database.PlanBlock
import rzavodsky.planner.views.EditableDayView

class EditablePlanBlockAdapter: PlanBlockAdapter(), EditableDayView.Adapter {
    var onBlockUpdate: ((PlanBlock) -> Unit)? = null

    override fun getColor(pos: Int): Int = data[pos].getBackgroundColor()

    override fun changeHourAt(pos: Int, hour: Int) {
        val block = data[pos]
        block.hour = hour
        onBlockUpdate?.invoke(block)
    }

    override fun changeDurationAt(pos: Int, duration: Int) {
        val block = data[pos]
        block.duration = duration
        onBlockUpdate?.invoke(block)
    }
}