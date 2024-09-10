package org.intelehealth.abdm.features.ui.registration

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.abdm.databinding.ActivityAbhaRegistrationConsentBinding
import org.intelehealth.abdm.domain.model.RegistrationConsent
import org.intelehealth.abdm.features.base.BaseActivity
import org.intelehealth.abdm.features.ui.registration.adapter.RegistrationConsentAdapter
import org.intelehealth.abdm.features.viewmodel.registration.AbhaRegistrationConsentViewModel

@AndroidEntryPoint
class AbhaRegistrationConsentActivity :
    BaseActivity<ActivityAbhaRegistrationConsentBinding, AbhaRegistrationConsentViewModel>(),
    RegistrationConsentAdapter.OnCheckboxChecked {
    private lateinit var registrationConsentAdapter: RegistrationConsentAdapter
    private lateinit var consentList: ArrayList<RegistrationConsent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialization()
        setClickListener()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initialization() {
        consentList = ArrayList()
        setConsentAdapter()
        viewModel.consentLiveData.observe(this) {
            consentList.addAll(it)
            registrationConsentAdapter.notifyDataSetChanged()
        }
    }

    override fun setClickListener() {
        binding.btnDecline.btnDecline.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnAccept.btnActive.setOnClickListener {
            startActivity(Intent(this, AbdmRegistrationActivity::class.java))
        }
    }

    private fun setConsentAdapter() {
        registrationConsentAdapter = RegistrationConsentAdapter(consentList, this)
        binding.rvAbhaConsent.layoutManager = LinearLayoutManager(this)
        binding.rvAbhaConsent.adapter = registrationConsentAdapter
    }

    override fun initViewModel() =
        ViewModelProvider(this)[AbhaRegistrationConsentViewModel::class.java]

    override fun initBinding() = ActivityAbhaRegistrationConsentBinding.inflate(layoutInflater)
    override fun onOptionChecked(model: RegistrationConsent) {
        TODO("Not yet implemented")
    }
}
