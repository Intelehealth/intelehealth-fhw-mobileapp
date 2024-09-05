package org.intelehealth.abdm.features

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.abdm.features.base.BaseActivity
import org.intelehealth.abdm.features.databinding.ActivityAbdmMainBinding
import org.intelehealth.abdm.features.viewmodel.AbdMainViewModel

@AndroidEntryPoint
class AbdmMainActivity : BaseActivity<ActivityAbdmMainBinding, AbdMainViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialization()
        setClickListener()
    }

    private fun initialization() {
        binding.toolbar.tvToolbarText.text = getText(R.string.privacy_policy)
    }

    override fun setClickListener() {
        binding.toolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnDecline.btnDecline.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnAccept.root.setOnClickListener {

        }
    }

    override fun initViewModel() = ViewModelProvider(this)[AbdMainViewModel::class.java]

    override fun initBinding() = ActivityAbdmMainBinding.inflate(layoutInflater)
}