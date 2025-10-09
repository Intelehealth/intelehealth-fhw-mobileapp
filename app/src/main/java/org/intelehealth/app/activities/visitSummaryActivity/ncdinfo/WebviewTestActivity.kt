package org.intelehealth.app.activities.visitSummaryActivity.ncdinfo

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.intelehealth.app.R
import java.io.File
import java.net.URL

class WebviewTestActivity : AppCompatActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_test)

        imageView = findViewById(R.id.pdfImageView)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnDownload = findViewById<ImageButton>(R.id.btnDownload)

        val url = intent.getStringExtra("url") ?: return

        // Download PDF file first
        val file = File(cacheDir, "temp.pdf")
        if (!file.exists()) {
            val input = URL(url).openStream()
            file.outputStream().use { input.copyTo(it) }
        }

        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(parcelFileDescriptor)

        showPage(0)

        // Button: Close
        btnClose.setOnClickListener {
            finish()
        }

        // Button: Download
        btnDownload.setOnClickListener {
            // Save PDF to Downloads folder
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outFile = File(downloads, "downloaded.pdf")
            file.copyTo(outFile, overwrite = true)
            Toast.makeText(this, "Saved to Downloads", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPage(index: Int) {
        if (index < 0 || index >= pdfRenderer.pageCount) return
        val page = pdfRenderer.openPage(index)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        imageView.setImageBitmap(bitmap)
        page.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer.close()
        parcelFileDescriptor.close()
    }
}
