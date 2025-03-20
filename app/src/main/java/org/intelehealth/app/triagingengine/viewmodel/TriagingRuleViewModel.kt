package org.intelehealth.app.triagingengine.viewmodel

//import org.mozilla.javascript.Context
//import org.mozilla.javascript.Scriptable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.MapContext
import org.intelehealth.app.triagingengine.TriagingRepository
import org.intelehealth.app.triagingengine.model.TriageCalculatedResultModel
import org.intelehealth.app.triagingengine.model.rules.AgeRiskScore
import org.intelehealth.app.triagingengine.model.rules.PopupResult
import org.intelehealth.app.triagingengine.model.rules.ReferralRule
import org.intelehealth.app.triagingengine.model.rules.SymptomDuration
import org.intelehealth.app.triagingengine.model.rules.SymptomDurationRiskScore
import org.intelehealth.app.triagingengine.network.Resource
import org.intelehealth.common.triagingrule.model.rules.TriagingReferralRule
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel
import javax.inject.Inject


/**
 * Created by Lincon Pradhan on  03-02-2025.
 **/
@HiltViewModel
class TriagingRuleViewModel @Inject constructor(private val repository: TriagingRepository) :
    BaseViewModel() {
    private val _triagingReferralRuleData = MutableLiveData<Resource<TriagingReferralRule>>()
    val triagingReferralRuleData: LiveData<Resource<TriagingReferralRule>> get() = _triagingReferralRuleData

    private val _triageCalculatedResultModel = MutableLiveData<TriageCalculatedResultModel>()
    val triageCalculatedResultModel: LiveData<TriageCalculatedResultModel> get() = _triageCalculatedResultModel

    private val _durationInDays = MutableLiveData<Int>();
    val durationInDays: LiveData<Int> get() = _durationInDays

    // Function to load the triaging rule data
    fun loadTriagingRuleData() {
        // Load triaging rule data from asset
        viewModelScope.launch {
            //TODO: need to manage the rules data in the local storage i.e. preference or database and not to load every time from the server
            _triagingReferralRuleData.value = Resource.Loading()
            val result = repository.loadTriagingRuleData()
            _triagingReferralRuleData.value = result
            //generateTriageResult()

        }
    }

    // Function to generate the triage result
    fun generateTriageResult(age: Int, mmName: String, durationInDay: Int) {
        println("generateTriageResult - age : $age , mmName : $mmName , durationInDay : $durationInDay")
        // Generate triage result
        viewModelScope.launch {
            triagingReferralRuleData.value?.data?.let { it ->
                val ageRiskScore = foundAgeRiskScore(age, it.ruleset.ageRiskScore)
                val symptomRiskScore =
                    it.ruleset.symptomRiskScore.find { it.name == mmName }?.score ?: 0
                val symptomDurationRiskScore =
                    foundSymptomRiskScore(durationInDay, it.ruleset.symptomDurationRiskScore)

                println("generateTriageResult - Age Risk Score : $ageRiskScore")
                println("generateTriageResult - Symptom Risk Score : $symptomRiskScore")
                println("generateTriageResult - Symptom Duration Risk Score : $symptomDurationRiskScore")
                val referralScore = evaluateReferralScoreExpression(
                    ageRiskScore,
                    symptomRiskScore,
                    symptomDurationRiskScore,
                    it.ruleset.formulas[0].expression
                )

                val referralRuleResult =
                    findReferralRuleResult(referralScore, it.ruleset.referralRules)

                println("generateTriageResult - Referral Score : $referralScore")

                referralRuleResult?.let { it11 ->
                    println("generateTriageResult - Referral Rule Result : $it11")
                    val x = referralRuleResult.resultFacilityCategory // Value to match

                    val matchingPopupResult = evaluatePopupExpression(x, it.ruleset.popupResult)
                    //triagingReferralRuleData.value?.data?.ruleset?.popupResult?.firstOrNull { it.condition == "x == '$x'" }?.popup
                    println("generateTriageResult -matchingPopup : ${matchingPopupResult?.popup}")
                    matchingPopupResult?.let { it2 ->
                        _triageCalculatedResultModel.value = TriageCalculatedResultModel(
                            popupResult = it2.popup,
                            popupResultHi = it2.popupHi,
                            resultFacilityType = referralRuleResult.resultFacilityType,
                            resultFacilityCategory = referralRuleResult.resultFacilityCategory,
                            resultHealthProviderDesignations = referralRuleResult.resultHealthProviderDesignations,
                            riskName = referralRuleResult.name,
                            riskNameHi = referralRuleResult.nameHi
                        )
                    }
                }

            }
        }
    }

    //  Function to evaluate the popup expression
    private fun evaluatePopupExpression(
        x: String,
        popupResults: List<PopupResult>
    ): PopupResult? {
        for (popupResult in popupResults) {
            val rawExpression = popupResult.condition

            val jexl = JexlBuilder().create()
            val context = MapContext()
            context.set("x", x)
            val expression = jexl.createExpression(rawExpression)
            val res = expression.evaluate(context) as Boolean
            if (res) {
                return popupResult
            }
        }
        return null
    }

    // Function to find the referral rule result
    private fun findReferralRuleResult(
        referralScore: Int,
        referralRules: List<ReferralRule>
    ): ReferralRule? {
        for (referralRule in referralRules) {
            val rawExpression = referralRule.condition

            val jexl = JexlBuilder().create()
            val context = MapContext()
            context.set("result", referralScore)
            val expression = jexl.createExpression(rawExpression)
            val res = expression.evaluate(context) as Boolean
            if (res) {
                return referralRule
            }
        }
        return null
    }

    // Function to find the age risk score
    private fun foundAgeRiskScore(age: Int, conditions: List<AgeRiskScore>): Int {
        for (condition in conditions) {
            val rawExpression = condition.condition

            val jexl = JexlBuilder().create()
            val context = MapContext()
            context.set("age", age)
            val expression = jexl.createExpression(rawExpression)
            val res = expression.evaluate(context) as Boolean
            if (res) {
                return condition.score
            }
        }
        return 0  // Default score if no conditions match
    }

    // Function to find the symptom risk score
    private fun foundSymptomRiskScore(
        duration: Int,
        conditions: List<SymptomDurationRiskScore>
    ): Int {
        for (condition in conditions) {
            val rawExpression = condition.condition

            val jexl = JexlBuilder().create()
            val context = MapContext()
            context.set("duration", duration)
            val expression = jexl.createExpression(rawExpression)
            val res = expression.evaluate(context) as Boolean
            if (res) {
                return condition.score
            }
        }
        return 0  // Default score if no conditions match
    }

    // Function to evaluate the referral score expression
    private fun evaluateReferralScoreExpression(
        x: Int,
        y1: Int,
        y2: Int,
        rawExpression: String
    ): Int {

        val jexl = JexlBuilder().create()
        val context = MapContext()
        context.set("x", x)
        context.set("y1", y1)
        context.set("y2", y2)
        val expression = jexl.createExpression(rawExpression)
        val res = expression.evaluate(context) as Int

        return res

    }


    // Function to calculate the duration in days
    fun calculateDurationInDays(mmName: String, clinicalString: String) {
        // ►<b>Skin disorder</b>: <br/>• Type of the skin lesion* - जाघ में दर्द.<br/>• Site - Legs, जाघ में दर्द.<br/>• No. of lesions* - Single lesions.<br/>• Duration -  2 Days.<br/>• Progression - Transient.<br/>• H/o specific illness - None.<br/>• Exposure to irritants/offending agents - No.<br/>• Prior treatment sought - None.<br/>• Additional information - सीएसी में.<br/>
        // <br/>• Duration - Subacute ( 2 - 8 weeks)
        // <br/>• Duration -  2 Days
        //
        viewModelScope.launch {
            triagingReferralRuleData.value?.data?.let {
                // else put zero as default
                val duration = getDurationForMatch(mmName, it.ruleset.symptomDuration) ?: 0
                if (duration > 0) {
                    _durationInDays.value = duration
                } else {
                    val days = convertDurationToDays(clinicalString) ?: 0
                    _durationInDays.value = days
                }
            }
        }

    }

    // Function to convert the duration to days
    private fun convertDurationToDays(durationString: String): Int? {
        println("convertDurationToDays - durationString : $durationString")
        /* val regex = Regex(
             """<br/>•\s*(duration|since)\s*-\s*(\d+)\s*(days?|hours?|weeks?|months?|years?)""",
             RegexOption.IGNORE_CASE
         )*/
        val regex = Regex(
            """<br/>•\s*(duration of [^ -]+|duration|since)\s*-\s*(\d+)\s*(days?|hours?|weeks?|months?|years?)""",
            RegexOption.IGNORE_CASE
        )
        val matchResult = regex.find(durationString)
        //[<br/>•Duration - 5 days, Duration, 5, days]
        // [<br/>• since -  5 Days, since, 5, Days]
        return matchResult?.let { match ->
//            val value = match.groupValues[2].toInt() // Extract the number
//            val unit = match.groupValues[3].lowercase() // Extract the unit
            val value = match.groupValues[match.groupValues.size - 2].toInt() // Extract the number
            val unit = match.groupValues[match.groupValues.size - 1].lowercase() // Extract the unit

            when {
                unit.startsWith("hour") -> value / 24 // Convert hours to days
                unit.startsWith("day") -> value // Keep days as is
                unit.startsWith("week") -> value * 7 // Convert weeks to days
                unit.startsWith("month") -> value * 30 // Approximate months to 30 days
                unit.startsWith("year") -> value * 365 // Convert years to days
                else -> null // Return null if no valid unit is found
            }
        }
    }

    // Function to get the duration for a matching mm_name
    private fun getDurationForMatch(
        mmName: String,
        symptomDurationList: List<SymptomDuration>
    ): Int? {
        // Find the first matching mm_name and return its duration
        return symptomDurationList.find { it.mmName == mmName }?.duration
    }

}
