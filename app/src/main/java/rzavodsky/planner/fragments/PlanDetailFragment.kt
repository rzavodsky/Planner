package rzavodsky.planner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import rzavodsky.planner.R
import rzavodsky.planner.database.PlanBlock
import rzavodsky.planner.database.PlanBlockDao
import rzavodsky.planner.database.PlanBlockDatabase
import rzavodsky.planner.databinding.FragmentPlanDetailBinding
import java.time.LocalDate

class PlanDetailFragment : Fragment(), MenuProvider {
    private val args: PlanDetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentPlanDetailBinding
    private lateinit var dataSource: PlanBlockDao
    private lateinit var planBlock: PlanBlock

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlanDetailBinding.inflate(inflater)
        dataSource = PlanBlockDatabase.getInstance(requireContext()).planBlockDao


        lifecycleScope.launch {
            planBlock = dataSource.getPlan(args.planId)
            binding.planBlock = planBlock
            requireActivity().addMenuProvider(this@PlanDetailFragment, viewLifecycleOwner)
        }

        return binding.root
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (planBlock.date == LocalDate.now()) {
            menuInflater.inflate(R.menu.plan_detail_menu, menu)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_delete -> {
            lifecycleScope.launch {
                dataSource.delete(binding.planBlock!!)
                findNavController().navigateUp()
            }
            true
        }
        else -> false
    }
}