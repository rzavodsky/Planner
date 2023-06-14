package rzavodsky.planner.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import rzavodsky.planner.database.PlanBlock
import rzavodsky.planner.databinding.PlanBlockBinding
import rzavodsky.planner.views.DayView

/**
 * Holder for the plan_block view, along with its DataBinding
 */
class PlanBlockViewHolder(val binding: PlanBlockBinding): DayView.ViewHolder(binding.root)

/**
 * Adapter, which provides a list of PlanBlocks to a DayView
 */
open class PlanBlockAdapter: DayView.Adapter<PlanBlockViewHolder>() {
    /**
     * Function, which is called when a block is clicked
     * The parameter is the block, which was updated
     */
    var onBlockClick: ((PlanBlock) -> Unit)? = null

    /**
     * A list of blocks, which should be displayed in a day view
     */
    var data = listOf<PlanBlock>()
        set(value) {
            field = value
            notifyDatasetChanged()
        }
    override fun getItemCount(): Int  {
        return data.size
    }

    override fun getHourAt(pos: Int): Int = data[pos].hour

    override fun getDurationAt(pos: Int): Int = data[pos].duration

    override fun bindViewHolder(pos: Int, view: PlanBlockViewHolder) {
        view.binding.planBlock = data[pos]
    }

    override fun createViewHolder(parent: ViewGroup): PlanBlockViewHolder {
        val binding = PlanBlockBinding.inflate(LayoutInflater.from(parent.context))
        return PlanBlockViewHolder(binding)
    }

    override fun onClick(pos: Int) {
        onBlockClick?.invoke(data[pos])
    }
}