package org.intelehealth.app.ui.initialsync

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.intelehealth.app.R
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.databinding.ActivityInitialSyncBinding
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.utilities.Logger
import org.intelehealth.app.utilities.SessionManager
import java.util.concurrent.Executors


class InitialSyncActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInitialSyncBinding

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
            binding.txtProgressTask.text = getString(R.string.sync_completed)
            navigateToHomeScreen()
        }
    }

    private fun navigateToHomeScreen() {
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
}
