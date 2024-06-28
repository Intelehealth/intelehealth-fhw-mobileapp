package org.intelehealth.app.ui.patient.activity

import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityPatientRegistrationBinding
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.ui.patient.adapter.PatientInfoPagerAdapter

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientRegistrationActivity : BaseActivity() {
    private lateinit var binding: ActivityPatientRegistrationBinding
    private lateinit var pagerAdapter: PatientInfoPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindPagerAdapter()
        manageTitleVisibilityOnScrolling()
    }

    private fun manageTitleVisibilityOnScrolling() {
        binding.appBarLayoutPatient.addOnOffsetChangedListener(object : OnOffsetChangedListener {
            var scrollRange = -1;
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout?.totalScrollRange ?: -1
                }

                binding.collapsingToolbar.title = if (scrollRange + verticalOffset == 0) {
                    resources.getString(R.string.add_new_patient)
                } else ""
            }
        })
    }

    private fun bindPagerAdapter() {
        pagerAdapter = PatientInfoPagerAdapter(supportFragmentManager, lifecycle)
        binding.pagerPatientInfo.adapter = pagerAdapter
    }
}