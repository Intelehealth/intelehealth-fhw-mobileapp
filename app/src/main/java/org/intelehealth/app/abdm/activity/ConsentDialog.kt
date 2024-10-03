package org.intelehealth.app.abdm.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.app.R
import org.intelehealth.app.abdm.adapter.CheckboxAdapter
import org.intelehealth.app.abdm.model.CheckBoxRecyclerModel
import org.intelehealth.app.database.dao.ProviderDAO
import org.intelehealth.app.databinding.ActivityConsentBinding
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.exception.DAOException
import java.util.Locale

@Suppress("DEPRECATION")
class ConsentDialog : DialogFragment() {

    private var clickable: Clickable? = null
    private var checkboxAdapter: CheckboxAdapter? = null
    private lateinit var modelList: MutableList<CheckBoxRecyclerModel>
    private var sessionManager: SessionManager? = null
    private lateinit var binding: ActivityConsentBinding

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.activity_consent,
            container,
            false
        )


        sessionManager = SessionManager(context)


        // check internet - end
        modelList = ArrayList()
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line1) + NEW_LINE,
                false
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line2) + NEW_LINE,
                false
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line3) + NEW_LINE,
                false
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line4) + NEW_LINE,
                false
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line5) + NEW_LINE,
                false
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                String.format(
                    getString(R.string.abha_consent_line6),
                    fetchHwFullName()
                ) + NEW_LINE, false
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line7) + NEW_LINE,
                false
            )
        )
        checkboxAdapter = CheckboxAdapter(context, modelList) {
            if (checkboxAdapter != null) {
                val allChecked =
                    (binding.rvAbhaConsent.adapter as CheckboxAdapter?)!!.areAllItemsChecked()
                if (allChecked) {
                    binding.btnAcceptPrivacy.setEnabled(true)
                    binding.btnAcceptPrivacy.background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ui2_common_primary_bg
                    )
                } else {
                    binding.btnAcceptPrivacy.setEnabled(false)
                    binding.btnAcceptPrivacy.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ui2_bg_disabled_time_slot
                        )
                }
            }
        }
        binding.rvAbhaConsent.setLayoutManager(LinearLayoutManager(context))
        binding.rvAbhaConsent.setAdapter(checkboxAdapter)


        binding.btnAcceptPrivacy.setOnClickListener {
            clickable?.isChecked(true)
            dismiss()
        }
        binding.btnDecline.setOnClickListener {
            clickable?.isChecked(false)
            dismiss()
        }
        return binding.root
    }

    fun setListeners(clickable: Clickable) {
        this.clickable = clickable
    }

    private fun isValidField(fieldName: String?): Boolean {
        return !fieldName.isNullOrEmpty() && fieldName != "null"
    }

    private fun fetchHwFullName(): String {
        try {
            val providerDAO = ProviderDAO()
            val providerDTO = providerDAO.getLoginUserDetails(sessionManager!!.providerID)
            if (providerDTO != null) {
                val firstname = isValidField(providerDTO.familyName)
                val lastname = isValidField(providerDTO.givenName)
                var userFullName = ""
                if (firstname && lastname) {
                    userFullName = providerDTO.givenName + " " + providerDTO.familyName
                } else if (firstname) {
                    userFullName = providerDTO.givenName
                } else if (lastname) {
                    userFullName = providerDTO.familyName
                }
                return userFullName
            }
        } catch (e: DAOException) {
            e.printStackTrace()
        }
        return "(health worker)"
    }


    private fun declinePP() {  // DECLINE BTN
        //  setResult(AppConstants.CONSENT_DECLINE);

        dismiss()
    }

    fun setLocale(context: Context): Context {
        val sessionManager1 = SessionManager(context)
        val appLanguage = sessionManager1.appLanguage
        val res = context.resources
        val conf = res.configuration
        val locale = Locale(appLanguage)
        Locale.setDefault(locale)
        conf.setLocale(locale)
        context.createConfigurationContext(conf)
        val dm = res.displayMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(LocaleList(locale))
        } else {
            conf.locale = locale
        }
        res.updateConfiguration(conf, dm)
        return context
    }

    companion object {
        const val ABHA_CONSENT: String = "ABHA_CONSENT"
        const val NEW_LINE: String = "<br>"
    }

    interface Clickable {
        fun isChecked(isCheck: Boolean)
    }
}