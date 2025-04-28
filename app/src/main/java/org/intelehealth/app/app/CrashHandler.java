package org.intelehealth.app.app;

import android.content.Context;
import android.util.Log;

import org.intelehealth.app.utilities.CustomLog;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler instance;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private Context context;

    private CrashHandler(Context context) {
        this.context = context;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new CrashHandler(context);
            Thread.setDefaultUncaughtExceptionHandler(instance);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Convert stack trace to String
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String fullStackTrace = sw.toString();

        // Log crash
        Log.e("CrashHandler", "Uncaught Exception: " + fullStackTrace);

        // Save crash to a file
        saveCrashToFile(fullStackTrace);

        // Send to server (optional)
        sendCrashToServer(fullStackTrace);

        // Pass exception to system default handler (if needed)
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        }
    }

    private void saveCrashToFile(String crashLog) {
        CustomLog.e("CrashHandler", "Uncaught Exception: "+ crashLog);
//        try {
//            File file = new File(context.getFilesDir(), "crash_log.txt");
//            FileWriter writer = new FileWriter(file, true); // Append mode
//            writer.write("\n--- Crash Log ---\n");
//            writer.write(crashLog);
//            writer.write("\n-----------------\n");
//            writer.close();
//            Log.e("CrashHandler", "Crash saved to: " + file.getAbsolutePath());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void sendCrashToServer(String crashLog) {
        // Implement API call to send crash logs to your server
    }
}
