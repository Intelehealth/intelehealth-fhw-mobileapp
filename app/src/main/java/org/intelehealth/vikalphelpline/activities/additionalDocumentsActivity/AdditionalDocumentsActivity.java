package org.intelehealth.vikalphelpline.activities.additionalDocumentsActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.intelehealth.vikalphelpline.R;
import org.intelehealth.vikalphelpline.app.AppConstants;
import org.intelehealth.vikalphelpline.database.dao.ImagesDAO;
import org.intelehealth.vikalphelpline.models.DocumentObject;
import org.intelehealth.vikalphelpline.utilities.BitmapUtils;
import org.intelehealth.vikalphelpline.utilities.SessionManager;
import org.intelehealth.vikalphelpline.utilities.UuidDictionary;

import org.intelehealth.vikalphelpline.activities.cameraActivity.CameraActivity;
import org.intelehealth.vikalphelpline.utilities.StringUtils;
import org.intelehealth.vikalphelpline.utilities.exception.DAOException;

public class AdditionalDocumentsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private String encounterAdultIntials;
    private List<DocumentObject> rowListItem;
    private AdditionalDocumentAdapter recyclerViewAdapter;
    SessionManager sessionManager = null;
    String m_finalImageName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
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
                onBackPressed();
            }
        });
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");

            ImagesDAO imagesDAO = new ImagesDAO();
            ArrayList<String> fileNameList = new ArrayList<String>();
            ArrayList<File> fileList = new ArrayList<File>();
            try {
                fileNameList = imagesDAO.getFilename(patientUuid, encounterAdultIntials); //TODO:
                for (String file_imagename : fileNameList) {
                    String filename = AppConstants.IMAGE_PATH + file_imagename + ".jpg";
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

            recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterAdultIntials, rowListItem, AppConstants.IMAGE_PATH, patientUuid);
            recyclerView.setAdapter(recyclerViewAdapter);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_additional_docs, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                String mFilename = data.getStringExtra("FILENAME");
                saveImage(mCurrentPhotoPath, mFilename);
                Log.v("main", "filename: "+ mFilename);
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Log.v("path", picturePath + "");

                EditText editText = new EditText(AdditionalDocumentsActivity.this);
                editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                AlertDialog.Builder builder1 = new AlertDialog.Builder(AdditionalDocumentsActivity.this)
                        .setTitle(R.string.dialog_title_enter_file_name)
                        .setView(editText);
                AlertDialog alertDialog = builder1.create();

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.button_save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         m_finalImageName = editText.getText().toString();
                        final String finalFilePath = AppConstants.IMAGE_PATH + m_finalImageName + ".jpg";
                        BitmapUtils.copyFile(picturePath, finalFilePath);
                        compressImageAndSave(finalFilePath, m_finalImageName);
                    }
                });
                alertDialog.show();

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        m_finalImageName = editText.getText().toString();

                        if (!StringUtils.isValidFileName(m_finalImageName)) {
                            Toast.makeText(AdditionalDocumentsActivity.this, R.string.invalid_filename, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        alertDialog.dismiss();

                        final String finalFilePath = AppConstants.IMAGE_PATH + m_finalImageName + ".jpg";
                        BitmapUtils.copyFile(picturePath, finalFilePath);
                        compressImageAndSave(finalFilePath, m_finalImageName);
                    }
                });
            }
        }
    }

    private Handler mBackgroundHandler;

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    void compressImageAndSave(final String filePath, String filename) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean flag = BitmapUtils.fileCompressed(filePath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (flag) {
                            saveImage(filePath, filename);
                        } else
                            Toast.makeText(AdditionalDocumentsActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private void saveImage(String picturePath, String mfilename_1) {
        Log.v("AdditionalDocuments", "picturePath = " + picturePath);
        File photo = new File(picturePath);
        if (photo.exists()) {
            try {

                long length = photo.length();
                length = length / 1024;
                Log.e("------->>>>", length + "");
            } catch (Exception e) {
                System.out.println("File not found : " + e.getMessage() + e);
            }

            recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
            Log.v("main", "photo_name: "+ photo.getName() + "\n" + photo.getAbsolutePath());
            String image = UUID.randomUUID().toString();
            updateImageDatabase_additional_doc(image, mfilename_1);

        }
    }

    /*     File photo = new File(mCurrentPhotoPath);
                if (photo.exists()) {
                    try{

                        long length = photo.length();
                        length = length/1024;
                        Log.e("------->>>>",length+"");
                    }catch(Exception e){
                        System.out.println("File not found : " + e.getMessage() + e);
                    }

                    recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
                    updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
                }
            }
        }
    }*/
    private void updateImageDatabase_additional_doc(String obsuuid, String mfilename) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase_1(obsuuid, mfilename, encounterAdultIntials,
                    UuidDictionary.COMPLEX_IMAGE_AD);

            imagesDAO.insertInto_tbl_additional_doc(UUID.randomUUID().toString(), patientUuid, encounterAdultIntials, obsuuid, mfilename, "0", "TRUE");

        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }
/*
    private void updateImageDatabase(String imageuuid) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase(imageuuid, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_docs:
                selectImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
                    startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);

                } else if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY);
                }
                else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


}
