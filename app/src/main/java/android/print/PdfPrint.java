package android.print;

import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.PublicDirFileSaverUtils;

import java.io.File;

/**
 * Created by Prajwal Waingankar
 * on 10-Aug-20.
 * Github: prajwalmw
 */


public class PdfPrint {
    private static final String TAG = "PdfPrint";
    PrintAttributes attributes;

    public PdfPrint(PrintAttributes printAttributes) {
this.attributes = printAttributes;
    }

    public void print(PrintDocumentAdapter printDocumentAdapter, File path, String filename, CallbackPrint callbackPrint)
    {
            printDocumentAdapter.onLayout(null, attributes, null,
                    new PrintDocumentAdapter.LayoutResultCallback(){
                        @Override
                        public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                            super.onLayoutFinished(info, changed);

                            printDocumentAdapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, getOutputFile(path, filename),
                                    new CancellationSignal(), new PrintDocumentAdapter.WriteResultCallback() {
                                        @Override
                                        public void onWriteFinished(PageRange[] pages) {
                                            super.onWriteFinished(pages);

                                            if (pages.length > 0) {
                                                File file = new File(path, filename);
                                                String path = file.getAbsolutePath();
                                                callbackPrint.success(path);
                                            } else {
                                                callbackPrint.onFailure();
                                            }
                                        }
                                    });
                        }
                    }, null);



    }

    private ParcelFileDescriptor getOutputFile(File path, String filename) {
        CustomLog.v(TAG, "path = "+path);
        CustomLog.v(TAG, "filename = "+filename);
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(path, filename);
        if (file.exists()) file.delete();
        try {
            file.createNewFile();
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
        } catch (Exception e) {
            CustomLog.e(TAG, "Failed to open ParcelFileDescriptor", e);
        }

        return null;
    }

    public interface CallbackPrint {
        public void success(String path);
        public void onFailure();
//        fun success(path: String)
//        fun onFailure()
    }

}
