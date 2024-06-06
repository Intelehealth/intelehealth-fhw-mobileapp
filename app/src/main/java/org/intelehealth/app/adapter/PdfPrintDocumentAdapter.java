package org.intelehealth.app.adapter;

/**
 * Created by Tanvir Hasan on 28-05-2024 : 11-37.
 * Email: mhasan@intelehealth.org
 */
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PdfPrintDocumentAdapter extends PrintDocumentAdapter {

    private final Context context;
    private final String pdfUri;

    public PdfPrintDocumentAdapter(Context context, String pdfUri) {
        this.context = context;
        this.pdfUri = pdfUri;
    }

    @Override
    public void onStart() {
        // Called when the printing starts.
    }

    @Override
    public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes1, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle bundle) {
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        PrintDocumentInfo info = new PrintDocumentInfo.Builder("print_output.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build();
        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
        try (FileInputStream fis = new FileInputStream(pdfUri);
             OutputStream output = new FileOutputStream(destination.getFileDescriptor())) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1 && !cancellationSignal.isCanceled()) {
                output.write(buffer, 0, bytesRead);
            }

            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
            } else {
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        } catch (IOException e) {
            callback.onWriteFailed(e.toString());
        }
    }

    @Override
    public void onFinish() {
        // Called when the printing finishes.
    }
}

