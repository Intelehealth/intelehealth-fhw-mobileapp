package org.intelehealth.app.ui.initialsync

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.app.R
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.databinding.ActivityInitialSyncBinding
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.utilities.Logger
import org.intelehealth.app.utilities.SessionManager
import java.util.concurrent.Executors


class InitialSyncActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInitialSyncBinding
    private var dotAnimators: List<ObjectAnimator> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitialSyncBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        doWork()
    }

    private fun setupUI() {
        val loggedInUser = intent.getStringExtra("loggedInUser").orEmpty()
        binding.apply {
            txtProgressTitle.text = getString(R.string.welcome_message, loggedInUser)
            txtProgressContent.text = getString(R.string.sync_wait_message)
            txtProgressTask.text = getString(R.string.syncing)
            progressIndicator.progress = 0 //default

        }
        startDotsAnimation()
    }

    /**
     * Pulses the three dots below "Syncing" on a staggered, endless loop so the screen still reads as
     * active during the long stretches between progress broadcasts.
     */
    private fun startDotsAnimation() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
        dotAnimators = dots.mapIndexed { index, dot ->
            ObjectAnimator.ofFloat(dot, View.ALPHA, 0.3f, 1f).apply {
                duration = DOT_PULSE_DURATION_MS
                startDelay = index * DOT_PULSE_STAGGER_MS
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                start()
            }
        }
    }

    private fun stopDotsAnimation() {
        dotAnimators.forEach { it.cancel() }
        dotAnimators = emptyList()
    }

    private fun doWork() {
        val sessionManager = SessionManager(IntelehealthApplication.getAppContext())
        if (sessionManager.isFirstTimeLaunched()) {
            val liveData = SyncDAO.getSyncProgress_LiveData()
            if (liveData.value == null) {
                updateProgressUI(0)
            }

            SyncDAO.getSyncProgress_LiveData().observe(this, syncObserver)
            runInBackground { SyncUtils().initialSync("home", this) }
        }
    }

    private val syncObserver = Observer<Int> { progress ->
        Log.d("progresssync", "progress: $progress")
        val safeProgress = progress.coerceIn(0, 100)
        runOnUiThread { updateProgressUI(safeProgress) }

        if (safeProgress == 100) handleSyncCompletion()
    }

    private fun updateProgressUI(progress: Int) {
        binding.apply {
            progressIndicator.progress = progress
            txtProgress.text = getString(R.string.sync_progress_percentage, progress)
        }
        Logger.logD(SyncDAO.PULL_ISSUE, "% -> $progress")
    }

    private fun handleSyncCompletion() {
        SyncDAO.getSyncProgress_LiveData().removeObserver(syncObserver)
        runOnUiThread {
            stopDotsAnimation()
            binding.txtProgressTask.text = getString(R.string.sync_completed)
            navigateToHomeScreen()
        }
    }

    private fun navigateToHomeScreen() {
        val sessionManager = SessionManager(this@InitialSyncActivity)
        // saving here the app last sync time instead of InitialSyncIntentService
        sessionManager.setLastSyncDateTime(AppConstants.dateAndTimeUtils.getcurrentDateTime(sessionManager.appLanguage))

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, HomeScreenActivity_New::class.java).apply {
                putExtra("setup", true)
                putExtra("firstLogin", "firstLogin")
            })
            finish()
        }, 1000)
    }

    private fun runInBackground(task: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            task()
        }
    }

    override fun onDestroy() {
        stopDotsAnimation()
        super.onDestroy()
    }

    private companion object {
        const val DOT_PULSE_DURATION_MS = 400L
        const val DOT_PULSE_STAGGER_MS = 150L
    }
}
