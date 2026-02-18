package org.intelehealth.abdm.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.abdm.databinding.DialogConsentBinding
import org.intelehealth.abdm.adapter.CheckboxAdapter
import org.intelehealth.abdm.model.CheckBoxRecyclerModel
import java.util.Locale
import org.intelehealth.abdm.R;

@Suppress("DEPRECATION")
class ConsentDialog(private val patientName: String) : DialogFragment() {

    private var IS_ACCEPTED: Boolean = false
    private var clickable: Clickable? = null
    private var checkboxAdapter: CheckboxAdapter? = null
    private lateinit var modelList: MutableList<CheckBoxRecyclerModel>
    private var sessionManager: SessionManager? = null
    private lateinit var binding: DialogConsentBinding

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
        binding = DialogConsentBinding.inflate(layoutInflater)
        sessionManager = SessionManager(context)

        // check internet - end
        modelList = ArrayList()
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line1) + NEW_LINE,
                false,
                true
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line2) + NEW_LINE,
                false,
                false
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line3) + NEW_LINE,
                false,
                true
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line4) + NEW_LINE,
                false,
                true
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line5) + NEW_LINE,
                false,
                true
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                String.format(
                    getString(R.string.abha_consent_line6),
                    fetchHwFullName()
                ) + NEW_LINE, false,
                true
            )
        )
        modelList.add(
            CheckBoxRecyclerModel(
                getString(R.string.abha_consent_line7, patientName) + NEW_LINE,
                false,
                true
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
                        R.drawable.bg_common_primary_enabled
                    )
                } else {
                    binding.btnAcceptPrivacy.setEnabled(false)
                    binding.btnAcceptPrivacy.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.bg_timeslot_disabled
                        )
                }
            }
        }
        binding.rvAbhaConsent.setLayoutManager(LinearLayoutManager(context))
        binding.rvAbhaConsent.setAdapter(checkboxAdapter)


        binding.btnAcceptPrivacy.setOnClickListener {
            IS_ACCEPTED = true
            dismiss()
        }
        binding.btnDecline.setOnClickListener {
            IS_ACCEPTED = false
            dismiss()
        }
        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (IS_ACCEPTED) {
            clickable?.isChecked(true)
        } else {
            clickable?.isChecked(false)
        }

    }

    fun setListeners(clickable: Clickable) {
        this.clickable = clickable
    }

    private fun isValidField(fieldName: String?): Boolean {
        return !fieldName.isNullOrEmpty() && fieldName != "null"
    }

    private fun fetchHwFullName(): String {
        val hwName: String = sessionManager?.hwFullName ?: ""
        return if (hwName.isBlank()) {
            "(Health Worker)"
        } else {
            hwName
        }
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