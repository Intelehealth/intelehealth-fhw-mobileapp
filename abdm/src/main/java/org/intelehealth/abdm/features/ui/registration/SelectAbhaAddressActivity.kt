package org.intelehealth.abdm.features.ui.registration

import android.os.Bundle
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.utils.ToastUtil
import org.intelehealth.abdm.databinding.ActivitySelectAbhaAddressBinding
import org.intelehealth.abdm.features.base.BaseActivity
import org.intelehealth.abdm.features.ui.registration.dialog.AbhaAddressSuggestionDialogFragment
import org.intelehealth.abdm.features.viewmodel.registration.SelectAbhaAddressViewModel
import java.util.regex.Pattern

@Suppress("DEPRECATION")
@AndroidEntryPoint
class SelectAbhaAddressActivity :
    BaseActivity<ActivitySelectAbhaAddressBinding, SelectAbhaAddressViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialization()
        setClickListener()
    }

    private fun initialization() {
        binding.toolbar.tvToolbarText.text = getString(R.string.select_an_abha_address)
        binding.btnSubmit.btnActive.text = getString(R.string.submit)

    }

    override fun setClickListener() {
        binding.toolbar.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSubmit.btnActive.setOnClickListener {
            ToastUtil.showShortToast(this, "Work in progress")
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

    private fun isValidAbhaRegex(input: String?): Boolean {
        val regex = "^(?!.*[._]{2})(?![._])[a-zA-Z0-9]+([._]?[a-zA-Z0-9]+)*$"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(input)
        return matcher.matches()
    }
}
