package org.intelehealth.app.ai.formatter

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.ObsDAO
import org.intelehealth.app.knowledgeEngine.Node
import org.intelehealth.app.models.VitalsObject
import org.intelehealth.app.models.dto.ObsDTO
import org.intelehealth.app.utilities.CustomLog
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.app.utilities.UuidDictionary
import org.intelehealth.app.utilities.exception.DAOException
import org.json.JSONArray
import org.json.JSONObject

/**
 * @author: Nagen Biswal
 * @date 2026-08-12
 *
 * Single entry point for turning a visit's captured form data into the AI backend's
 * structured JSON (see Temp.json for the target shape) and persisting it as the
 * visit's consolidated AI-summary obs.
 *
 * The Node-tree walking, the JSON assembly, and the DB save all live in this one
 * file/package on purpose: dropping org.intelehealth.app.ai.formatter into another
 * project (plus its Node/ObsDAO/UuidDictionary/SessionManager dependencies) should
 * be enough to get this feature working there too.
 *
 * Key mapping is auto-derived from each Node's display text (snake_cased) rather
 * than matched against Temp.json's exact field names (site/radiation/etc.) - revisit
 * [toSnakeCase] / [buildQuestionGroup] once real node data is available and the
 * generated keys need to line up with a fixed backend contract.
 */
object VisitSummaryAiFormatter {

    private const val TAG = "VisitSummaryAiFormatter"

    /**
     * Builds the AI-summary JSON from the visit's captured data and saves it under
     * [encounterUuid] as the [UuidDictionary.AI_JSON_FORMAT_VISIT_SUMMARY_CONCEPT_UUID] obs.
     *
     * [patientSex]/[patientAgeYears] aren't part of the four Node-tree sections but
     * are required for the demographic block - pass through whatever the caller
     * already has for the patient.
     *
     * @return the JSON that was saved, or null if the DB write failed.
     */
    @JvmStatic
    fun formatAndSave(
        encounterUuid: String,
        patientSex: String?,
        patientAgeYears: Int?,
        vital: VitalsObject?,
        chiefComplaintNodes: List<Node>?,
        physicalExamNodes: List<Node>?,
        patientHistoryNodes: List<Node>?,
        familyHistoryNodes: List<Node>?
    ): JSONObject? {
        val json = buildAiSummaryJson(
            patientSex,
            patientAgeYears,
            vital,
            chiefComplaintNodes,
            physicalExamNodes,
            patientHistoryNodes,
            familyHistoryNodes
        )
        return if (saveAiSummaryToDb(encounterUuid, json)) json else null
    }

    // ---------------------------------------------------------------------------
    // JSON assembly
    // ---------------------------------------------------------------------------

    private fun buildAiSummaryJson(
        patientSex: String?,
        patientAgeYears: Int?,
        vital: VitalsObject?,
        chiefComplaintNodes: List<Node>?,
        physicalExamNodes: List<Node>?,
        patientHistoryNodes: List<Node>?,
        familyHistoryNodes: List<Node>?
    ): JSONObject {
        val extraction = JSONObject()
        extraction.put("demographic", buildDemographic(patientSex, patientAgeYears, vital))
        extraction.put("chief_complaint", buildChiefComplaint(chiefComplaintNodes))
        extraction.put("associated_symptom", buildAssociatedSymptoms(chiefComplaintNodes))
        extraction.put("history", buildHistory(patientHistoryNodes, familyHistoryNodes))
        extraction.put("physical_examination", buildPhysicalExamination(physicalExamNodes))

        val root = JSONObject()
        root.put("extraction", extraction)
        return root
    }

    private fun buildDemographic(sex: String?, age: Int?, vital: VitalsObject?): JSONObject {
        val demographic = JSONObject()
        demographic.put("sex", sex ?: JSONObject.NULL)
        demographic.put("age", age ?: JSONObject.NULL)
        demographic.put("vital", buildVital(vital))
        return demographic
    }

    private fun buildVital(vital: VitalsObject?): JSONObject {
        val json = JSONObject()
        json.put("sbp", numericOrNull(vital?.bpsys))
        json.put("dbp", numericOrNull(vital?.bpdia))
        json.put("pulse", numericOrNull(vital?.pulse))
        json.put("rr", numericOrNull(vital?.resp))
        json.put("spo2", numericOrNull(vital?.spo2))
        json.put("weight", numericOrNull(vital?.weight))
        json.put("height", numericOrNull(vital?.height))
        json.put("temp", numericOrNull(vital?.temperature))
        json.put("bmi", numericOrNull(vital?.bmi))
        return json
    }

