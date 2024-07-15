package org.intelehealth.app.ui.patient.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.navigation.fragment.NavHostFragment
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityPatientRegistrationBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.ui.patient.adapter.PatientInfoPagerAdapter
import org.intelehealth.app.utilities.BundleKeys.Companion.PATIENT_CURRENT_STAGE
import org.intelehealth.app.utilities.BundleKeys.Companion.PATIENT_UUID
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.DialogUtils.CustomDialogListener
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.config.presenter.fields.factory.PatientViewModelFactory
import org.intelehealth.config.room.entity.FeatureActiveStatus
import java.util.UUID

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientRegistrationActivity : BaseActivity() {
    private lateinit var binding: ActivityPatientRegistrationBinding
    private lateinit var pagerAdapter: PatientInfoPagerAdapter
    private val patientViewModel by lazy {
        return@lazy PatientViewModelFactory.create(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        manageTitleVisibilityOnScrolling()
        extractAndBindUI()
        setupActionBar()
        observeCurrentPatientStage()
    }

    private fun observeCurrentPatientStage() {
        patientViewModel.patientStageData.observe(this) { changeIconStatus(it) }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            handleBackPressed()
        }
    }

    private fun handleBackPressed() {
        DialogUtils.patientRegistrationDialog(
            this,
            ContextCompat.getDrawable(this, R.drawable.close_patient_svg),
            resources.getString(R.string.close_patient_registration),
            resources.getString(R.string.sure_you_want_close_registration),
            resources.getString(R.string.yes),
            resources.getString(R.string.no)
        ) { action -> if (action == CustomDialogListener.POSITIVE_CLICK) finish() }
    }

    private fun extractAndBindUI() {
        Timber.d { "extractAndBindUI" }
//        "623b0286-ddba-4ef5-9f40-0da37200465f"
        intent?.let {
            val patientId = if (it.hasExtra(PATIENT_UUID)) it.getStringExtra(PATIENT_UUID)
            else null

            patientId?.let { id ->
                patientViewModel.isEditMode = true
                fetchPatientDetails(id)
            } ?: generatePatientId()

            val stage = if (it.hasExtra(PATIENT_CURRENT_STAGE)) {
                IntentCompat.getSerializableExtra(
                    it,
                    PATIENT_CURRENT_STAGE,
                    PatientRegStage::class.java
                )
            } else PatientRegStage.PERSONAL

            stage?.let { it1 -> navigateToStage(it1) }
        }
    }

    private fun navigateToStage(stage: PatientRegStage) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostPatientReg) as NavHostFragment
        val navController = navHostFragment.navController
        when (stage) {
            PatientRegStage.PERSONAL -> return
            PatientRegStage.ADDRESS -> navController.graph.apply {
                setStartDestination(R.id.fragmentPatientAddressInfo)
            }

            PatientRegStage.OTHER -> navController.graph.apply {
                setStartDestination(R.id.fragmentPatientOtherInfo)
            }
        }
    }

    private fun generatePatientId() {
        Timber.d { "generatePatientId" }
        patientViewModel.updatedPatient(PatientDTO().apply { uuid = UUID.randomUUID().toString() })
    }

    private fun fetchPatientDetails(id: String) {
        patientViewModel.loadPatientDetails(id).observe(this) {
            Timber.d { "Result => ${Gson().toJson(it)}" }
            it ?: return@observe
            patientViewModel.handleResponse(it) { patient ->
                patientViewModel.updatedPatient(patient)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sync, menu)
        return true
    }

    private fun startRefreshing() {
//        val syncAnimator =
//            ObjectAnimator.ofFloat<View>(null, View.ROTATION, 0f, 359f).setDuration(1200)
//        syncAnimator.repeatCount = ValueAnimator.INFINITE
//        syncAnimator.interpolator = LinearInterpolator()
        if (NetworkConnection.isOnline(this)) {
            SyncUtils().syncBackground()
        }
//        refresh.clearAnimation()
//        syncAnimator.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sync)
            startRefreshing()
        return true
    }

//    private fun manageTitleVisibilityOnScrolling() {
//        binding.appBarLayoutPatient.addOnOffsetChangedListener(object : OnOffsetChangedListener {
//            var scrollRange = -1;
//            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
//                if (scrollRange == -1) {
//                    scrollRange = appBarLayout?.totalScrollRange ?: -1
//                }
//
//                binding.collapsingToolbar.title = if (scrollRange + verticalOffset == 0) {
//                    resources.getString(R.string.add_new_patient)
//                } else ""
//            }
//        })
//    }


    private fun changeIconStatus(stage: PatientRegStage) {
        if (stage == PatientRegStage.PERSONAL) {
            binding.patientTab.tvIndicatorPatientPersonal.isSelected = true
        } else if (stage == PatientRegStage.ADDRESS) {
            binding.patientTab.tvIndicatorPatientPersonal.isActivated = true
            binding.patientTab.tvIndicatorPatientAddress.isSelected = true
        } else if (stage == PatientRegStage.OTHER) {
            binding.patientTab.tvIndicatorPatientPersonal.isActivated = true
            binding.patientTab.tvIndicatorPatientAddress.isActivated = true
            binding.patientTab.tvIndicatorPatientOther.isSelected = true
        }
    }

    override fun onFeatureActiveStatusLoaded(activeStatus: FeatureActiveStatus?) {
        super.onFeatureActiveStatusLoaded(activeStatus)
        activeStatus?.let {
            patientViewModel.activeStatusAddressSection = it.activeStatusPatientAddress
            patientViewModel.activeStatusOtherSection = it.activeStatusPatientOther
            binding.addressActiveStatus = it.activeStatusPatientAddress
            binding.otherActiveStatus = it.activeStatusPatientOther
        }
    }

    companion object {
        fun startPatientRegistration(
            context: Context,
            patientId: String,
            stage: PatientRegStage = PatientRegStage.PERSONAL
        ) {
            Intent(context, PatientRegistrationActivity::class.java).apply {
                putExtra(PATIENT_UUID, patientId)
                putExtra(PATIENT_CURRENT_STAGE, stage)
            }.also { context.startActivity(it) }
        }
    }
}