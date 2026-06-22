package org.intelehealth.ncd.fhir
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import androidx.core.animation.doOnEnd
import androidx.core.graphics.ColorUtils
import com.google.android.material.button.MaterialButton

class QuestionnaireBottomActionController(private val rootView: View) {

    private val bottomActionIds = listOf(
        "cancel_questionnaire",
        "pagination_previous_button",
        "pagination_next_button",
        "review_mode_button",
        "submit_questionnaire"
    )

    // debounce handler
    private val handler = Handler(Looper.getMainLooper())
    private var pendingToggleRunnable: Runnable? = null
    //private var lastEnabledState: Boolean? = null
    private val debounceMs = 300L

    /**
     * Attach auto-toggle behavior:
     * - finds inputs that are marked required (or fallback to visible inputs)
     * - listens to changes and toggles bottom actions using a debounce and state check
     */
    fun attachAutoToggleForRequiredInputs() {
        val inputs = findRelevantInputsForValidation()
        attachListenersToInputs(inputs) {
            // debounce - cancel previous and post new
            pendingToggleRunnable?.let { handler.removeCallbacks(it) }
            val r = Runnable {
                val valid = isPageValidStrict()
                // only change UI if state actually changed
                if (lastEnabledState == null || lastEnabledState != valid) {
                    //setBottomActionsEnabled(valid)
                    lastEnabledState = valid
                }
            }
            pendingToggleRunnable = r
            handler.postDelayed(r, debounceMs)
        }

        // initial evaluation
        val initialValid = isPageValidStrict()
        // setBottomActionsEnabled(initialValid)
        lastEnabledState = initialValid
    }

