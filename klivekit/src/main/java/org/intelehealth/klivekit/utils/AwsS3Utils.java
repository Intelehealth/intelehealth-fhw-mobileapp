package org.intelehealth.klivekit.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AwsS3Utils {
    public static final String ACTION_FILE_UPLOAD_DONE = "org.intelehealth.apprtc.ACTION_FILE_UPLOAD_DONE";

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    public static void saveFileToS3Cloud(Context context, String visitUUid, String filePath) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        PropertyReader propertyReader = new PropertyReader(context);
        Properties properties = propertyReader.getMyProperties("config.properties");
        Log.e("AwsS3Utils", "properties  = " + properties);
        String mimeType;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String sourceFileName = filePath.substring(filePath.lastIndexOf("/") + 1);//new UuidGenerator().UuidGenerator() + "jpg";
                    String fileName = UUID.randomUUID().toString() + "." + filePath.substring(filePath.lastIndexOf(".") + 1);// + "jpg";
                    File sourceFile = new File(filePath);
                    File tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/TEMP/" + fileName);

                    Log.e("AwsS3Utils", "filePath  = " + filePath);
                    Log.e("AwsS3Utils", "sourceFileName  = " + sourceFileName);
                    Log.e("AwsS3Utils", "fileName  = " + fileName);
                    if (!tempFile.exists()) {
                        tempFile.getParentFile().mkdirs();

                        tempFile.createNewFile();
                        Log.e("AwsS3Utils", "File Created");
                    }
                    if (sourceFile.exists()) {

                        InputStream in = new FileInputStream(sourceFile);
                        OutputStream out = new FileOutputStream(tempFile);

                        // Copy the bits from instream to outstream
                        byte[] buf = new byte[1024];
                        int len;

                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        in.close();
                        out.close();

                        Log.v("AwsS3Utils", "Copy file successful.");

                    } else {
                        Log.v("AwsS3Utils", "Copy file failed. Source file missing.");
                    }

                    //String mimeType = getMimeType(context, selectedImageUrl);
               /* if (mimeType.equalsIgnoreCase("jpg") || mimeType.equalsIgnoreCase("png")) {
                    mimeType = "image/" + mimeType;
                }*/
                    Log.v("AwsS3Utils", "saveFileToS3Cloud - fileName : " + fileName);
                    //Log.v("AwsS3Utils", "saveFileToS3Cloud - mimeType : " + mimeType);
                    BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(propertyReader.getAwsAccessID(), propertyReader.getAwsSecretKey());
                    AmazonS3Client amazonS3Client = new AmazonS3Client(basicAWSCredentials);

                    PutObjectRequest por = new PutObjectRequest(propertyReader.getAwsS3BucketName(), fileName, tempFile);
                    //new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/"+PICTURE_NAME));
                    PutObjectResult putObjectResult = amazonS3Client.putObject(por);

                    //Log.v("AmazonS3", amazonS3Client.getBucketLocation(AppConstants.S3_BUCKET_NAME));

                    //String finalMimeType = mimeType;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //UI Thread work here
                            //ResponseHeaderOverrides override = new ResponseHeaderOverrides();
                            //override.setContentType("image/jpeg");
                            //GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(Constants.S3_BUCKET_NAME, fileName);
                            // Added an hour's worth of milliseconds to the current time.
                            //urlRequest.setExpiration(new Date(System.currentTimeMillis() + 3600000));
                            //urlRequest.setResponseHeaders(override);
                            //URL url = amazonS3Client.generatePresignedUrl( urlRequest );
                            if (tempFile.exists()) {
                                if (tempFile.delete()) {
                                    System.out.println("temp file Deleted");
                                } else {
                                    System.out.println("temp file not Deleted");
                                }
                            }
                            String fileUrl = propertyReader.getAwsS3BucketPrefixUrl() + fileName;
                            Intent intent = new Intent();
                            intent.setAction(ACTION_FILE_UPLOAD_DONE);
                            intent.putExtra("fileUrl", fileUrl);
                            context.sendBroadcast(intent);
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