    /**
     * Root complaint categories (e.g. "Abdominal Pain") are always present in
     * [nodes] whether or not the nurse actually filled them in - same as the
     * legacy formatComplainRecord() walk - so this processes every category
     * unconditionally and only keeps the ones that produced answered fields.
     */
    private fun buildChiefComplaint(nodes: List<Node>?): JSONArray {
        val array = JSONArray()
        nodes.orEmpty()
            .filter { !it.text.equals(Node.ASSOCIATE_SYMPTOMS, ignoreCase = true) }
            .forEach { symptomNode ->
                val symptom = nodeText(symptomNode) ?: return@forEach
                val fields = buildProtocolFields(symptomNode)
                if (fields.length() > 0) {
                    val entry = JSONObject()
                    entry.put("symptom", symptom)
                    entry.put("protocol_fields", fields)
                    array.put(entry)
                }
            }
        return array
    }

    private fun buildProtocolFields(symptomNode: Node): JSONObject {
        val fields = JSONObject()
        collectAnsweredFields(symptomNode.optionsList, fields)
        return fields
    }

    /**
     * Walks the symptom's protocol questions (Site, Duration, ...). Each question's
     * own isSelected() only means "answered somewhere below" - not "this is the
     * picked value" (same convention the UI's chip adapters use) - so the real
     * answer always comes from [resolveValue], keyed by the QUESTION's own text.
     */
    private fun collectAnsweredFields(nodes: List<Node>?, into: JSONObject) {
        nodes.orEmpty().forEach { node ->
            if (!node.isSelected) return@forEach
            val key = nodeText(node)?.let { toSnakeCase(it) } ?: return@forEach
            val answer = resolveValue(node) ?: return@forEach
            into.put(key, answer)
        }
    }

    /**
     * Resolves a Question node down to its answer: a terminal node (no further
     * choices, e.g. free text/date input) carries its own answer via getLanguage();
     * a branching node's answer is whichever child has isSelected == true. If that
     * selected child is itself just a plain terminal pick (e.g. a Site chip like
     * "Middle (C) - Umbilical"), its text is used directly as the scalar answer.
     * If the selected child is itself a further sub-question (e.g. "High Blood
     * Pressure" -> "Diagnosed on" -> a date), it's nested under its own label
     * instead of collapsing away - {"diagnosed_on": "12/Aug/2026"} - so answers
     * with real follow-up structure don't lose that structure.
     */
    private fun resolveValue(node: Node): Any? {
        val options = node.optionsList
        if (node.isTerminal || options.isNullOrEmpty()) {
            return nodeText(node)
        }
        val selectedChild = options.firstOrNull { it.isSelected } ?: return null
        val childOptions = selectedChild.optionsList
        if (selectedChild.isTerminal || childOptions.isNullOrEmpty()) {
            return nodeText(selectedChild)
        }
        val nestedValue = resolveValue(selectedChild) ?: return nodeText(selectedChild)
        val key = nodeText(selectedChild)?.let { toSnakeCase(it) } ?: "value"
        val wrapped = JSONObject()
        wrapped.put(key, nestedValue)
        return wrapped
    }

    private fun buildAssociatedSymptoms(nodes: List<Node>?): JSONObject {
        val associateNode = nodes.orEmpty()
            .firstOrNull { it.text.equals(Node.ASSOCIATE_SYMPTOMS, ignoreCase = true) }
        val present = JSONArray()
        val absent = JSONArray()
        associateNode?.optionsList.orEmpty().forEach { option ->
            val label = nodeText(option) ?: return@forEach
            when {
                option.isSelected -> present.put(label)
                option.isNoSelected -> absent.put(label)
            }
        }
        val json = JSONObject()
        json.put("present", present)
        json.put("absent", absent)
        return json
    }

    private fun buildHistory(patientHistoryNodes: List<Node>?, familyHistoryNodes: List<Node>?): JSONObject {
        val history = JSONObject()
        history.put("patient_history", buildQuestionGroup(patientHistoryNodes))
        history.put("family_history", buildQuestionGroup(familyHistoryNodes))
        return history
    }

    /**
     * Generic converter for a flat list of top-level questions: a multi-choice
     * question (e.g. "Medical History") becomes a nested object with one key per
     * checkbox option; anything else is a single yes/no/free-text answer, resolved
     * the same isSelected-chain way as chief-complaint protocol fields.
     */
    private fun buildQuestionGroup(nodes: List<Node>?): JSONObject {
        val json = JSONObject()
        nodes.orEmpty().forEach { node ->
            val key = nodeText(node)?.let { toSnakeCase(it) } ?: return@forEach
            val options = node.optionsList
            json.put(
                key,
                if (node.isMultiChoice && !options.isNullOrEmpty()) {
                    buildCheckboxGroup(options)
                } else {
                    resolveValue(node) ?: JSONObject.NULL
                }
            )
        }
        return json
    }

