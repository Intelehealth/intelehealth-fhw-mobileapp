package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.utils.BundleConstant
import org.intelehealth.abdm.common.utils.ProgressBarUtils
import org.intelehealth.abdm.common.utils.ToastUtil
import org.intelehealth.abdm.common.utils.ValidationUtils.isValidAbhaRegex
import org.intelehealth.abdm.databinding.ActivitySelectAbhaAddressBinding
import org.intelehealth.abdm.features.base.BaseActivity
import org.intelehealth.abdm.features.intent.EnrollAbhaAddressIntent
import org.intelehealth.abdm.features.ui.registration.dialog.AbhaAddressSuggestionDialogFragment
import org.intelehealth.abdm.features.viewmodel.registration.SelectAbhaAddressViewModel
import org.intelehealth.abdm.features.viewstate.AbhaAddressSuggestionViewState
import org.intelehealth.abdm.features.viewstate.EnrollAbhaAddressViewState
import java.util.Objects

@Suppress("DEPRECATION")
@AndroidEntryPoint
class SelectAbhaAddressActivity :
    BaseActivity<ActivitySelectAbhaAddressBinding, SelectAbhaAddressViewModel>() {
    private var transactionId: String? = null
    private lateinit var progressBarUtils: ProgressBarUtils
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialization()
        setClickListener()
        handleAbhaSuggestionList()
        handelOnEnrollAbhaAddress()
    }

    private fun initialization() {
        binding.toolbar.tvToolbarText.text = getString(R.string.select_an_abha_address)
        binding.btnSubmit.btnActive.text = getString(R.string.submit)
        if (intent.hasExtra(BundleConstant.TXN_ID)) {
            transactionId = intent.getStringExtra(BundleConstant.TXN_ID)
        }
        progressBarUtils = ProgressBarUtils(this)
        transactionId?.let { viewModel.sendIntent(EnrollAbhaAddressIntent.GetSuggestionList(it)) }
    }

    override fun setClickListener() {
        binding.toolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSubmit.btnActive.setOnClickListener {
            val chip: Chip = binding.chipGrp.findViewById(binding.chipGrp.checkedChipId)
            val selectedChip = chip.text?.toString() ?: ""
            val abhaAddress = Objects.requireNonNull(binding.etAbhaAddress.text).toString()
            if (TextUtils.isEmpty(selectedChip) && TextUtils.isEmpty(abhaAddress)) {
                ToastUtil.showShortToast(this, getString(R.string.please_select_abha_address))
            } else if (!TextUtils.isEmpty(selectedChip)) {
                transactionId?.let {
                    viewModel.sendIntent(
                        EnrollAbhaAddressIntent.EnrollAbhaAddress(
                            it,
                            selectedChip
                        )
                    )
                }
            } else if (isValidAbhaAddress(abhaAddress)) {
                transactionId?.let {
                    viewModel.sendIntent(
                        EnrollAbhaAddressIntent.EnrollAbhaAddress(
                            it,
                            abhaAddress
                        )
                    )
                }
            }
        }
        binding.ivInfoAbhaSuggestion.setOnClickListener {
            AbhaAddressSuggestionDialogFragment().show(
                supportFragmentManager,
                AbhaAddressSuggestionDialogFragment::class.simpleName
            )
        }
    }

    override fun initViewModel() =
        ViewModelProvider(this)[SelectAbhaAddressViewModel::class.java]

    override fun initBinding() = ActivitySelectAbhaAddressBinding.inflate(layoutInflater)


    private fun createDynamicChips(chipTitle: String) {
        val chip = Chip(this)
        chip.id = ViewCompat.generateViewId()
        chip.text = chipTitle
        chip.isCheckable = true
        chip.setChipBackgroundColorResource(R.color.white)
        chip.setChipStrokeColorResource(R.color.colorPrimaryDark)
        chip.chipStrokeWidth = 2f
        chip.setTextColor(getColor(R.color.colorPrimary))
        chip.isCloseIconVisible
        chip.setCheckedIconTintResource(R.color.colorPrimary)
        binding.chipGrp.addView(chip)
    }


    private fun isValidAbhaAddress(text: String): Boolean {
        if (text.length < 8) {
            Toast.makeText(
                this,
                getString(R.string.abha_address_must_be_at_least_8_characters_long),
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (!isValidAbhaRegex(text)) {
            Toast.makeText(
                this,
                getText(R.string.please_enter_valid_abha_address),
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else {
            return true
        }
    }

    private fun handleAbhaSuggestionList() {
        viewModel.abhaAddressSuggestionState.observe(this)
        {
            when (it) {

                is AbhaAddressSuggestionViewState.Idle -> {}
                is AbhaAddressSuggestionViewState.Loading -> progressBarUtils.showLinearProgressbar()
                is AbhaAddressSuggestionViewState.Success -> {
                    progressBarUtils.dismissProgressBar()
                    it.data.abhaAddressList?.forEach { abhaAddress ->
                        createDynamicChips(abhaAddress)
                    }
                }

                is AbhaAddressSuggestionViewState.Error -> {
                    progressBarUtils.dismissProgressBar()
                    ToastUtil.showShortToast(this, it.message)
                }
            }
        }
    }

    private fun handelOnEnrollAbhaAddress() {
        viewModel.enrollAbhaAddressState.observe(this)
        {
            when (it) {

                is EnrollAbhaAddressViewState.Idle -> {}
                is EnrollAbhaAddressViewState.Loading -> progressBarUtils.showCircularProgressbar()
                is EnrollAbhaAddressViewState.Success -> {
                    progressBarUtils.dismissProgressBar()
                    ToastUtil.showShortToast(this, "Move to Create Profile Screen")
                }

                is EnrollAbhaAddressViewState.Error -> {
                    progressBarUtils.dismissProgressBar()
                    ToastUtil.showShortToast(this, it.message)
                }
            }
        }
    }



}
