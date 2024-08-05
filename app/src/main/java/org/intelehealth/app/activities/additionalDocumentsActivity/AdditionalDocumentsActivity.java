package org.intelehealth.app.activities.additionalDocumentsActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import org.intelehealth.app.utilities.CustomLog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.notification.AdapterInterface;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.models.DocumentObject;
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;

import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.ihutils.ui.CameraActivity;

public class AdditionalDocumentsActivity extends BaseActivity implements AdapterInterface {

    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private String encounterAdultIntials;
    private List<DocumentObject> rowListItem;
    private AdditionalDocumentAdapter recyclerViewAdapter;
    SessionManager sessionManager = null;
    private Handler mBackgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_documents);
        Toolbar topToolBar = findViewById(R.id.toolbar);

        //removes the bug of no translation seen even when provided....
        topToolBar.setTitle(getString(R.string.title_activity_additional_documents));
        setSupportActionBar(topToolBar);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");

            ImagesDAO imagesDAO = new ImagesDAO();
            ArrayList<String> fileuuidList = new ArrayList<String>();
            ArrayList<File> fileList = new ArrayList<File>();
            try {
                fileuuidList = imagesDAO.getImageUuid(encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);
                for (String fileuuid : fileuuidList) {
                    String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                    if (new File(filename).exists()) {
                        fileList.add(new File(filename));
                    }
                }
            } catch (DAOException e) {
                e.printStackTrace();
            }
            rowListItem = new ArrayList<>();

            for (File file : fileList)
                rowListItem.add(new DocumentObject(file.getName(), file.getAbsolutePath()));

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
            RecyclerView recyclerView = findViewById(R.id.document_RecyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new AdditionalDocumentAdapter(this,encounterAdultIntials,
                    rowListItem, AppConstants.IMAGE_PATH, this, true);
            recyclerView.setAdapter(recyclerViewAdapter);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_additional_docs, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void updateImageDatabase(String imageuuid) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase(imageuuid, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD, AppConstants.IMAGE_ADDITIONAL_DOC);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_docs:
                selectImage();
//                Intent cameraIntent = new Intent(this, CameraActivity.class);
//                String imageName = UUID.randomUUID().toString();
//                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
//                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
//                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    /**
     *   Open dialog to Select douments from Image and Camera as Per the Choices
     */
    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(AdditionalDocumentsActivity.this);
        builder.setTitle(R.string.additional_doc_image_picker_title);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent cameraIntent = new Intent(AdditionalDocumentsActivity.this, CameraActivity.class);
                    String imageName = UUID.randomUUID().toString();
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
                    cameraActivityResult.launch(cameraIntent);

                } else if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryActivityResult.launch(intent);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    ActivityResultLauncher<Intent> cameraActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            String mCurrentPhotoPath = result.getData().getStringExtra("RESULT");
            saveImage(mCurrentPhotoPath);
        }
    });

    ActivityResultLauncher<Intent> galleryActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if(result.getData()!=null) {
                Uri selectedImage = result.getData().getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                CustomLog.v("path", picturePath + "");

                // copy & rename the file
                String finalImageName = UUID.randomUUID().toString();
                final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";
                BitmapUtils.copyFile(picturePath, finalFilePath);
                compressImageAndSave(finalFilePath);
            }
        }
    });

/**
 * @param filePath Final Image path to compress.
 *
 * */
    void compressImageAndSave(final String filePath) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean flag = BitmapUtils.fileCompressed(filePath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (flag) {
                            saveImage(filePath);
                        } else
                            Toast.makeText(AdditionalDocumentsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }
    private void saveImage(String picturePath) {
        CustomLog.v("AdditionalDocuments", "picturePath = " + picturePath);
        File photo = new File(picturePath);
        if (photo.exists()) {
            try {

                long length = photo.length();
                length = length / 1024;
                CustomLog.e("------->>>>", length + "");
            } catch (Exception e) {
                System.out.println("File not found : " + e.getMessage() + e);
            }

            recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
            updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
        }
    }


    @Override
    public void deleteNotifi_Item(List<NotificationModel> list, int position) {

    }

    @Override
    public void deleteAddDoc_Item(List<DocumentObject> list, int position) {

    }
}
