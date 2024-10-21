package org.intelehealth

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.work.WorkInfo
import com.github.ajalt.timberkt.Timber
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityDynamicModuleDownloadingBinding
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.utilities.CustomLog.Companion.d
import org.intelehealth.config.room.entity.FeatureActiveStatus
import org.intelehealth.config.worker.ConfigSyncWorker
import org.intelehealth.installer.downloader.DynamicModuleDownloadManager

/**
 * Created by Vaghela Mithun R. on 09-10-2024 - 12:09.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class DynamicModuleDownloadingActivity : BaseActivity() {
    private lateinit var binding: ActivityDynamicModuleDownloadingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDynamicModuleDownloadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnRefreshConfig.setOnClickListener { loadConfig() }
        binding.progressDownloading.max = 100
    }

    private fun loadConfig() {
        ConfigSyncWorker.startConfigSyncWorker(this@DynamicModuleDownloadingActivity) {
            runOnUiThread { binding.txtDownloadStatus.text = "Config Updated" }
        }
    }

    override fun onFeatureActiveStatusLoaded(activeStatus: FeatureActiveStatus?) {
        super.onFeatureActiveStatusLoaded(activeStatus)
        activeStatus?.let {
            val moduleName = getString(R.string.module_video)
            val hasInstalled = manager.isModuleDownloaded(moduleName)

            println("$TAG =>hasInstalled=>$hasInstalled")
            println("$TAG=>activeStatus.getVideoSection()=>" + it.videoSection)

            val modules: MutableList<String> = ArrayList()
            modules.add(moduleName)
            if (it.videoSection && !hasInstalled) {
                println("$TAG=>Downloading")
                d(TAG, "=>Downloading")
                manager.downloadDynamicModule(moduleName)
            } else if (!it.videoSection && hasInstalled) {
                println("$TAG=>uninstalling")
                d(TAG, "=>uninstalling")
                manager.requestUninstall(modules)
            }
        }
    }

    override fun onDownloading(percentage: Int) {
        super.onDownloading(percentage)
        binding.progressDownloading.progress = percentage
        binding.txtPercentageDownloaded.text = "$percentage%"
        binding.txtDownloadStatus.text = "Downloading..."
    }

    override fun onDownloadCompleted() {
        super.onDownloadCompleted()
        binding.txtDownloadStatus.text = "Downloaded"
    }

    override fun onInstallSuccess() {
        super.onInstallSuccess()
        binding.txtDownloadStatus.text = "Installed"
        val intent = Intent(this, DynamicModuleDownloadingActivity::class.java)
        intent.putExtra("from", "splash")
        intent.putExtra("username", "")
        intent.putExtra("password", "")
        startActivity(intent)
        finish()
    }

    override fun onFailed(errorMessage: String) {
        super.onFailed(errorMessage)
        binding.txtDownloadStatus.text = errorMessage
    }

    companion object {
        const val TAG = "DynamicModuleDownloadingActivity"
    }
}