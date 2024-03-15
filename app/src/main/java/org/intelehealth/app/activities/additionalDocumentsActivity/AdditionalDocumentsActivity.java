package org.intelehealth.app.activities.additionalDocumentsActivity;

import static org.intelehealth.app.activities.cameraActivity.CameraActivity.IS_DISPENSE_ADMINISTER;
import static org.intelehealth.app.activities.medicationAidActivity.AdministerDispenseActivity.IMAGE_LIMIT;
import static org.intelehealth.app.activities.medicationAidActivity.AdministerDispenseActivity.IMAGE_LIST_INTENT;
import static org.intelehealth.klivekit.utils.DateTimeUtils.ADD_DOC_IMAGE_FORMAT;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.DocumentObject;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;

import org.intelehealth.app.activities.cameraActivity.CameraActivity;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.BaseActivity;
import org.intelehealth.klivekit.utils.DateTimeUtils;

public class AdditionalDocumentsActivity extends BaseActivity {

    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private Context context;
    private String encounterVitals, visitUuid;
    private String patientUuid, encounterAdultIntials, encounterDispenseAdminister;
    private List<DocumentObject> rowListItem;
    private AdditionalDocumentAdapter recyclerViewAdapter;
    SessionManager sessionManager = null;
    private Handler mBackgroundHandler;
    private boolean isDispenseAdminister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        context = AdditionalDocumentsActivity.this;

        //this language code is no longer required as we are moving towards more optimised as well as generic code for localisation. Check "attachBaseContext".

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
                if (encounterDispenseAdminister != null && !encounterDispenseAdminister.isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("rowListItem", (Serializable) rowListItem);
                    intent.putExtra("encounterDispenseAdminister", encounterDispenseAdminister);
                    setResult(IMAGE_LIST_INTENT, intent);
                    finish();
                } else {
                    onBackPressed();
                }

            }
        });

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            ImagesDAO imagesDAO = new ImagesDAO();
            ArrayList<String> fileuuidList = new ArrayList<String>();
            ArrayList<File> fileList = new ArrayList<File>();

            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            encounterDispenseAdminister = intent.getStringExtra("encounterDispenseAdminister");
            fileuuidList = (ArrayList<String>) intent.getSerializableExtra("fileuuidList");
            isDispenseAdminister = intent.getBooleanExtra("isDispenseAdminister", false);

            try {
                if (encounterDispenseAdminister != null && !encounterDispenseAdminister.isEmpty())
                    fileuuidList = imagesDAO.getImageUuid(encounterDispenseAdminister, UuidDictionary.COMPLEX_IMAGE_AD);    // Encounter Dispense OR Administer.
                else
                    fileuuidList = imagesDAO.getImageUuid(encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);  // Encounter Adultinitial.

//                if (encounterDispenseAdminister == null || encounterDispenseAdminister.isEmpty())
//                    fileuuidList = imagesDAO.getImageUuid(encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);  // Encounter Adultinitial.

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

            if (encounterDispenseAdminister != null && !encounterDispenseAdminister.isEmpty())
                recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterDispenseAdminister, rowListItem, AppConstants.IMAGE_PATH);
            else
                recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterAdultIntials, rowListItem, AppConstants.IMAGE_PATH);

            recyclerView.setAdapter(recyclerViewAdapter);

        }

        getOnBackPressedDispatcher().addCallback(callback);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_additional_docs, menu);
        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == CameraActivity.TAKE_IMAGE) {
//            if (resultCode == RESULT_OK) {
//                String mCurrentPhotoPath = data.getStringExtra("RESULT");
//                saveImage(mCurrentPhotoPath);
////                String mCurrentPhotoPath = data.getStringExtra("RESULT");
////                File photo = new File(mCurrentPhotoPath);
////                if (photo.exists()) {
////                    try{
////
////                        long length = photo.length();
////                        length = length/1024;
////                        Log.e("------->>>>",length+"");
////                    }catch(Exception e){
////                        System.out.println("File not found : " + e.getMessage() + e);
////                    }
////
////                    recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
////                    updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
////                }
//            }
//            if (resultCode == RESULT_CANCELED) {
//
//            }
//
//
//        } else if (requestCode == PICK_IMAGE_FROM_GALLERY) {
//            if (resultCode == RESULT_OK) {
//
//
//                Uri selectedImage = data.getData();
//                String[] filePath = {MediaStore.Images.Media.DATA};
//                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
//                c.moveToFirst();
//                int columnIndex = c.getColumnIndex(filePath[0]);
//                String picturePath = c.getString(columnIndex);
//                c.close();
//                //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
//                Log.v("path", picturePath + "");
//
//                // copy & rename the file
//                String finalImageName = UUID.randomUUID().toString();
//                final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";
//                BitmapUtils.copyFile(picturePath, finalFilePath);
//                compressImageAndSave(finalFilePath);
//
//            }
//            if (resultCode == RESULT_CANCELED) {
//
//            }
//        }
//    }

    private void updateImageDatabase(String imageuuid) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            if (encounterDispenseAdminister != null && !encounterDispenseAdminister.isEmpty())
                imagesDAO.insertObsImageDatabase(imageuuid, encounterDispenseAdminister, UuidDictionary.COMPLEX_IMAGE_AD);
            else
                imagesDAO.insertObsImageDatabase(imageuuid, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);

        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_docs) {
            selectImage();
//                Intent cameraIntent = new Intent(this, CameraActivity.class);
//                String imageName = UUID.randomUUID().toString();
//                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
//                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
//                startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
     * Open dialog to Select douments from Image and Camera as Per the Choices
     */
    private void selectImage() {
        if (encounterDispenseAdminister != null && !encounterDispenseAdminister.isEmpty()) {
            if (rowListItem != null && rowListItem.size() >= IMAGE_LIMIT) {
                Toast.makeText(context, getString(R.string.max_4_images_is_allowed), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(AdditionalDocumentsActivity.this);
        builder.setTitle(R.string.additional_doc_image_picker_title);
        builder.setItems(options, (dialog, item) -> {

            if (item == 0) {
                Intent cameraIntent = new Intent(AdditionalDocumentsActivity.this, CameraActivity.class);

                String imageName = "";
                if (isDispenseAdminister)
                    imageName = filename_openmrsid_datetime_format();
                else
                    imageName = UUID.randomUUID().toString();

                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
                cameraIntent.putExtra(IS_DISPENSE_ADMINISTER, isDispenseAdminister);
                resultCameraContract.launch(cameraIntent);
            }
            else if (item == 1) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(IS_DISPENSE_ADMINISTER, isDispenseAdminister);
                resultGalleryContract.launch(intent);
            }
            else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private final ActivityResultLauncher<Intent> resultCameraContract = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), o -> {
                if (o.getData() != null) {
                    String mCurrentPhotoPath = o.getData().getStringExtra("RESULT");
                    saveImage(mCurrentPhotoPath);
                }
            });

    private final ActivityResultLauncher<Intent> resultGalleryContract = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), o -> {
                if (o.getData() != null) {
                    Uri selectedImage = o.getData().getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    String picturePath = c.getString(columnIndex);
                    c.close();
                    //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                    Log.v("path", picturePath + "");

                    // copy & rename the file
                    String finalImageName = "";
                    if (isDispenseAdminister)
                        showEnterInputDialog(picturePath);
                    else {
                        finalImageName = UUID.randomUUID().toString();
                        final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";
                        BitmapUtils.copyFile(picturePath, finalFilePath);
                        compressImageAndSave(finalFilePath);
                    }
                }
            });

    public void showEnterInputDialog(String picturePath){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_input_entry);

        TextInputEditText textInputEditText = dialog.findViewById(R.id.dialog_editText);
        Button save_button = dialog.findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textInputEditText.getText().toString().isEmpty())
                    textInputEditText.setError(getString(R.string.error_field_required));
                else {
                    String txtInputValue = textInputEditText.getText().toString();
                    String currentDateTime = DateTimeUtils.formatToLocalDate(new Date(), ADD_DOC_IMAGE_FORMAT);
                    String mImageName = txtInputValue + "_" + currentDateTime;

                    final String finalFilePath = AppConstants.IMAGE_PATH + mImageName + ".jpg";
                    BitmapUtils.copyFile(picturePath, finalFilePath);
                    compressImageAndSave(finalFilePath);
                    dialog.dismiss();
                }
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        dialog.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
        textInputEditText.requestFocus();
    }

