package org.intelehealth.app.abdm.dialog

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.intelehealth.app.R
import org.intelehealth.app.abdm.model.AadharApiBody
import org.intelehealth.app.abdm.model.EnrollNumberWithABDMRequest
import org.intelehealth.app.abdm.model.OTPResponse
import org.intelehealth.app.abdm.utils.ABDMConstant
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.databinding.DialogMobileNumberOtpVerificationBinding
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.SnackbarUtils
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.app.utilities.UrlModifiers
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog
import retrofit2.Response


@Suppress("DEPRECATION")
class MobileNumberOtpVerificationDialog : DialogFragment() {
    private lateinit var binding: DialogMobileNumberOtpVerificationBinding
    private val cpd: CustomProgressDialog? = null
    private var snackBarUtils: SnackbarUtils? = null
    private var sessionManager: SessionManager? = null
    private var mobileNumber: String? = null
    private var accessToken: String? = null
    private var txnId: String? = null
    private var onMobileEnrollCompleted: OnMobileEnrollCompleted? = null
    private var resendCounter = 2
    private var countDownTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        snackBarUtils = SnackbarUtils()
        sessionManager = SessionManager(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_mobile_number_otp_verification,
            container,
            false
        )
        resendOtp()
        resendCounterAttemptsTextDisplay()
        binding.sendOtpBtn.setOnClickListener {
            if (binding.otpBox.text.isNullOrEmpty()) {
                snackBarUtils?.showSnackLinearLayoutParentSuccess(
                    context, binding.llParents,
                    StringUtils.getMessageTranslated(
                        getString(R.string.please_enter_otp_received),
                        sessionManager?.appLanguage
                    ), false
                )
            }
            else if (binding.otpBox.text!!.length < 6)
            {
                snackBarUtils?.showSnackLinearLayoutParentSuccess(
                    context, binding.llParents,
                    StringUtils.getMessageTranslated(
                        getString(R.string.please_enter_6_digit_valid_otp),
                        sessionManager?.appLanguage
                    ), false
                )
            }
            else {
                callEnrollABDMWithMobileApi(binding.otpBox.text.toString(), txnId)
            }
        }

        binding.resendBtn.setOnClickListener {
            if (resendCounter != 0) {
                resendCounter--
                resendOtp()
                resendCounterAttemptsTextDisplay()
                callMobileVerificationApi()
            } else
                resendCounterAttemptsTextDisplay()
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        callMobileVerificationApi()
        return super.onCreateDialog(savedInstanceState)
    }

    private fun resendCounterAttemptsTextDisplay() {
        if (resendCounter != 0) binding.tvResendCounter.text =
            resources.getString(R.string.number_of_retries_left, resendCounter)
        else {
            binding.tvResendCounter.text =
                getString(R.string.maximum_number_of_retries_exceeded_please_try_again_after_10_mins)
            binding.resendBtn.isEnabled = false
            binding.resendBtn.setTextColor(resources.getColor(R.color.medium_gray))
            binding.resendBtn.paintFlags = binding.resendBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    private fun callMobileVerificationApi() {
        // payload
        val aadharApiBody = AadharApiBody()
        aadharApiBody.scope = ABDMConstant.SCOPE_MOBILE
        aadharApiBody.value = mobileNumber
        aadharApiBody.txnId = txnId
        val url = UrlModifiers.getAadharOTPVerificationUrl()
        val responseBodySingle =
            AppConstants.apiInterface.GET_OTP_FOR_AADHAR(url, accessToken, aadharApiBody)
        cpd?.show()
        Thread {
            // api - start
            responseBodySingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableSingleObserver<Response<OTPResponse>>() {
                    override fun onSuccess(response: Response<OTPResponse>) {
                        cpd?.dismiss()
                        if (response.code() == 200) {
                            val otpResponse = response.body()
                            snackBarUtils?.showSnackLinearLayoutParentSuccess(
                                context, binding.llParents,
                                StringUtils.getMessageTranslated(
                                    otpResponse?.message ?: "",
                                    sessionManager?.appLanguage
                                ), true
                            )
                            txnId = otpResponse?.txnId
                        }
                    }

                    override fun onError(e: Throwable) {

                        Toast.makeText(
                            context,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()

                        cpd?.dismiss()
                    }
                })
            // api - end
        }.start()
    }


    private fun callEnrollABDMWithMobileApi(otp: String?, txnId: String?) {
        val apiRequest =
            EnrollNumberWithABDMRequest(otp = otp, txnId = txnId, mobileNo = mobileNumber)
        val url = UrlModifiers.getEnrollByAbdmUrl()
        val responseBodySingle =
            AppConstants.apiInterface.getEnrollNumberWithABDM(url, accessToken, apiRequest)
        cpd?.show()
        Thread {
            // api - start
            responseBodySingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableSingleObserver<Response<OTPResponse>>() {
                    override fun onSuccess(response: Response<OTPResponse>) {
                        cpd?.dismiss()
                        if (response.code() == 200) {
                            val otpResponse = response.body()
                            if (otpResponse != null) {

                                if (!otpResponse.authResult.equals("failed", true)) {
                                    snackBarUtils?.showSnackLinearLayoutParentSuccess(
                                        context, binding.llParents,
                                        StringUtils.getMessageTranslated(
                                            otpResponse.message,
                                            sessionManager?.appLanguage
                                        ), true
                                    )
                                    onMobileEnrollCompleted?.mobileRegistered(otpResponse.txnId)
                                    dismiss()
                                } else {
                                    snackBarUtils?.showSnackLinearLayoutParentSuccess(
                                        context, binding.llParents,
                                        StringUtils.getMessageTranslated(
                                            otpResponse.message,
                                            sessionManager?.appLanguage
                                        ), false
                                    )

                                }
                            }

                        } else {
                            snackBarUtils?.showSnackLinearLayoutParentSuccess(
                                context, binding.llParents,
                                StringUtils.getMessageTranslated(
                                    getString(R.string.please_enter_valid_otp),
                                    sessionManager?.appLanguage
                                ), false
                            )

                        }

                    }

                    override fun onError(e: Throwable) {

                        Toast.makeText(
                            context,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()

                        cpd?.dismiss()
                    }
                })
            // api - end
        }.start()
    }

    fun openMobileNumberVerificationDialog(
        accessToken: String?,
        txnId: String?,
        mobileNumber: String?,
        onMobileEnrollCompleted: OnMobileEnrollCompleted,
    ) {
        this.accessToken = accessToken
        this.mobileNumber = mobileNumber
        this.txnId = txnId
        this.onMobileEnrollCompleted = onMobileEnrollCompleted
    }


    private fun resendOtp() {
        binding.resendBtn.isEnabled = false
        binding.resendBtn.setTextColor(ContextCompat.getColor(requireContext(),R.color.medium_gray))

        val resendTime = resources.getString(R.string.resend_otp_in)

        if (countDownTimer != null) countDownTimer?.cancel() // reset any existing countdown.

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (resendCounter!=0)
                {
                    val time =
                        resendTime + " " + millisUntilFinished / 1000 + " " + resources.getString(R.string.seconds)
                    binding.resendBtn.text = time
                }

            }

            override fun onFinish() {
                if ( resendCounter != 0) {
                    binding.resendBtn.isEnabled = true
                    binding.resendBtn.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorPrimary))
                }

                binding.resendBtn.text = resources.getString(R.string.resend_otp)
                if (cpd != null && cpd.isShowing) cpd.dismiss()
            }
        }.start()
    }


    interface OnMobileEnrollCompleted {
        fun mobileRegistered(txnId: String?)
    }

}