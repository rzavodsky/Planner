package rzavodsky.planner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import rzavodsky.planner.database.PlanBlockDatabase
import rzavodsky.planner.databinding.FragmentPlanDetailBinding

class PlanDetailFragment : Fragment() {
    val args: PlanDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPlanDetailBinding.inflate(inflater)

        val dataSource = PlanBlockDatabase.getInstance(requireContext()).planBlockDao

        lifecycleScope.launch {
            val plan = dataSource.getPlan(args.planId)
            binding.planBlock = plan
        }

        return binding.root
    }
}