package org.intelehealth.abdm.features.ui

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.utils.PreferenceUtils
import org.intelehealth.abdm.common.utils.ProgressBarUtils
import org.intelehealth.abdm.common.utils.ToastUtil
import org.intelehealth.abdm.databinding.ActivityAbdmMainBinding
import org.intelehealth.abdm.features.base.BaseActivity
import org.intelehealth.abdm.features.ui.registration.dialog.RegistrationConfirmationDialog
import org.intelehealth.abdm.features.viewmodel.AbdMainViewModel
import org.intelehealth.abdm.features.viewstate.GetAuthTokenViewState
import org.intelehealth.abdm.features.viewstate.SendAadhaarOtpViewState

@AndroidEntryPoint
class AbdmMainActivity : BaseActivity<ActivityAbdmMainBinding, AbdMainViewModel>() {

    private lateinit var progressBarUtils: ProgressBarUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialization()
        setClickListener()
    }

    private fun initialization() {
        binding.toolbar.tvToolbarText.text = getText(R.string.privacy_policy)
        progressBarUtils = ProgressBarUtils(this)
        handleAuthTokenState()
    }

    override fun setClickListener() {
        binding.toolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnDecline.btnDecline.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAccept.btnActive.setOnClickListener {
            val confirmationDialog = RegistrationConfirmationDialog()
            confirmationDialog.show(
                supportFragmentManager, RegistrationConfirmationDialog::class.simpleName
            )
        }
    }

    override fun initViewModel() = ViewModelProvider(this)[AbdMainViewModel::class.java]

    override fun initBinding() = ActivityAbdmMainBinding.inflate(layoutInflater)

    private fun handleAuthTokenState() {
        viewModel.authTokenState.observe(this) {
            when (it) {
                is GetAuthTokenViewState.Idle -> {}

                is GetAuthTokenViewState.Loading -> progressBarUtils.showCircularProgressbar()

                is GetAuthTokenViewState.Success -> {
                    progressBarUtils.dismissProgressBar()
                    it.data.accessToken?.let { accessToken ->
                        PreferenceUtils.saveAuthToken(
                            this,
                            accessToken
                        )
                    }
                }

                is GetAuthTokenViewState.Error -> {
                    progressBarUtils.dismissProgressBar()
                    ToastUtil.showShortToast(this, it.message)
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

}