package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.view.Window
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.net.toUri
import org.intelehealth.app.R
import org.intelehealth.app.utilities.NetworkConnection
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.min


class ShowInfoModuleDialog(
    private val context: Context,
    private val url: String,
    moduleName: String
) {

    private var dialog: Dialog? = null
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private var file: File? = null

    fun show() {
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.dialog_pdf_viewer)

        // Resize dialog to 85% width & 65% height
        dialog?.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
            (context.resources.displayMetrics.heightPixels * 0.65).toInt()
        )
        dialog?.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_for_dialog)
        dialog?.setCanceledOnTouchOutside(false)

        val imageView = dialog?.findViewById<ZoomableImageView>(R.id.pdfImageView)
        val btnClose = dialog?.findViewById<ImageButton>(R.id.btnClose)
        val btnDownload = dialog?.findViewById<ImageButton>(R.id.btnDownload)

        // Create a temporary file for this PDF
        Thread {
            try {
                val tempFile = File.createTempFile("pdf_temp_", ".pdf", context.cacheDir)
                file = tempFile // store reference for download button

                // Download PDF into temp file
                URL(url).openStream().use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Open PDF renderer on UI thread
                (context as Activity).runOnUiThread {
                    parcelFileDescriptor =
                        ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    pdfRenderer = PdfRenderer(parcelFileDescriptor)
                    if (imageView != null) {
                        showPage(0, imageView)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "Failed to load PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()

        // Close button
        btnClose?.setOnClickListener { dismiss() }

        if ((NetworkConnection.isOnline(context))) {
            btnDownload?.setOnClickListener {
                Thread {
                    try {
                        val uri = url.toUri()
                        val fileName2 = uri.lastPathSegment

                        val fileName = uri.lastPathSegment ?: fileName2
                        val downloads =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val outFile = fileName?.let { it1 -> File(downloads, it1) }

                        // Open connection to the URL
                        val connection = URL(url).openConnection()
                        connection.connect()

                        val input = connection.getInputStream()
                        val output = FileOutputStream(outFile)

                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }

                        output.flush()
                        output.close()
                        input.close()

                        // File fully downloaded — show toast with delay
                        (context as Activity).runOnUiThread {
                            Handler(Looper.getMainLooper()).postDelayed({
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.saved_to_downloads),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }, 1000) // 1000ms = 1 second delay
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        (context as Activity).runOnUiThread {
                            Toast.makeText(
                                context,
                                context.getString(R.string.failed_to_save_downloads),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.start()
            }
        }else{
            Toast.makeText(
                context,
                context.getString(R.string.could_not_connect_with_server),
                Toast.LENGTH_SHORT
            ).show()
        }


        // Download button
/*
        btnDownload?.setOnClickListener {
            Thread {
                try {
                    val uri = url.toUri()
                    val fileName2 = uri.lastPathSegment
                    val fileName = uri.lastPathSegment ?: fileName2
                    val downloads =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val outFile = fileName?.let { it1 -> File(downloads, it1) }

                    // Copy the file
                    outFile?.let { it1 -> file?.copyTo(it1, overwrite = true) }

                    // Notify success on main thread
                    (context as Activity).runOnUiThread {
                        Toast.makeText(
                            context,
                            context.getString(R.string.saved_to_downloads),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Notify failure on main thread
                    (context as Activity).runOnUiThread {
                        Toast.makeText(
                            context,
                            context.getString(R.string.failed_to_save_downloads),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.start()
        }
*/


        dialog?.show()
    }

    fun dismiss() {
        pdfRenderer.close()
        parcelFileDescriptor.close()
        dialog?.dismiss()
    }
    private fun showPage(index: Int, imageView: ZoomableImageView) {
        if (index < 0 || index >= pdfRenderer.pageCount) return
        val page = pdfRenderer.openPage(index)

        val dialogWidth = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        val dialogHeight = (context.resources.displayMetrics.heightPixels * 0.7).toInt()

        val scaleX = dialogWidth.toFloat() / page.width
        val scaleY = dialogHeight.toFloat() / page.height
        val scale = min(scaleX, scaleY)

        val bitmapWidth = (page.width * scale).toInt()
        val bitmapHeight = (page.height * scale).toInt()

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        // Center the bitmap in ZoomableImageView
        imageView.post { imageView.setBitmapCentered(bitmap) }

        page.close()
    }

}