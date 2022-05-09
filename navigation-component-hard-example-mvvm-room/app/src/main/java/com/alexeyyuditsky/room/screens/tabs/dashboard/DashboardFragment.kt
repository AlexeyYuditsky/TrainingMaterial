package com.alexeyyuditsky.room.screens.tabs.dashboard

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alexeyyuditsky.room.R
import com.alexeyyuditsky.room.Repositories
import com.alexeyyuditsky.room.databinding.FragmentDashboardBinding
import com.alexeyyuditsky.room.model.boxes.entities.Box
import com.alexeyyuditsky.room.screens.tabs.dashboard.DashboardViewModel
import com.alexeyyuditsky.room.utils.viewModelCreator
import com.alexeyyuditsky.room.views.DashboardItemView

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val viewModel by viewModelCreator { DashboardViewModel(Repositories.boxesRepository) }

    private lateinit var binding: FragmentDashboardBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)

        clearBoxViews()

        viewModel.boxes.observe(viewLifecycleOwner) { renderBoxes(it) }
    }

    private fun renderBoxes(boxes: List<Box>) {
        clearBoxViews()
        if (boxes.isEmpty()) {
            binding.noBoxesTextView.isVisible = true
            binding.boxesContainer.isVisible = false
        } else {
            binding.noBoxesTextView.isVisible = false
            binding.boxesContainer.isVisible = true
            createBoxes(boxes)
        }
    }

    private fun createBoxes(boxes: List<Box>) {
        // let's create boxes here by using dynamic view generation
        val width = resources.getDimensionPixelSize(R.dimen.dashboard_item_width)
        val height = resources.getDimensionPixelSize(R.dimen.dashboard_item_height)
        val generatedIdentifiers = boxes.map { box ->
            val id = View.generateViewId()
            val dashboardItemView = DashboardItemView(requireContext())
            dashboardItemView.setBox(box)
            dashboardItemView.id = id
            dashboardItemView.tag = box
            dashboardItemView.setOnClickListener(boxClickListener)
            val params = ConstraintLayout.LayoutParams(width, height)
            binding.boxesContainer.addView(dashboardItemView, params)
            return@map id
        }.toIntArray()
        binding.flowView.referencedIds = generatedIdentifiers
    }

    private fun clearBoxViews() {
        binding.boxesContainer.removeViews(1, binding.root.childCount - 1)
    }

    private val boxClickListener = View.OnClickListener {
        val box = it.tag as Box
        val direction = DashboardFragmentDirections.actionDashboardFragmentToBoxFragment(
            box.id,
            box.colorName,
            box.colorValue
        )
        findNavController().navigate(direction)
    }

}