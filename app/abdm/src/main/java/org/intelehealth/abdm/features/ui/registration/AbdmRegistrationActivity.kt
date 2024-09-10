package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.abdm.databinding.ActivityAbdmRegistrationBinding
import org.intelehealth.abdm.features.base.BaseActivity
import org.intelehealth.abdm.features.viewmodel.registration.AbdmRegistrationViewModel

@AndroidEntryPoint
class AbdmRegistrationActivity :
    BaseActivity<ActivityAbdmRegistrationBinding, AbdmRegistrationViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun setClickListener() {

    }

    override fun initViewModel() = ViewModelProvider(this)[AbdmRegistrationViewModel::class.java]

    override fun initBinding() = ActivityAbdmRegistrationBinding.inflate(layoutInflater)
}