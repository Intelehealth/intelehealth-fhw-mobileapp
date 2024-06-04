package org.intelehealth.app.shared

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.config.presenter.feature.data.FeatureActiveStatusRepository
import org.intelehealth.config.presenter.feature.factory.FeatureActiveStatusViewModelFactory
import org.intelehealth.config.presenter.feature.viewmodel.FeatureActiveStatusViewModel
import org.intelehealth.config.room.ConfigDatabase
import org.intelehealth.config.room.entity.FeatureActiveStatus

/**
 * Created by Vaghela Mithun R. on 31-05-2024 - 12:16.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class BaseFragment : Fragment() {
    private val featureStatusViewModel by viewModels<FeatureActiveStatusViewModel> {
        val db = ConfigDatabase.getInstance(requireActivity().applicationContext)
        val repository = FeatureActiveStatusRepository(db.featureActiveStatusDao())
        FeatureActiveStatusViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        featureStatusViewModel.fetchFeaturesActiveStatus().observe(viewLifecycleOwner) {
            it ?: return@observe
            onFeatureStatusLoaded(it)
        }
    }

    open fun onFeatureStatusLoaded(status: FeatureActiveStatus) {
        Timber.d { "Feature status =>${Gson().toJson(status)}" }
    }
}