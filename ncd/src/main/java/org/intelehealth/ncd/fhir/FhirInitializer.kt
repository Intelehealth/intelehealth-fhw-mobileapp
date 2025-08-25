package org.intelehealth.ncd.fhir
import com.google.android.fhir.DatabaseErrorStrategy
import com.google.android.fhir.FhirEngineConfiguration
import com.google.android.fhir.FhirEngineProvider
object FhirInitializer {
    fun init() {
        FhirEngineProvider.init(
            FhirEngineConfiguration(
                enableEncryptionIfSupported = true,
                databaseErrorStrategy = DatabaseErrorStrategy.RECREATE_AT_OPEN
            )
        )
    }
}