package org.intelehealth.abdm.features.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.intelehealth.abdm.features.R
import org.intelehealth.abdm.features.databinding.DialogAbdmRegistrationConfirmationBinding


class RegistrationConfirmationDialog : DialogFragment() {
    private lateinit var mBinding: DialogAbdmRegistrationConfirmationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mBinding =
            DialogAbdmRegistrationConfirmationBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialization()
        setClickListener()
    }

    private fun initialization() {
        mBinding.btnYes.btnDecline.text = getString(R.string.yes)
        mBinding.btnNo.btnActive.text = getString(R.string.no)
    }

    private fun setClickListener() {
        mBinding.btnYes.btnDecline.setOnClickListener { }
        mBinding.btnNo.btnActive.setOnClickListener { }
    }

}

