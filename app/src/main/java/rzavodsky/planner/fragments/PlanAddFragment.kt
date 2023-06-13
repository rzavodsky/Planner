package rzavodsky.planner.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import rzavodsky.planner.PlanAddModel
import rzavodsky.planner.Preferences
import rzavodsky.planner.adapters.TaskAdapter
import rzavodsky.planner.TaskModel
import rzavodsky.planner.Tasks
import rzavodsky.planner.database.PlanBlock
import rzavodsky.planner.database.PlanBlockDatabase
import rzavodsky.planner.databinding.FragmentPlanAddBinding
import rzavodsky.planner.databinding.TaskItemBinding
import java.time.LocalDate

class PlanAddFragment : Fragment() {
    private lateinit var binding: FragmentPlanAddBinding
    private lateinit var viewModel: PlanAddModel
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlanAddBinding.inflate(inflater)
        viewModel = ViewModelProvider(requireActivity())[PlanAddModel::class.java]

        if (savedInstanceState == null) {
            viewModel.selectedTasks = mutableListOf()
        }

        for (task in viewModel.selectedTasks) {
            if (task.isTempTask) {
                addTempTask(task)
            }
        }

        binding.confirmButton.setOnClickListener { confirm() }

        binding.tempTaskAddButton.setOnClickListener {
            val taskName = binding.tempTaskInput.text.toString()
            if (taskName == "") return@setOnClickListener
            binding.tempTaskInput.text.clear()

            val task = TaskModel(true, viewModel.selectedTasks.size + 1)
            task.name = taskName
            viewModel.selectedTasks.add(task)
            addTempTask(task)
        }

        adapter = TaskAdapter(viewLifecycleOwner)

        Tasks.getInstance().tasks.observe(viewLifecycleOwner) {tasks ->
            adapter.data = tasks.values.toList().map { task ->
                val model = TaskModel(false, null)
                model.orgTask = task
                model.onClick = {
                    if (model.index.value == null) {
                        viewModel.selectedTasks.add(model)
                        model.index.value = viewModel.selectedTasks.size
                    } else {
                        removeTask(model, null)
                    }
                }
                model
            }
            recreateSelectedTasks()
        }

        binding.orgTasksView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        binding.orgTasksView.adapter = adapter

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun recreateSelectedTasks() {
        val newSelectedTasks = mutableListOf<TaskModel>()
        for (selectedTask in viewModel.selectedTasks) {
            if (selectedTask.isTempTask) {
                newSelectedTasks.add(selectedTask)
            } else {
                for (task in adapter.data) {
                    if (selectedTask.orgTask!!.id == task.orgTask!!.id) {
                        task.index.value = selectedTask.index.value
                        newSelectedTasks.add(task)
                        break
                    }
                }
            }
        }
        adapter.notifyDataSetChanged()
        viewModel.selectedTasks = newSelectedTasks
    }

    private fun confirm() {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val duration = pref.getInt(Preferences.defaultDuration, 1)
        val startHour = pref.getInt(Preferences.startHour, 0)
        lifecycleScope.launch {
            val dao = PlanBlockDatabase.getInstance(requireContext()).planBlockDao
            for (task in viewModel.selectedTasks) {
                val nextStart = dao.getLastBlockForDay(LocalDate.now())?.let { it.hour + it.duration } ?: startHour
                dao.insert(PlanBlock(nextStart, duration, LocalDate.now(),
                    !task.isTempTask, task.orgTask?.id, task.name))
            }
            findNavController().navigateUp()
        }
    }

    private fun removeTask(task: TaskModel, taskView: View?) {
        viewModel.selectedTasks.remove(task)

        if (task.isTempTask) {
            binding.tempTaskLayout.removeView(taskView!!)
        }

        // Fix count
        for (t in viewModel.selectedTasks) {
            if (t.index.value!! > task.index.value!!) {
                t.index.value = t.index.value!! - 1
            }
        }
        task.index.value = null
    }

    private fun addTempTask(task: TaskModel) {
        val taskBinding = TaskItemBinding.inflate(layoutInflater)
        taskBinding.viewModel = task
        taskBinding.lifecycleOwner = viewLifecycleOwner
        binding.tempTaskLayout.addView(taskBinding.root)
        task.onClick = {
            removeTask(task, taskBinding.root)
        }
    }
}