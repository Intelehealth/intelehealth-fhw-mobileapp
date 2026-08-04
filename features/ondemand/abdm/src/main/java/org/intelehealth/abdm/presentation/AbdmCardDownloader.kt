package org.intelehealth.abdm.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Base64
import android.widget.Toast
import androidx.core.content.FileProvider
import org.intelehealth.abdm.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.intelehealth.abdm.domain.repository.AbhaProfileRepository
import java.io.File
import java.io.FileOutputStream

/**
 * Fire-and-forget ABHA card download, triggered by the host after the patient is created. Fetches
 * the card (base64 image) via the existing profile repository and stores it as a PNG keyed by ABHA
 * number. Ports legacy AbhaCardDownloadUtil, dropping its AbdmManager gate flags: in the ABHA flow
 * we simply download. Failures are swallowed — a missing card never blocks registration.
 */
object AbdmCardDownloader {

    private const val DIR_NAME = "Intelehealth_AbhaCard"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @JvmStatic
    fun downloadInBackground(context: Context, xToken: String?, cardScope: String?, abhaNumber: String?) {
        if (xToken.isNullOrBlank() || cardScope.isNullOrBlank() || abhaNumber.isNullOrBlank()) return
        val appContext = context.applicationContext
        if (cardFile(appContext, abhaNumber).exists()) return

        val repository = EntryPointAccessors
            .fromApplication(appContext, AbdmCardDownloaderEntryPoint::class.java)
            .abhaProfileRepository()

        val bearerToken = if (xToken.startsWith("Bearer ", ignoreCase = true)) xToken else "Bearer $xToken"
        scope.launch {
            repository.fetchAbhaCard(bearerToken, cardScope)
                .onSuccess { card -> store(appContext, card.image, abhaNumber) }
        }
    }

    /**
     * Drops the cached card so the next [downloadInBackground] fetches a fresh one. Call this only from
     * the events that change what the card depicts — a newly verified communication number, or a newly
     * set preferred ABHA address.
     *
     * Eviction rather than a "force refresh" flag because the cached file *is* the cache policy: once
     * the replacement is stored, the existence check suppresses every later call on its own. There is
     * no state left behind that could keep re-triggering downloads on a metered or barely-there
     * connection, which matters because the card is otherwise fetched on each registration save.
     */
    @JvmStatic
    fun invalidate(context: Context, abhaNumber: String?) {
        if (abhaNumber.isNullOrBlank()) return
        runCatching { cardFile(context.applicationContext, abhaNumber).delete() }
    }

    /** Opens the stored card PNG in the system image viewer (legacy parity: FileProvider + ACTION_VIEW). */
    @JvmStatic
    fun viewCard(context: Context, abhaNumber: String?) {
        if (abhaNumber.isNullOrBlank()) return
        val file = cardFile(context.applicationContext, abhaNumber)
        if (!file.exists()) {
            Toast.makeText(context, R.string.abdm_card_not_available, Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
            )
        }
    }

    private fun cardFile(context: Context, abhaNumber: String): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), DIR_NAME)
        return File(dir, "$abhaNumber.png")
    }

    private fun store(context: Context, base64Image: String, abhaNumber: String) {
        runCatching {
            val bytes = Base64.decode(base64Image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return
            val file = cardFile(context, abhaNumber)
            file.parentFile?.let { if (!it.exists()) it.mkdirs() }
            FileOutputStream(file, false).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AbdmCardDownloaderEntryPoint {
    fun abhaProfileRepository(): AbhaProfileRepository
}
