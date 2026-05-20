package org.intelehealth.app.models

import org.intelehealth.app.models.dto.PatientDTO

data class PatientSearchResult(

    var patient: PatientDTO? = null,
    var source: MatchSource = MatchSource.LOCAL,
    var score: Double = 0.0,
    var ihscore: Double = 0.0,
    var nrscore: Double = 0.0,
    var grade: MatchGrade = MatchGrade.NOT_MATCHED,
    var phoneMatched: Boolean = false,
    var isIHNetwork: Boolean = false,
    var isNRNetwork: Boolean = false,
    var dobMatched: Boolean = false,
    var firstNameScore: Double = 0.0,
    var lastNameScore: Double = 0.0,
    var localDbResult: Boolean = false,
    var openmrsUuid: String? = null,
    var cruid: String? = null
) {

    fun isCertainMatch(): Boolean {
        return score >= 0.95
    }

    fun isProbableMatch(): Boolean {
        return score in 0.80..0.94
    }

    fun isPossibleMatch(): Boolean {
        return score in 0.60..0.79
    }

    fun isNoMatch(): Boolean {
        return score < 0.60
    }

    /**
     * UI label helper
     */
    fun getDisplayLabel(): String {

        return when (grade) {

            MatchGrade.CERTAIN ->
                "Patient already exists"

            MatchGrade.PROBABLE ->
                "Possible duplicate patient"

            MatchGrade.POSSIBLE ->
                "Potential match found"

            MatchGrade.NOT_MATCHED ->
                "No duplicate found"
        }
    }

    /**
     * Score text helper
     */
    fun getFormattedScore(): String {
        return String.format("%.2f", score)
    }
}
enum class MatchGrade {

    CERTAIN,

    PROBABLE,

    POSSIBLE,

    NOT_MATCHED
}
enum class MatchSource {

    LOCAL,
    OPENMRS,
    FHIR,
    CLIENT_REGISTRY
}