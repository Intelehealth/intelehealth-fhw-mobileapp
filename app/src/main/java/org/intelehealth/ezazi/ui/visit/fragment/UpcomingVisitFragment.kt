package org.intelehealth.ezazi.ui.visit.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.core.Result
import org.intelehealth.ezazi.databinding.FragmentCommenListviewBinding
import org.intelehealth.ezazi.databinding.FragmentVisitStatusListBinding
import org.intelehealth.ezazi.models.dto.PatientDTO
import org.intelehealth.ezazi.ui.visit.adapter.VisitStatusAdapter
import org.intelehealth.ezazi.ui.visit.viewmodel.VisitViewModel
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder
import org.intelehealth.klivekit.utils.extensions.setupLinearView
import java.util.LinkedList

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 00:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class UpcomingVisitFragment : Fragment(R.layout.fragment_commen_listview), BaseViewHolder.ViewHolderClickListener {
    private lateinit var binding: FragmentCommenListviewBinding
    private lateinit var adapter: VisitStatusAdapter
    private val viewMode: VisitViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.Factory.from(VisitViewModel.initializer)
        )[VisitViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCommenListviewBinding.bind(view)
        loadUpcomingVisit()
    }

    private fun initListView() {
        adapter = VisitStatusAdapter(requireContext(), LinkedList())
        adapter.viewHolderClickListener = this
        binding.rvPrescription.setupLinearView(adapter)
    }

    private fun loadUpcomingVisit() {
        viewMode.upcomingVisits().observe(viewLifecycleOwner) {
            viewMode.handleResponse(it) { visits ->
                Timber.d { "Upcoming Visit ${Gson().toJson(visits)}" }
                bindDataToUI(it)
            }
        }
    }

    private fun bindDataToUI(result: Result<List<PatientDTO>>) {

    }

    companion object {
        fun newInstance(): UpcomingVisitFragment {
            return UpcomingVisitFragment()
        }
    }

    override fun onViewHolderViewClicked(view: View?, position: Int) {

    }
}