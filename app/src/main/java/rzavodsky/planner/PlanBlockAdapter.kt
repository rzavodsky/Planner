package rzavodsky.planner

import android.view.LayoutInflater
import android.view.ViewGroup
import rzavodsky.planner.database.PlanBlock
import rzavodsky.planner.databinding.PlanBlockBinding
import rzavodsky.planner.views.DayView
import rzavodsky.planner.views.EditableDayView

class PlanBlockViewHolder(val binding: PlanBlockBinding): DayView.ViewHolder(binding.root)

class PlanBlockAdapter: EditableDayView.Adapter<PlanBlockViewHolder>() {
    var onBlockUpdate: ((PlanBlock) -> Unit)? = null
    var onBlockClick: ((PlanBlock) -> Unit)? = null
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

    override fun getColor(pos: Int): Int = data[pos].getBackgroundColor()
}