package org.intelehealth.abdm.presentation.abha_verify

/** Two-stage screen: collect the identifier, then the OTP. */
internal enum class AbhaVerifyStep {
    EnterDetails,
    EnterOtp,
}
