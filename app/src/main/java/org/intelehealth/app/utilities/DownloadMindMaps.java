package org.intelehealth.app.utilities;

import android.content.Context;
import android.os.AsyncTask;

import org.intelehealth.app.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Created by Sagar Shimpi
 * Github - sagars23
 */

public class DownloadMindMaps extends AsyncTask<String, Integer, String> {

    Context context;
    String screenStr="";
    private androidx.appcompat.app.AlertDialog alertDialog;


    public DownloadMindMaps(Context _context, androidx.appcompat.app.AlertDialog alertDialog, String screenStr, boolean from) {
        this.context = _context;
        this.alertDialog = alertDialog;
        this.screenStr=screenStr;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... args) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        String destinationFilePath = "";
        try {
            URL url = new URL(args[0]);
            CustomLog.d("MindMapDownloadTask", "url=" + url);
            destinationFilePath = args[1];

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
            }

            int fileLength=connection.getContentLength();
            //Download Zip
            input = connection.getInputStream();

            CustomLog.d("MindMapDownloadTask", "destinationFilePath=" + destinationFilePath);
            new File(destinationFilePath).createNewFile();
            output = new FileOutputStream(destinationFilePath);

            byte data[] = new byte[4096];
            int count;
            long total = 0;
            while ((count = input.read(data)) != -1) {

                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);

            }

        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }

        File f = new File(destinationFilePath);

        CustomLog.d("MindMapDownloadTask", "f.getParentFile().getPath()=" + f.getParentFile().getPath());
        CustomLog.d("MindMapDownloadTask", "f.getName()=" + f.getName().replace(".zip", ""));

        unpackZip(destinationFilePath);
        return context.getResources().getString(R.string.protocols_downloaded_successfully);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (alertDialog != null)
            alertDialog.dismiss();
        CustomLog.e("MindMapDownloadTask", "Successfully get MindMap URL"+s);
        if(!s.equalsIgnoreCase(context.getResources().getString(R.string.protocols_downloaded_successfully))) {
            if(screenStr.equalsIgnoreCase("setup")){
              //  ((SetupActivity)context).showMindmapFailedAlert();    // Prajwal - commenting this line as it is using old java context. Not sure on what change is needed so commenting it out.
            }else if(screenStr.equalsIgnoreCase("home")){
                SessionManager sessionManager=new SessionManager(context);
                sessionManager.setLicenseKey("");
              //  Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }
        }else{
          //  Toast.makeText(context, s, Toast.LENGTH_LONG).show();
        }
        //Check is there any existing mindmaps are present, if yes then delete.
        File mindMapZip = new File(context.getFilesDir().getAbsolutePath(), "mindmaps.zip");
        CustomLog.e("MindMap Zip=", "" + mindMapZip.exists());
        if (mindMapZip.exists()) {
            mindMapZip.delete();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        CustomLog.e("------>>>",values[0]+"");
    }

//    private boolean unpackZip(String filePath) {
//        InputStream is;
//        ZipInputStream zis;
//        try {
//            File zipfile = new File(filePath);
//            String parentFolder = zipfile.getParentFile().getPath();
//            String filename;
//
//            is = new FileInputStream(filePath);
//            zis = new ZipInputStream(new BufferedInputStream(is));
//            ZipEntry ze;
//            byte[] buffer = new byte[1024];
//            int count;
//
//            while ((ze = zis.getNextEntry()) != null) {
//                filename = ze.getName();
//
//                if (ze.isDirectory()) {
//                    File fmd = new File(parentFolder + "/" + filename);
//                    fmd.mkdirs();
//                    continue;
//                }
//                FileOutputStream fout = new FileOutputStream(parentFolder + "/" + filename);
//
//                while ((count = zis.read(buffer)) != -1) {
//                    fout.write(buffer, 0, count);
//                }
//                fout.close();
//                zis.closeEntry();
//            }
//            zis.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
    private boolean unpackZip(String filePath) {
        InputStream is;
        ZipInputStream zis;
        try {
            File zipfile = new File(filePath);
            String parentFolder = zipfile.getParentFile().getPath();
            String filename;

            is = new FileInputStream(filePath);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {

                filename = ze.getName();
//                File f = new File(parentFolder, ze.getName());

                if (ze.isDirectory()) {
                    File fmd = new File(parentFolder + "/" + filename);
                    fmd.mkdirs();
                    String canonicalPath = fmd.getCanonicalPath();
                    if (!canonicalPath.startsWith(parentFolder)) {
                        // SecurityException
                    }
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(parentFolder + "/" + filename);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }
                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
