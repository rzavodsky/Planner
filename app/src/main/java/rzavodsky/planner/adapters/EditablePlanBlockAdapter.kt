package rzavodsky.planner.adapters

import android.content.Context
import rzavodsky.planner.database.PlanBlock
import rzavodsky.planner.views.EditableDayView

class EditablePlanBlockAdapter: PlanBlockAdapter(), EditableDayView.Adapter {
    var onBlockUpdate: ((PlanBlock) -> Unit)? = null

    override fun getColor(pos: Int, context: Context): Int = data[pos].getBackgroundColor(context)

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