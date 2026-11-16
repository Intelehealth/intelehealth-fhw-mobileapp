package org.intelehealth.abdm.config

/** Configuration contract :app provides to abdm. baseUrl must end with "/". */
interface AbdmConfig {
    val baseUrl: String

    /**
     * The ABHA address suffix for the active environment ("@abdm" for prod, "@sbx" for staging).
     * Used to detect the auto-generated default phr address during enrolment.
     */
    val abhaAddressSuffix: String
}