    /**
     * Find inputs *that matter for validation*:
     * - prefer widgets explicitly marked required (by tag, adjacent '*' TextView, or contentDescription)
     * - fallback to visible input widgets if nothing marked required on page
     */
    private fun findRelevantInputsForValidation(): List<View> {
        val root = rootView ?: return emptyList()
        val candidates = mutableListOf<View>()

        fun recurse(v: View) {
            // common answer widgets
            if (v is EditText || v is RadioGroup || v is CheckBox || v is Switch || v is Spinner) {
                if (v.visibility == View.VISIBLE && v.isEnabled) {
                    // prefer those marked required
                    if (isMarkedRequired(v)) candidates.add(v)
                }
            }
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) recurse(v.getChildAt(i))
            }
        }
        recurse(root)

        // if none explicitly marked required, fall back to all visible inputs
        if (candidates.isEmpty()) {
            val fallback = mutableListOf<View>()
            fun recurse2(v: View) {
                if (v is EditText || v is RadioGroup || v is CheckBox || v is Switch || v is Spinner) {
                    if (v.visibility == View.VISIBLE && v.isEnabled) fallback.add(v)
                }
                if (v is ViewGroup) {
                    for (i in 0 until v.childCount) recurse2(v.getChildAt(i))
                }
            }
            recurse2(root)
            return fallback
        }
        return candidates
    }

    /** Heuristics to detect "required" on a widget */
    private fun isMarkedRequired(v: View): Boolean {
        // 1) explicit tag set by renderer or your code
        val tagVal = v.tag
        if (tagVal is String && tagVal.equals("required", ignoreCase = true)) return true

        // 2) contentDescription or hint containing 'required' or '*'
        val cd = v.contentDescription?.toString() ?: ""
        val hint = when (v) {
            is EditText -> v.hint?.toString() ?: ""
            is Spinner -> v.prompt?.toString() ?: ""
            else -> ""
        }
        if (cd.contains("required", true) || hint.contains("required", true)) return true
        if (cd.contains("*") || hint.contains("*")) return true

        // 3) adjacent TextView in the parent that contains '*' (common pattern: label has asterisk)
        (v.parent as? ViewGroup)?.let { parent ->
            for (i in 0 until parent.childCount) {
                val sib = parent.getChildAt(i)
                if (sib is TextView && sib != v) {
                    val t = sib.text?.toString() ?: ""
                    if (t.contains("*")) return true
                }
            }
        }

        return false
    }

    /** Attach listeners to the set of input views (re-uses earlier attachListeners code) */
    private fun attachListenersToInputs(inputs: List<View>, onChanged: () -> Unit) {
        inputs.forEach { v ->
            when (v) {
                is EditText -> {
                    // avoid attaching multiple watchers -> tag-check
                    val key = "qbac_text_watcher"
                    val existing = v.getTag(key.hashCode())
                    if (existing == null) {
                        val watcher = object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                            }

                            override fun afterTextChanged(s: Editable?) {
                                onChanged()
                            }
                        }
                        v.addTextChangedListener(watcher)
                        v.setTag(key.hashCode(), watcher)
                    }
                }

                is RadioGroup -> {
                    v.setOnCheckedChangeListener { _, _ -> onChanged() }
                }

                is CheckBox -> {
                    v.setOnCheckedChangeListener { _, _ -> onChanged() }
                }

                is Switch -> {
                    v.setOnCheckedChangeListener { _, _ -> onChanged() }
                }

                is Spinner -> {
                    v.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            onChanged()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            onChanged()
                        }
                    }
                }

                else -> {
                    // best-effort
                    v.setOnClickListener { onChanged() }
                }
            }
        }
    }

    /**
     * Strict page-level validation:
     * - checks only the inputs returned by findRelevantInputsForValidation()
     * - ensures required EditText non-empty, RadioGroup has selection, CheckBox true (if required), Spinner selection valid
     */
    private fun isPageValidStrict(): Boolean {
        val inputs = findRelevantInputsForValidation()
        if (inputs.isEmpty()) return true // nothing to validate

        inputs.forEach { v ->
            when (v) {
                is EditText -> {
                    if (v.text.toString().trim().isEmpty()) return false
                }

                is RadioGroup -> {
                    if (v.checkedRadioButtonId == -1) return false
                }

                is CheckBox -> {
                    // consider required checkbox must be checked
                    if (!v.isChecked) return false
                }

                is Switch -> {
                    if (!v.isChecked) return false
                }

                is Spinner -> {
                    val pos = v.selectedItemPosition
                    if (pos == AdapterView.INVALID_POSITION) return false
                    // optionally treat position 0 as hint and invalid:
                    if (pos == 0) return false
                }

                else -> {
                    // unknown widget: be conservative and fail
                    return false
                }
            }
        }
        return true
    }

    private fun findViewByName(name: String): View? {
        val ctx = rootView.context

        // Fast lookup by identifier
        try {
            val resId = ctx.resources.getIdentifier(name, "id", ctx.packageName)
            if (resId != 0) {
                rootView.findViewById<View>(resId)?.let { return it }
            }
        } catch (_: Exception) {
        }

        // Fallback: recursive search
        fun recurse(v: View): View? {
            val id = v.id
            if (id != View.NO_ID) {
                try {
                    val entry = v.resources.getResourceEntryName(id)
                    if (entry.equals(name, ignoreCase = true)) return v
                } catch (_: Exception) {
                }
            }
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) {
                    recurse(v.getChildAt(i))?.let { return it }
                }
            }
            return null
        }
        return recurse(rootView)
    }
    /* private fun View.animateColorAlpha(targetAlpha: Float) {
        animate()
            .alpha(targetAlpha)
            .setDuration(200)   // quick transition (200ms)
            .start()
    }
*/
    /*  private fun enableBottomActions(alphaEnabled: Float = 1.0f) {
        bottomActionIds.forEach { name ->
            findViewByName(name)?.apply {
                if (!isEnabled) {
                    isEnabled = true
                    isClickable = true
                    isFocusable = true
                }
                animateColorAlpha(alphaEnabled)
            }
        }
    }

    private fun disableBottomActions(alphaDisabled: Float = 0.5f) {
        bottomActionIds.forEach { name ->
            findViewByName(name)?.apply {
                if (isEnabled) {
                    isEnabled = false
                    isClickable = false
                    isFocusable = false
                }
                animateColorAlpha(alphaDisabled)
            }
        }
    }*/

    /*fun enableBottomActions(alphaEnabled: Float = 1.0f) {
        bottomActionIds.forEach { name ->
            findViewByName(name)?.apply {
                isEnabled = true
                alpha = alphaEnabled
                isClickable = true
                //isFocusable = true
            }
        }
    }

    fun disableBottomActions(alphaDisabled: Float = 0.2f) {
        bottomActionIds.forEach { name ->
            findViewByName(name)?.apply {
                isEnabled = false
                alpha = alphaDisabled
                isClickable = false
                //isFocusable = false
            }
        }
    }*/

    /*fun setBottomActionsEnabled(enabled: Boolean) {
        if (enabled) enableBottomActions() else disableBottomActions()
    }*/

    private val ENABLE_ANIM_MS = 150L
    private val ALPHA_ENABLED = 1.0f
    private val ALPHA_DISABLED = 0.3f
    // Track last state to avoid duplicate work
    private var lastEnabledState: Boolean? = null

    /** Instant disable: interaction blocked and view dimmed immediately. */
    fun disableBottomActionsImmediate() {
        bottomActionIds.forEach { name ->
            findViewByName(name)?.apply {
                // block interaction immediately
                isEnabled = false
                isClickable = false
                isFocusable = false
                // visual dim instantly to avoid flicker
                alpha = ALPHA_DISABLED
            }
        }
    }

    /**
     * Enable with a short animation. Interaction will be enabled only after the animation ends.
     * This prevents quick enable->disable flicker while keeping logic simple.
     */
    fun enableBottomActionsWithSimpleDelay() {
        bottomActionIds.forEach { name ->
            val v = findViewByName(name) ?: return@forEach

            // Ensure it's currently disabled (safety) and visually dimmed
            v.isEnabled = true
            v.isClickable = true
            v.isFocusable = true
            v.alpha = ALPHA_ENABLED


        }
    }

    /**
     * One-line API to set state smoothly:
     * - when false -> immediate disable
     * - when true  -> enable with a short alpha animation, enabling interaction at the end
     */
    fun setBottomActionsEnabledSmooth(enabled: Boolean) {
        println("QBAC: setBottomActionsEnabledSmooth($enabled), last=$lastEnabledState")
        //if (lastEnabledState == enabled) return  // no change → skip
        //lastEnabledState = enabled
        if (!enabled) {
            disableBottomActionsImmediate()
        } else {
            // To avoid accidental quick toggles causing visual glitches,
            // ensure disabled state first then animate enabling.
            //disableBottomActionsImmediate()
            // Start the enable animation immediately after disabling;
            // you can add a small postDelayed if you want a tiny gap.
            enableBottomActionsWithSimpleDelay()
        }
    }
    fun isNextButtonVisible(): Boolean {
        return findViewByName("pagination_next_button")?.visibility == View.VISIBLE
    }

    fun isPreviousButtonVisible(): Boolean {
        return findViewByName("pagination_previous_button")?.visibility == View.VISIBLE
    }

    fun isNextButtonEnabled(): Boolean {
        return findViewByName("pagination_next_button")?.isEnabled == true
    }

    fun isPreviousButtonEnabled(): Boolean {
        return findViewByName("pagination_previous_button")?.isEnabled == true
    }

    fun isLastPage(): Boolean {
        return !isNextButtonVisible()
    }

    fun isFirstPage(): Boolean {
        return !isPreviousButtonVisible()
    }

    fun getNextButton(): View? {
        return findViewByName("pagination_next_button")
    }
}