/*
    public void dialogTextInput(String picturePath) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        View convertView = getLayoutInflater().inflate(R.layout.dialog_input_entry, null);
        materialAlertDialogBuilder.setView(convertView);
        final TextInputEditText textInputEditText = convertView.findViewById(R.id.dialog_editText);

        materialAlertDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (textInputEditText.getText().toString().isEmpty())
                    textInputEditText.setError(getString(R.string.error_field_required));
                else {
                    String txtInputValue = textInputEditText.getText().toString();
                    String currentDateTime = DateTimeUtils.formatToLocalDate(new Date(), ADD_DOC_IMAGE_FORMAT);
                    String mImageName = txtInputValue + "_" + currentDateTime;

                    final String finalFilePath = AppConstants.IMAGE_PATH + mImageName + ".jpg";
                    BitmapUtils.copyFile(picturePath, finalFilePath);
                    compressImageAndSave(finalFilePath);
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = materialAlertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        if (!alertDialog.isShowing())
            alertDialog.show();

        Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
        IntelehealthApplication.setAlertDialogCustomTheme(context, alertDialog);
    }
*/


    /**
     * @param filePath Final Image path to compress.
     */
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
            updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
        }
    }

    private final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (encounterDispenseAdminister != null && !encounterDispenseAdminister.isEmpty()) {
                Intent intent = new Intent();
                intent.putExtra("rowListItem", (Serializable) rowListItem);
                intent.putExtra("encounterDispenseAdminister", encounterDispenseAdminister);
                setResult(IMAGE_LIST_INTENT, intent);
                finish();
            } else
                finish();
        }
    };

    private String filename_openmrsid_datetime_format() {
        String value = "";

        try {
            String openmrsID = new PatientsDAO().getOpenmrsId(patientUuid);
            String currentDateTime = DateTimeUtils.formatToLocalDate(new Date(), ADD_DOC_IMAGE_FORMAT);
            value = openmrsID + "_" + currentDateTime;
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        Log.d("TAG", "filename_openmrsid_datetime_format: " + value);
        return value;
    }

}
