package rzavodsky.planner.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import rzavodsky.planner.TaskModel
import rzavodsky.planner.databinding.TaskItemBinding

class TaskViewHolder(val binding: TaskItemBinding): RecyclerView.ViewHolder(binding.root)

class TaskAdapter(private val lifecycleOwner: LifecycleOwner): RecyclerView.Adapter<TaskViewHolder>() {
    var data = listOf<TaskModel>()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value.sortedBy { it.orgTask?.getInternalPriority() ?: Long.MAX_VALUE }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context))
        binding.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.binding.viewModel = data[position]
        holder.binding.lifecycleOwner = lifecycleOwner
    }
}