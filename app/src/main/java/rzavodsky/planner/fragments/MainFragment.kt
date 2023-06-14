package rzavodsky.planner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import rzavodsky.planner.adapters.EditablePlanBlockAdapter
import rzavodsky.planner.R
import rzavodsky.planner.Tasks
import rzavodsky.planner.database.PlanBlockDatabase
import rzavodsky.planner.databinding.FragmentMainBinding
import java.time.LocalDate

/**
 * Main fragment of the application. Shows a list of plans for today in an EditableDayView.
 */
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater)

        val dataSource = PlanBlockDatabase.getInstance(requireContext()).planBlockDao

        val adapter = EditablePlanBlockAdapter(requireContext())
        adapter.onBlockUpdate = {
            lifecycleScope.launch {
                dataSource.update(it)
            }
        }

        adapter.onBlockClick = { block ->
            val action = MainFragmentDirections.actionMainFragmentToPlanDetailFragment(block.id)
            findNavController().navigate(action)
        }

        // Automatically update PlanBlockAdapter using db data
        dataSource.getAllPlansForDay(LocalDate.now()).observe(viewLifecycleOwner) {
            adapter.data = it ?: listOf()
        }

        Tasks.getInstance().tasks.observe(viewLifecycleOwner) {
            adapter.notifyDatasetChanged()
        }
        binding.dayView.editableAdapter = adapter
        binding.dayView.adapter = adapter

        binding.planAddButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_planAddFragment)
        }
        return binding.root
    }

}