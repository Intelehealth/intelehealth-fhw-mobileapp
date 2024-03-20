package org.intelehealth.ncd.search.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.intelehealth.ncd.databinding.LayoutSearchPatientCategoryBinding

class HypertensionScreeningFragment : Fragment() {

    private var binding: LayoutSearchPatientCategoryBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutSearchPatientCategoryBinding.inflate(layoutInflater)
        return binding!!.root
    }

}
