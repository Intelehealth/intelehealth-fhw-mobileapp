package org.intelehealth.ezazi.ui.visit.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.intelehealth.ezazi.R
import org.intelehealth.ezazi.databinding.FragmentVisitStatusListBinding

/**
 * Created by Vaghela Mithun R. on 16-01-2024 - 00:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class OutcomePendingVisitFragment : Fragment(R.layout.fragment_visit_status_list) {
    private lateinit var binding: FragmentVisitStatusListBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVisitStatusListBinding.bind(view)
    }

    companion object {
        fun newInstance(): OutcomePendingVisitFragment {
            return OutcomePendingVisitFragment()
        }
    }
}