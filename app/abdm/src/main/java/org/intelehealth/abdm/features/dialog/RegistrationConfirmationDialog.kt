package org.intelehealth.abdm.features.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.intelehealth.abdm.R
import org.intelehealth.abdm.common.utils.ToastUtil
import org.intelehealth.abdm.databinding.DialogAbdmRegistrationConfirmationBinding


class RegistrationConfirmationDialog : DialogFragment() {
    private lateinit var mBinding: DialogAbdmRegistrationConfirmationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DialogAbdmRegistrationConfirmationBinding.inflate(inflater, container, false)
        initialization()
        setClickListener()
        return mBinding.root
    }

    private fun initialization() {
        mBinding.btnYes.btnDecline.text = getString(R.string.yes)
        mBinding.btnNo.btnActive.text = getString(R.string.no)
    }

    private fun setClickListener() {
        mBinding.btnYes.btnDecline.setOnClickListener {

        }
        mBinding.btnNo.btnActive.setOnClickListener {
            ToastUtil.showShortToast(requireContext(), "Work in progress")
        }
    }

}

