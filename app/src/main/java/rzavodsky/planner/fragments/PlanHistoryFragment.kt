package rzavodsky.planner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import rzavodsky.planner.util.DateFormatter
import rzavodsky.planner.adapters.PlanBlockAdapter
import rzavodsky.planner.database.PlanBlock
import rzavodsky.planner.database.PlanBlockDao
import rzavodsky.planner.database.PlanBlockDatabase
import rzavodsky.planner.databinding.FragmentPlanHistoryBinding
import java.time.LocalDate

const val STATE_DATE = "date"

/**
 * Fragment, which shows the history of all plans
 */
class PlanHistoryFragment : Fragment() {
    private lateinit var binding: FragmentPlanHistoryBinding
    private var date = LocalDate.now().minusDays(1)
    private lateinit var adapter: PlanBlockAdapter
    private lateinit var db: PlanBlockDao
    private var data: LiveData<List<PlanBlock>>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        savedInstanceState?.getLong(STATE_DATE)?.let {
            date = LocalDate.ofEpochDay(it)
        }
        binding = FragmentPlanHistoryBinding.inflate(inflater)

        binding.backwardButton.setOnClickListener {
            date = date.minusDays(1)
            onDateChange()
        }

        binding.forwardButton.setOnClickListener {
            date = date.plusDays(1)
            onDateChange()
        }

        adapter = PlanBlockAdapter()
        adapter.onBlockClick = {
            val action = PlanHistoryFragmentDirections
                .actionPlanHistoryFragmentToPlanDetailFragment(it.id)
            findNavController().navigate(action)
        }
        binding.dayView.adapter = adapter

        db = PlanBlockDatabase.getInstance(requireContext()).planBlockDao

        onDateChange()
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(STATE_DATE, date.toEpochDay())
        super.onSaveInstanceState(outState)
    }

    private fun onDateChange() {
        if (date >= LocalDate.now()) {
            date = LocalDate.now().minusDays(1)
        }
        binding.date.text = DateFormatter.formatDate(date)
        data?.removeObservers(viewLifecycleOwner)
        data = db.getAllPlansForDay(date)
        data?.observe(viewLifecycleOwner) {
            adapter.data = it
        }
    }
}