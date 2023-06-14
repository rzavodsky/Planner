package rzavodsky.planner.adapters

import android.content.Context
import rzavodsky.planner.database.PlanBlock
import rzavodsky.planner.views.EditableDayView

/**
 * Adapter, which provides a list of PlanBlocks to an EditableDayView
 */
class EditablePlanBlockAdapter(val context: Context): PlanBlockAdapter(), EditableDayView.Adapter {
    /**
     * Function, which is called when a block is updated by the view
     * This should be used to save the state of the block
     * The parameter is the block, which was updated
     */
    var onBlockUpdate: ((PlanBlock) -> Unit)? = null

    override fun getColor(pos: Int): Int = data[pos].getBackgroundColor(context)

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