    private fun buildCheckboxGroup(options: List<Node>): JSONObject {
        val group = JSONObject()
        options.forEach { option ->
            val key = nodeText(option)?.let { toSnakeCase(it) } ?: return@forEach
            group.put(key, checkboxValue(option))
        }
        return group
    }

    /**
     * A checkbox's own isSelected/isNoSelected IS the real "was this checked"
     * signal (unlike a branching Question, where that flag only means "answered
     * somewhere below"), so this checks it directly. A checked item with its own
     * follow-up question (e.g. "High Blood Pressure" -> "Diagnosed on") nests it
     * via [resolveValue] instead of collapsing to a bare checkmark.
     */
    private fun checkboxValue(option: Node): Any {
        if (!option.isSelected && !option.isNoSelected) return JSONObject.NULL
        val childOptions = option.optionsList
        if (childOptions.isNullOrEmpty()) return nodeText(option) ?: ""
        return resolveValue(option) ?: (nodeText(option) ?: "")
    }

    /** physicalExamNodes is PhysicalExam.getSelectedNodes() - the Location level. */
    private fun buildPhysicalExamination(nodes: List<Node>?): JSONObject {
        val findings = JSONArray()
        collectExamFindings(nodes, findings)
        val json = JSONObject()
        json.put("findings", findings)
        return json
    }

    /**
     * Walks the whole physical-exam tree unconditionally (Location/Exam/Question,
     * whatever the actual depth) rather than assuming a fixed container depth - a
     * fixed 2-level skip under-counted findings because not every exam category
     * nests the same number of levels. A node is recorded as a finding only when
     * it has a directly selected child (a genuine answered Question), using
     * [resolveValue] for the answer so follow-up sub-questions stay nested. Once a
     * node is captured this way, [resolveValue] has already walked its entire
     * selected chain, so recursion stops there instead of rediscovering the same
     * leaf again as its own separate (and now orphaned-looking) entry.
     */
    private fun collectExamFindings(nodes: List<Node>?, into: JSONArray) {
        nodes.orEmpty().forEach { node ->
            val hasSelectedChild = node.optionsList.orEmpty().any { it.isSelected }
            if (hasSelectedChild) {
                val name = nodeText(node)
                val answer = resolveValue(node)
                if (name != null && answer != null) {
                    val entry = JSONObject()
                    entry.put("name", name)
                    entry.put("answer", answer)
                    into.put(entry)
                    return@forEach
                }
            }
            collectExamFindings(node.optionsList, into)
        }
    }

    // ---------------------------------------------------------------------------
    // Node text / value helpers
    // ---------------------------------------------------------------------------

    /**
     * A node's canonical text: prefer "language" (the filled-in typed/picked value)
     * over the raw "text" label; "%" is a magic "no real value" placeholder used
     * throughout the mind-map data, so a language of exactly "%" is skipped in
     * favor of falling back to "text" rather than treating the whole node as blank.
     */
    private fun nodeText(node: Node): String? {
        val language = node.language
        val value = if (!language.isNullOrEmpty() && language != "%") language else node.text
        return if (value.isNullOrBlank()) null else value
    }

    private fun toSnakeCase(text: String): String =
        text.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

    private fun numericOrNull(value: String?): Any {
        if (value.isNullOrBlank()) return JSONObject.NULL
        val number = value.toDoubleOrNull() ?: return value
        return if (number == number.toLong().toDouble()) number.toLong() else number
    }

    // ---------------------------------------------------------------------------
    // DB save
    // ---------------------------------------------------------------------------

    private fun saveAiSummaryToDb(encounterUuid: String, json: JSONObject): Boolean {
        return try {
            val sessionManager = SessionManager(IntelehealthApplication.getAppContext())
            val obsDAO = ObsDAO()
            val existingObsUuid = obsDAO.getObsuuid(encounterUuid, UuidDictionary.AI_JSON_FORMAT_VISIT_SUMMARY_CONCEPT_UUID)

            val obsDTO = ObsDTO()
            obsDTO.conceptuuid = UuidDictionary.AI_JSON_FORMAT_VISIT_SUMMARY_CONCEPT_UUID
            obsDTO.encounteruuid = encounterUuid
            obsDTO.creator = sessionManager.creatorID
            obsDTO.value = StringUtils.getValue(json.toString())

            val isSaved = if (existingObsUuid != null) {
                obsDTO.uuid = existingObsUuid
                obsDAO.updateObs(obsDTO)
            } else {
                obsDAO.insertObs(obsDTO)
            }
            CustomLog.i(TAG, "saveAiSummaryToDb: encounterUuid=$encounterUuid isSaved=$isSaved")
            CustomLog.d(TAG, "saveAiSummaryToDb: json=${json.toString(2)}")
            isSaved
        } catch (e: DAOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }
}
