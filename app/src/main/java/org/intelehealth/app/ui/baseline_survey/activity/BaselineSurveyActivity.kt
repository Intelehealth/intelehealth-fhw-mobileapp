package org.intelehealth.app.ui.baseline_survey.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityBaselineSurveyBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.ui.baseline_survey.factory.BaselineSurveyViewModelFactory
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.utilities.BaselineSurveySource
import org.intelehealth.app.utilities.BaselineSurveyStage
import org.intelehealth.app.utilities.BundleKeys.Companion.BASELINE_CURRENT_SOURCE
import org.intelehealth.app.utilities.BundleKeys.Companion.BASELINE_CURRENT_STAGE
import org.intelehealth.app.utilities.BundleKeys.Companion.PATIENT_UUID
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.DialogUtils.CustomDialogListener
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.NetworkUtils
import org.intelehealth.app.utilities.NetworkUtils.InternetCheckUpdateInterface
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.config.room.entity.FeatureActiveStatus
import org.intelehealth.ncd.constants.Constants


/**
 * Created by Shazzad H Kanon on 06-12-2024 - 11:00.
 * Email : shazzad@intelehealth.org
 * Mob   : +8801647040520
 **/
class BaselineSurveyActivity : BaseActivity() {
    private lateinit var binding: ActivityBaselineSurveyBinding
    private val baselineSurveyViewModel by lazy {
        return@lazy BaselineSurveyViewModelFactory.create(this, this)
    }

