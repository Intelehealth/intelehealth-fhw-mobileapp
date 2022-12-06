package org.intelehealth.app.activities.chatHelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.cameraActivity.CameraActivity;
import org.intelehealth.app.activities.help.adapter.ChatSupportAdapter_New;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.DocumentObject;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatHelpActivity_New extends AppCompatActivity {
    private static final String TAG = "ChatHelpActivity_New";
    TextInputEditText etSendMessage;
    TextInputLayout telSendMessage;
    ImageView ivSendAttachment, ivSendMessage;
    LinearLayout layoutCamera, layoutGallery, layoutDocument;
    View layoutChooseOptions;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private Handler mBackgroundHandler;
    RecyclerView rvChatSupport;
    ChatHelpAdapter_New chatHelpAdapter_new;
    List<ChatHelpModel> chattingDetailsList;
    FrameLayout layoutMediaOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_help_new_ui2);

        chattingDetailsList = new ArrayList<>();
        chatHelpAdapter_new = new ChatHelpAdapter_New(this, chattingDetailsList);


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

        ivSendMessage.setOnClickListener(v -> {
            fillDataInList();

        });


        if (CheckInternetAvailability.isNetworkAvailable(this)) {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));
        } else {
            ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }


        clickListeners();
    }

    private void fillDataInList() {
        String outgoingMsg = etSendMessage.getText().toString();
        // SimpleDateFormat formatDate = new SimpleDateFormat("hh:mm a");
        //  Log.d(TAG, "fillDataInList: formatDate : " + formatDate);


        ChatHelpModel c1 = new ChatHelpModel("", outgoingMsg, "",
                "Mon 5 at 4 pm", "", false,
                false, false, false, false,
                false, false, true, "", "");
        chattingDetailsList.add(c1);

      /*  ChatHelpModel c2 = new ChatHelpModel("hi, incoming msg 1", "", "",
                "", "", false,
                false, false, false, false,
                false, false, true, "");
        chattingDetailsList.add(c2);*/

        chatHelpAdapter_new = new ChatHelpAdapter_New(this, chattingDetailsList);
        rvChatSupport.setAdapter(chatHelpAdapter_new);

        Log.d(TAG, "fillDataInList: chattingDetailsList size  :" + chattingDetailsList.size());

        etSendMessage.setText("");
    }

    private void clickListeners() {

        ivSendAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ivSendAttachment");
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
            startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);
        });

        layoutGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                saveImage(mCurrentPhotoPath);
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            if (data != null) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                Log.v("path", picturePath + "");

                // copy & rename the file
                String finalImageName = UUID.randomUUID().toString();
                final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";
                BitmapUtils.copyFile(picturePath, finalFilePath);
                compressImageAndSave(finalFilePath);
            }
        }
    }

    // save image
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
            //update list from here
            ChatHelpModel c1 = new ChatHelpModel("", "", "",
                    "", "", true,
                    false, false, false, false,
                    false, false, false, picturePath, "");
            chattingDetailsList.add(c1);
            Log.d(TAG, "saveImage: chattingDetailsList size : " + chattingDetailsList.size());
            chatHelpAdapter_new = new ChatHelpAdapter_New(this, chattingDetailsList);
            rvChatSupport.setAdapter(chatHelpAdapter_new);
            //chatHelpAdapter_new.add(c1);
            //updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
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


}