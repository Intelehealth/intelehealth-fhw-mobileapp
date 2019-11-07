package io.intelehealth.client.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

    public DownloadMindMaps(Context _context) {
        this.context = _context;
    }

    @Override
    protected String doInBackground(String... args) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        String destinationFilePath = "";
        try {
            URL url = new URL(args[0]);
            destinationFilePath = args[1];

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
            }

            //Download Zip
            input = connection.getInputStream();

            Log.d("MindMapDownloadTask", "destinationFilePath=" + destinationFilePath);
            new File(destinationFilePath).createNewFile();
            output = new FileOutputStream(destinationFilePath);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
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

        Log.d("MindMapDownloadTask", "f.getParentFile().getPath()=" + f.getParentFile().getPath());
        Log.d("MindMapDownloadTask", "f.getName()=" + f.getName().replace(".zip", ""));
        unpackZip(destinationFilePath);
        return "Mindmaps Downloaded Successfully!";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();

        //Check is there any existing mindmaps are present, if yes then delete.
        File mindMapZip = new File(context.getFilesDir().getAbsolutePath(), "mindmaps.zip");
        Log.e("MindMap Zip=", "" + mindMapZip.exists());
        if (mindMapZip.exists()) {
            mindMapZip.delete();
        }
    }

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

                if (ze.isDirectory()) {
                    File fmd = new File(parentFolder + "/" + filename);
                    fmd.mkdirs();
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