    private lateinit var syncAnimator: ObjectAnimator
    private lateinit var actionRefresh: ImageView
    private val networkUtil by lazy {
        NetworkUtils(this, networkStatusListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaselineSurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadFeatureActiveStatus()
        extractAndBindUI()
        handleOnBackPressListener()
        setupActionBar()
        observeCurrentPatientStage()
    }

    private fun handleOnBackPressListener() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Keeping this empty as we don't want to redirect back
            }
        }

        onBackPressedDispatcher.addCallback(this@BaselineSurveyActivity, callback)
    }

    private fun observeCurrentPatientStage() {
        baselineSurveyViewModel.mutableBaselineSurveyStageData.observe(this) { changeIconStatus(it) }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun handleBackPressed() {
        if (baselineSurveyViewModel.baselineEditMode) finish()
        else {
            DialogUtils.patientRegistrationDialog(
                this,
                ContextCompat.getDrawable(this, R.drawable.close_patient_svg),
                resources.getString(R.string.close_baseline_survey),
                resources.getString(R.string.sure_you_want_close_baseline_survey),
                resources.getString(R.string.yes),
                resources.getString(R.string.no)
            ) { action -> if (action == CustomDialogListener.POSITIVE_CLICK) finish() }
        }
    }

    private fun extractAndBindUI() {
        intent?.let {
            val patientId = if (it.hasExtra(PATIENT_UUID)) it.getStringExtra(PATIENT_UUID) else null

            val source = if (it.hasExtra(BASELINE_CURRENT_SOURCE)) {
                IntentCompat.getSerializableExtra(
                    it, BASELINE_CURRENT_SOURCE, BaselineSurveySource::class.java
                )
            } else BaselineSurveyStage.GENERAL

            patientId?.let { id ->
                baselineSurveyViewModel.apply {
                    baselineEditMode = true
                    this.patientId = id
                }
                fetchPatientDetails(id)
            }

            val stage = if (it.hasExtra(BASELINE_CURRENT_STAGE)) {
                IntentCompat.getSerializableExtra(
                    it, BASELINE_CURRENT_STAGE, BaselineSurveyStage::class.java
                )
            } else BaselineSurveyStage.GENERAL

            stage?.let { it1 -> navigateToStage(it1) }
        }
    }

    private fun navigateToStage(stage: BaselineSurveyStage) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostBaselineSurvey) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph =
            navController.navInflater.inflate(R.navigation.navigation_baseline_survey)
        val startDestination = when (stage) {
            BaselineSurveyStage.GENERAL -> R.id.fragmentBaselineGeneral
            BaselineSurveyStage.MEDICAL -> R.id.fragmentBaselineMedical
            BaselineSurveyStage.OTHER -> R.id.fragmentBaselineOther
        }
        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
    }

    private fun fetchPatientDetails(id: String) {
        baselineSurveyViewModel.loadBaselineData(id).observe(this) {
            it ?: return@observe
            baselineSurveyViewModel.handleResponse(it) { data ->
                baselineSurveyViewModel.updateBaselineData(data)
            }
        }
    }

    private fun updatePatientDetails(patient: PatientDTO) = patient.apply {
        if (createdDate.isNullOrEmpty()) {
            createdDate = DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMMM, yyyy")
        }
        if (providerUUID.isNullOrEmpty()) {
            providerUUID = SessionManager.getInstance(this@BaselineSurveyActivity).providerID
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sync, menu)
        menu?.findItem(R.id.action_sync)?.actionView?.let {
            actionRefresh = it.findViewById(R.id.refresh)
            ObjectAnimator.ofFloat<View>(actionRefresh, View.ROTATION, 0f, 359f).apply {
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                duration = 1200
            }.also { anim -> syncAnimator = anim }

            actionRefresh.setOnClickListener { startRefreshing() }
        }

        return true
    }

    private fun startRefreshing() {

        if (NetworkConnection.isOnline(this)) {
            SyncUtils().syncBackground()
        }
        actionRefresh.clearAnimation()
        syncAnimator.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sync) startRefreshing()
        else if (item.itemId == R.id.action_cancel) handleBackPressed()
        return true
    }

    private fun changeIconStatus(stage: BaselineSurveyStage) {
        binding.baselineSurveyTab.apply {
            when (stage) {
                BaselineSurveyStage.GENERAL -> {
                    tvIndicatorBaselineGeneral.isSelected = true
                }

                BaselineSurveyStage.MEDICAL -> {
                    tvIndicatorBaselineGeneral.isActivated = true
                    tvIndicatorBaselineMedical.isSelected = true
                }

                BaselineSurveyStage.OTHER -> {
                    tvIndicatorBaselineGeneral.isActivated = true
                    tvIndicatorBaselineMedical.isActivated = true
                    tvIndicatorBaselineOther.isSelected = true
                }
            }
        }
    }

    override fun onFeatureActiveStatusLoaded(activeStatus: FeatureActiveStatus?) {
        super.onFeatureActiveStatusLoaded(activeStatus)
        if (::syncAnimator.isInitialized) syncAnimator.cancel()
        activeStatus?.let {
            baselineSurveyViewModel.baselineActiveMedicalSection = it.baselineMedicalSection
            baselineSurveyViewModel.baselineActiveOtherSection = it.baselineOtherSection

            if (it.baselineMedicalSection.not() && it.baselineOtherSection.not()) {
                binding.baselineSurveyTab.root.isVisible = false
            } else {
                binding.baselineSurveyTab.root.isVisible = true
                binding.activeBaselineMedical = it.baselineMedicalSection
                binding.activeBaselineOther = it.baselineOtherSection
            }
        }
    }

    override fun onStart() {
        super.onStart()
        networkUtil.callBroadcastReceiver()
    }

    override fun onPause() {
        super.onPause()
        networkUtil.unregisterNetworkReceiver()
    }

    private val networkStatusListener = InternetCheckUpdateInterface {
        if (::actionRefresh.isInitialized) actionRefresh.isEnabled = it
    }

    companion object {
        @JvmStatic
        fun startBaselineSurvey(
            context: Context,
            patientId: String? = null,
            stage: BaselineSurveyStage = BaselineSurveyStage.GENERAL,
            source: BaselineSurveySource = BaselineSurveySource.PATIENT_DETAIL,
            intent: Intent? = null

        ) {
            Intent(context, BaselineSurveyActivity::class.java).apply {
                putExtra(PATIENT_UUID, patientId)
                putExtra(BASELINE_CURRENT_STAGE, stage)
                putExtra(BASELINE_CURRENT_SOURCE, source)
                putExtra(
                    Constants.INTENT_NCD_CATEGORY,
                    intent?.getStringExtra(Constants.INTENT_NCD_CATEGORY)
                )

            }.also { context.startActivity(it) }
        }
    }
}