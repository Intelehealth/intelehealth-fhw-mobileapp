package org.intelehealth.app.activities.chatHelp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StrictMode;
import android.provider.MediaStore;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.ui2.calendarviewcustom.CalendarViewDemoActivity;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.ihutils.ui.CameraActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ChatHelpActivity_New extends BaseActivity implements ClickListenerInterface {
    private static final String TAG = "ChatHelpActivity_New";
    TextInputEditText etSendMessage;
    TextInputLayout telSendMessage;
    ImageView ivSendAttachment, ivSendMessage;
    LinearLayout layoutCamera, layoutGallery, layoutDocument;
    View layoutChooseOptions;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private static final int PICKFILE_RESULT_CODE = 2002;

    private Handler mBackgroundHandler;
    RecyclerView rvChatSupport;
    ChatHelpAdapter_New chatHelpAdapter_new;
    List<ChatHelpModel> chattingDetailsList;
    FrameLayout layoutMediaOptions;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    private static final int BUFFER_SIZE = 1024 * 2;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_help_new_ui2);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        chattingDetailsList = new ArrayList<>();
        chatHelpAdapter_new = new ChatHelpAdapter_New(this, chattingDetailsList, this);


        ImageView ivIsInternet = findViewById(R.id.iv_is_internet);
        etSendMessage = findViewById(R.id.et_send_msg_chat);
        telSendMessage = findViewById(R.id.tel_send_msg_chat);
        ivSendAttachment = findViewById(R.id.iv_attachment_chat);
        layoutChooseOptions = findViewById(R.id.layout_options_choose_media);
        ivSendMessage = findViewById(R.id.iv_send_message_chat);
        layoutMediaOptions = findViewById(R.id.layout_media_options);


        layoutCamera = layoutChooseOptions.findViewById(R.id.card_camera_option);
        layoutGallery = layoutChooseOptions.findViewById(R.id.card_gallery_option);
        layoutDocument = layoutChooseOptions.findViewById(R.id.card_document_option);
        rvChatSupport = findViewById(R.id.rv_chatting11);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvChatSupport.setLayoutManager(layoutManager);

        ImageView ivCallSupport = findViewById(R.id.iv_call_support);

        ivCallSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatHelpActivity_New.this, CalendarViewDemoActivity.class);
                startActivity(intent);
            }
        });

        ivSendMessage.setOnClickListener(v -> {
            fillDataInList();

        });


        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ui2_ic_no_internet));

        }
        checkPerm();
        etSendMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                layoutMediaOptions.setVisibility(View.GONE);

                return false;
            }
        });

    }

    private void fillDataInList() {
        String outgoingMsg = etSendMessage.getText().toString();
        // SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
        //  CustomLog.d(TAG, "fillDataInList: formatDate : " + formatDate);


        ChatHelpModel c1 = new ChatHelpModel("", outgoingMsg, "",
                getCurrentTime(), "", false,
                false, false, false, false,
                false, false, true, "", "");
        chattingDetailsList.add(c1);

      /*  ChatHelpModel c2 = new ChatHelpModel("hi, incoming msg 1", "", "",
                "", "", false,
                false, false, false, false,
                false, false, true, "");
        chattingDetailsList.add(c2);*/

        chatHelpAdapter_new = new ChatHelpAdapter_New(this, chattingDetailsList, this);
        rvChatSupport.setAdapter(chatHelpAdapter_new);

        CustomLog.d(TAG, "fillDataInList: chattingDetailsList size  :" + chattingDetailsList.size());

        etSendMessage.setText("");
    }

    private void clickListeners() {

        ivSendAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomLog.d(TAG, "onClick: ivSendAttachment");
                layoutMediaOptions.setVisibility(View.VISIBLE);
            }
        });
        //media selection options
        layoutCamera.setOnClickListener(v -> {
            layoutMediaOptions.setVisibility(View.GONE);

            Intent cameraIntent = new Intent(ChatHelpActivity_New.this, CameraActivity.class);
            String imageName = UUID.randomUUID().toString();
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
            cameraActivityResult.launch(cameraIntent);
        });

        layoutGallery.setOnClickListener(v -> {
            layoutMediaOptions.setVisibility(View.GONE);
          /*  Intent intent = new Intent();
            intent.setType("image/* video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Video"),PICK_IMAGE_FROM_GALLERY);

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY);*/
           /* Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/* video/*");
            startActivityForResult(pickIntent, PICK_IMAGE_FROM_GALLERY);*/

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("*/*");
            photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            galleryActivityResult.launch(photoPickerIntent);
        });

        layoutDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutMediaOptions.setVisibility(View.GONE);

                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("application/pdf");
                layoutMediaResult.launch(chooseFile);
            }
        });
    }

    ActivityResultLauncher<Intent> cameraActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            String mCurrentPhotoPath = result.getData().getStringExtra("RESULT");
            saveImage(mCurrentPhotoPath);
        }
    });

    ActivityResultLauncher<Intent> galleryActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri selectedImage = result.getData().getData();
                CustomLog.d(TAG, "onActivityResult: selectedImage : " + selectedImage);
                if (selectedImage.toString().toLowerCase().contains("image") || selectedImage.toString().toLowerCase().contains(".jpeg") || selectedImage.toString().toLowerCase().contains(".jpg")) {
                    CustomLog.d(TAG, "onActivityResult: im image if");
                    //handle image
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
                } else if (selectedImage.toString().toLowerCase().contains("video") || selectedImage.toString().toLowerCase().contains(".mp4")) {
                    //handle video
                    String filemanagerstring = selectedImage.getPath();
                    CustomLog.d(TAG, "onActivityResult: video result : " + filemanagerstring);

                    // MEDIA GALLERY
                    String selectedImagePath = getPath(selectedImage);
                    if (selectedImagePath != null) {
                        ChatHelpModel c1 = new ChatHelpModel("", "", "",
                                getCurrentTime(), "", false,
                                false, true, false, false,
                                false, false, false, selectedImagePath,
                                "");
                        chattingDetailsList.add(c1);
                        CustomLog.d(TAG, "saveImage: chattingDetailsList size : " + chattingDetailsList.size());
                        chatHelpAdapter_new = new ChatHelpAdapter_New(this, chattingDetailsList, this);
                        rvChatSupport.setAdapter(chatHelpAdapter_new);
                    }

                }


            }
        }
    });

    ActivityResultLauncher<Intent>  layoutMediaResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Uri uri = result.getData().getData();
            String selectedDocPath = getPathNew(uri);
            CustomLog.d(TAG, "onActivityResult: src file path : " + selectedDocPath);
            String filename = selectedDocPath.substring(selectedDocPath.lastIndexOf("/") + 1);
            CustomLog.d(TAG, "onActivityResult: filename  : " + filename);

            ChatHelpModel c1 = new ChatHelpModel("", "", "",
                    getCurrentTime(), "", false,
                    true, false, false,
                    false,
                    false, false, false, selectedDocPath,
                    "");
            chattingDetailsList.add(c1);
            CustomLog.d(TAG, "saveImage: chattingDetailsList size : " + chattingDetailsList.size());
            chatHelpAdapter_new = new ChatHelpAdapter_New(this, chattingDetailsList, this);
            rvChatSupport.setAdapter(chatHelpAdapter_new);
        }
    });

    public String getPathNew(Uri uri) {

        String path = null;
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            path = uri.getPath();
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private void saveImage(String picturePath) {
        CustomLog.v("AdditionalDocuments", "picturePath = " + picturePath);
        try {


            File photo = new File(picturePath);
            if (photo.exists()) {
                try {
                    long length = photo.length();
                    length = length / 1024;
                    CustomLog.e("------->>>>", length + "");
                } catch (Exception e) {
                    System.out.println("File not found : " + e.getMessage() + e);
                }
                //update list from here
                ChatHelpModel c1 = new ChatHelpModel("", "", "",
                        getCurrentTime(), "", true,
                        false, false, false, false,
                        false, false, false, picturePath, "");
                chattingDetailsList.add(c1);
                CustomLog.d(TAG, "saveImage: chattingDetailsList size : " + chattingDetailsList.size());
                chatHelpAdapter_new = new ChatHelpAdapter_New(this, chattingDetailsList, this);
                rvChatSupport.setAdapter(chatHelpAdapter_new);
                //chatHelpAdapter_new.add(c1);
                //updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
            }

        } catch (Exception e) {
            CustomLog.d(TAG, "saveImage: exception : " + e.getLocalizedMessage());
        }
    }

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
                            Toast.makeText(ChatHelpActivity_New.this, getString(R.string.something_went_wrong),
                                    Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            clickListeners();

        }
       /* PermissionListener permissionlistener = new PermissionListener() {

            @Override
            public void onPermissionGranted() {
//                Toast.makeText(SplashActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
//                Timer t = new Timer();
//                t.schedule(new splash(), 2000);

//                TempDialog = new ProgressDialog(SplashActivity.this, R.style.AlertDialogStyle);
//                TempDialog.setMessage("Data migrating...");
//                TempDialog.setCancelable(false);
//                TempDialog.setProgress(i);
//                TempDialog.show();


            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(SplashActivity.this, getString(R.string.permission_denied) + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(R.string.reject_permission_results)
                .setPermissions(*//*Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,*//*
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GROUP_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                checkPerm();
            } else {
                showPermissionDeniedAlert(permissions);
            }

        }
    }

    private void showPermissionDeniedAlert(String[] permissions) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);

        // AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        alertdialogBuilder.setMessage(R.string.reject_permission_results);
        alertdialogBuilder.setPositiveButton(R.string.retry_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkPerm();
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.ok_close_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int getAccountPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (getAccountPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);

        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void performOnClick(String whichItem, String mediaPath) {
        if (!whichItem.isEmpty() && whichItem.equals("image")) {
            showFullScreenImage(ChatHelpActivity_New.this, mediaPath);
        } else if (!whichItem.isEmpty() && whichItem.equals("document")) {
            openDocument(mediaPath);
        }


    }

    private void openDocument(String mediaPath) {
        CustomLog.d(TAG, "openDocument: mediaPath : "+mediaPath);
     /*   File file = new File(Environment.getExternalStorageDirectory(),
                mediaPath);
        Uri path = Uri.fromFile(file);
        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfOpenintent.setDataAndType(path, "application/pdf");
        try {
            startActivity(pdfOpenintent);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        File file = new File(mediaPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    public void showFullScreenImage(Context context, String mediaPath) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_layout_full_screen_image_ui2, null);
        alertdialogBuilder.setView(convertView);
        ImageView ivFullImage = convertView.findViewById(R.id.iv_full_image);

        AlertDialog alertDialog = alertdialogBuilder.create();
        RequestBuilder<Drawable> requestBuilder = Glide.with(this)
                .asDrawable().sizeMultiplier(0.3f);
        Glide.with(ChatHelpActivity_New.this)
                .load(mediaPath)
                .thumbnail(requestBuilder)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(ivFullImage);

        alertDialog.show();


    }

    private String getCurrentTime() {
        LocalTime localTime = LocalTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d).substring(0, 3);
        String formattedTime = dayOfTheWeek + " " + getResources().getString(R.string.at) + " " + localTime.format(dateTimeFormatter);
        return formattedTime;
    }

}