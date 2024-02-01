package org.intelehealth.ezazi.ui.elcg.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.ajalt.timberkt.Timber
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.FragmentElcgDataBinding
import org.intelehealth.ezazi.ui.elcg.activity.WhoElcgActivity
import org.intelehealth.ezazi.ui.elcg.adapter.Adapter
import org.intelehealth.ezazi.ui.elcg.data.ELCGDataSource
import org.intelehealth.ezazi.ui.elcg.data.ELCGRepository
import org.intelehealth.ezazi.ui.elcg.model.ELCGGraph
import org.intelehealth.ezazi.ui.elcg.viewmodel.ELCGViewModel

/**
 * Created by Vaghela Mithun R. on 23-11-2023 - 20:12.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 */
class ELCGDataFragment private constructor() : Fragment(R.layout.fragment_elcg_data) {
    private var binding: FragmentElcgDataBinding? = null
    private lateinit var elcgGraph: ELCGGraph
    private val viewModel: ELCGViewModel by lazy {
        ELCGViewModel(ELCGRepository(ELCGDataSource()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentElcgDataBinding.bind(view)
        binding!!.rvELCGData.layoutManager = LinearLayoutManager(context)
        loadELCGData()
        observeGraphData()
    }

    private fun loadELCGData() {
        if (requireArguments().containsKey(GRAPH)) {
            val graph: ELCGGraph = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getSerializable(GRAPH, ELCGGraph::class.java)
            } else {
                requireArguments().getSerializable(GRAPH)
            } as ELCGGraph

            if (activity is WhoElcgActivity) {
                val whoElcgActivity: WhoElcgActivity = activity as WhoElcgActivity
                whoElcgActivity.encounters?.let { viewModel.buildELCGGraph(it, graph) }
            }
        }
    }

    private fun observeGraphData() {
        viewModel.elcgGraphLiveData.observe(viewLifecycleOwner) { data ->
            Timber.d { "graph size ${data.size}" }
            binding!!.rvELCGData.adapter = Adapter(requireContext(), data)
        }
    }

    companion object {
        const val GRAPH = "graph"
        fun newInstance(elcgGraph: ELCGGraph): ELCGDataFragment {
            return ELCGDataFragment().apply {
                Bundle().apply {
                    putSerializable(GRAPH, elcgGraph)
                    arguments = this
                }
            }
        }
    }
}