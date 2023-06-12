package rzavodsky.planner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import rzavodsky.planner.PlanBlockAdapter
import rzavodsky.planner.database.PlanBlockDatabase
import rzavodsky.planner.databinding.FragmentMainBinding
import java.time.LocalDate

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater)

        val dataSource = PlanBlockDatabase.getInstance(requireContext()).planBlockDao

        val adapter = PlanBlockAdapter()
        adapter.onBlockUpdate = {
            lifecycleScope.launch {
                dataSource.update(it)
            }
        }

        // Automatically update PlanBlockAdapter using db data
        dataSource.getAllPlansForDay(LocalDate.now())
            .observe(viewLifecycleOwner) {
            adapter.data = it ?: listOf()
        }
        binding.dayView.editableAdapter = adapter

        return binding.root
    }

}