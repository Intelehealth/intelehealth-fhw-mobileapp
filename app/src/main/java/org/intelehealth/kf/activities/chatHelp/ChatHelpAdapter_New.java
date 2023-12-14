package org.intelehealth.kf.activities.chatHelp;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.kf.R;
import org.intelehealth.kf.app.AppConstants;
import org.intelehealth.kf.appointment.model.AppointmentInfo;
import org.intelehealth.kf.database.dao.ImagesDAO;
import org.intelehealth.kf.database.dao.PatientsDAO;
import org.intelehealth.kf.utilities.DownloadFilesUtils;
import org.intelehealth.kf.utilities.Logger;
import org.intelehealth.kf.utilities.SessionManager;
import org.intelehealth.kf.utilities.UrlModifiers;
import org.intelehealth.kf.utilities.exception.DAOException;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class ChatHelpAdapter_New extends RecyclerView.Adapter<ChatHelpAdapter_New.MyViewHolder> {
    Context context;
    List<ChatHelpModel> chattingDetailsList;
    private static final String TAG = "ChatHelpAdapter_New";
    ClickListenerInterface clickListenerInterface;
    String whichItem = "";
    String mediaPath = "";

    public ChatHelpAdapter_New(Context context) {
        this.context = context;

    }

    public ChatHelpAdapter_New(Context context, List<ChatHelpModel> chattingDetailsList, ClickListenerInterface clickListenerInterface) {
        this.context = context;
        this.chattingDetailsList = chattingDetailsList;
        this.clickListenerInterface = clickListenerInterface;
    }

    @Override
    public ChatHelpAdapter_New.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_chat_text_ui2, parent, false);
        ChatHelpAdapter_New.MyViewHolder myViewHolder = new ChatHelpAdapter_New.MyViewHolder(view);

        return myViewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onBindViewHolder(ChatHelpAdapter_New.MyViewHolder holder, int position) {
        ChatHelpModel chatHelpModel = chattingDetailsList.get(position);
        Log.d(TAG, "onBindViewHolder: image path : " + chatHelpModel.getOutgoingMediaPath());
        Log.d(TAG, "onBindViewHolder: isimage : " + chatHelpModel.isOutgoingMsgImage());
        Log.d(TAG, "onBindViewHolder: istext : " + chatHelpModel.isOutgoingMsgText());
        Log.d(TAG, "onBindViewHolder: outgoing video path : " + chatHelpModel.getOutgoingMediaPath());

        holder.tvIncomingMsgTime.setText(chatHelpModel.getOutgoingMsgTime());
        if (chatHelpModel.isOutgoingMsgText()) {

            Log.d(TAG, "onBindViewHolder: in if");

            holder.tvOutgoingMsg.setVisibility(View.VISIBLE);
            holder.cardOutgoingImage.setVisibility(View.GONE);
            holder.layoutSentStatus.setVisibility(View.VISIBLE);
            holder.cardOutgoingDocument.setVisibility(View.GONE);

            holder.tvOutgoingMsg.setText(chatHelpModel.getOutgoingMsg());
        } else if (chatHelpModel.isOutgoingMsgImage()) {
            whichItem = "image";
            mediaPath = chatHelpModel.getOutgoingMediaPath();
            holder.tvOutgoingMsg.setVisibility(View.GONE);
            holder.cardOutgoingImage.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder: in else");
            holder.layoutSentStatus.setVisibility(View.VISIBLE);
            holder.cardOutgoingDocument.setVisibility(View.GONE);

            /*if (chatHelpModel.getOutgoingMediaPath() == null || chatHelpModel.getOutgoingMediaPath().equalsIgnoreCase("")) {
                if (NetworkConnection.isOnline(context)) {
                    //  profilePicDownloaded(chatHelpModel, holder);
                }
            }
*/

            if (chatHelpModel.getOutgoingMediaPath() != null && !chatHelpModel.getOutgoingMediaPath().isEmpty()) {
                Log.d(TAG, "onBindViewHolder:  glide");
                Glide.with(context)
                        .load(chatHelpModel.getOutgoingMediaPath())
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.ivSelectedImage);
            } else {
                holder.ivSelectedImage.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }

        } else if (chatHelpModel.isOutgoingMsgVideo()) {
            try {
                holder.tvOutgoingMsg.setVisibility(View.GONE);
                holder.cardOutgoingImage.setVisibility(View.VISIBLE);
                holder.cardOutgoingDocument.setVisibility(View.GONE);

                Log.d(TAG, "onBindViewHolder: in else");
                holder.layoutSentStatus.setVisibility(View.VISIBLE);
                Log.d(TAG, "onBindViewHolder1: outgoing video path1 : " + chatHelpModel.getOutgoingMediaPath());
                //holder.ivSelectedImage.setImageBitmap(createVideoThumbNail(chatHelpModel.getOutgoingMediaPath()));

                Glide.with(context).load(chatHelpModel.getOutgoingMediaPath())
                        .asBitmap()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(holder.ivSelectedImage);

            } catch (Exception e) {
                Log.d(TAG, "onBindViewHolder1: " + e.getLocalizedMessage());
            }


        } else if (chatHelpModel.isOutgoingMsgDocument()) {
            whichItem = "document";
            mediaPath = chatHelpModel.getOutgoingMediaPath();
            String outgoingDocPath = chatHelpModel.getOutgoingMediaPath();
            if (outgoingDocPath != null && !outgoingDocPath.isEmpty()) {
                holder.layoutSentStatus.setVisibility(View.VISIBLE);
                holder.tvOutgoingMsg.setVisibility(View.GONE);
                holder.cardOutgoingImage.setVisibility(View.GONE);
                holder.cardOutgoingDocument.setVisibility(View.VISIBLE);


                String filename = outgoingDocPath.substring(outgoingDocPath.lastIndexOf("/") + 1);
                holder.tvOutgoingDocFileName.setText(filename);

            }
        }

        //for incoming msg
        if (chatHelpModel.isIncomingMsgText()) {
            Log.d(TAG, "onBindViewHolder: in if");

            holder.tvIncomingMsg.setVisibility(View.VISIBLE);
            holder.cardIncomingImage.setVisibility(View.GONE);
            holder.tvIncomingMsg.setText(chatHelpModel.getOutgoingMsg());
        } else if (chatHelpModel.isIncomingMsgImage() || chatHelpModel.isIncomingMsgVideo()) {
            holder.tvIncomingMsg.setVisibility(View.GONE);
            holder.cardIncomingImage.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder: in else");

            /*if (chatHelpModel.getOutgoingMediaPath() == null || chatHelpModel.getOutgoingMediaPath().equalsIgnoreCase("")) {
                if (NetworkConnection.isOnline(context)) {
                    //  profilePicDownloaded(chatHelpModel, holder);
                }
            }
            */

            if (chatHelpModel.getIncomingMediaPath() != null && !chatHelpModel.getIncomingMediaPath().isEmpty()) {
                Log.d(TAG, "onBindViewHolder:  glide");
                Glide.with(context)
                        .load(chatHelpModel.getIncomingMediaPath())
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.ivIncomingMedia);
            } else {
                holder.ivIncomingMedia.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }

        } else if (chatHelpModel.isIncomingMsgDocument()) {

        }

        holder.cardOutgoingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clickListenerInterface.performOnClick(whichItem, mediaPath);
                } catch (ClassCastException cce) {
                    cce.printStackTrace();

                }
            }
        });
        holder.cardOutgoingDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clickListenerInterface.performOnClick(whichItem, mediaPath);
                } catch (ClassCastException cce) {
                    cce.printStackTrace();

                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return chattingDetailsList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSelectedImage, ivIncomingMedia;
        CardView cardOutgoingImage, cardIncomingImage, cardOutgoingDocument;
        TextView tvOutgoingMsg, tvIncomingMsg, tvOutgoingDocFileName, tvIncomingMsgTime;
        RelativeLayout layoutSentStatus;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivSelectedImage = itemView.findViewById(R.id.iv_outgoing_image_or_video);
            cardOutgoingImage = itemView.findViewById(R.id.card_outgoing_image_or_video);
            tvOutgoingMsg = itemView.findViewById(R.id.tv_sent_msg_new);
            cardIncomingImage = itemView.findViewById(R.id.card_incoming_image_or_video);
            ivIncomingMedia = itemView.findViewById(R.id.iv_incoming_image_or_video);
            tvIncomingMsg = itemView.findViewById(R.id.tv_incoming_msg);
            layoutSentStatus = itemView.findViewById(R.id.layout_outgoing_msg_sent_status);
            cardOutgoingDocument = itemView.findViewById(R.id.card_outgoing_doc);
            tvOutgoingDocFileName = itemView.findViewById(R.id.tv_outgoing_doc_filename);
            tvIncomingMsgTime = itemView.findViewById(R.id.tv_incoming_msg_time);


        }
    }

    public void profilePicDownloaded(AppointmentInfo model, MyViewHolder holder) {
        SessionManager sessionManager = new SessionManager(context);
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getUuid());
        Logger.logD("TAG", "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD
                (url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                        downloadFilesUtils.saveToDisk(file, model.getUuid());
                        Logger.logD("TAG", file.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD("TAG", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD("TAG", "complete" + model.getPatientProfilePhoto());
                        PatientsDAO patientsDAO = new PatientsDAO();
                        boolean updated = false;
                        try {
                            updated = patientsDAO.updatePatientPhoto(model.getUuid(),
                                    AppConstants.IMAGE_PATH + model.getUuid() + ".jpg");
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        if (updated) {
                            Glide.with(context)
                                    .load(AppConstants.IMAGE_PATH + model.getUuid() + ".jpg")
                                    .thumbnail(0.3f)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(holder.ivSelectedImage);
                        }
                        ImagesDAO imagesDAO = new ImagesDAO();
                        boolean isImageDownloaded = false;
                        try {
                            isImageDownloaded = imagesDAO.insertPatientProfileImages(
                                    AppConstants.IMAGE_PATH + model.getUuid() + ".jpg", model.getUuid());
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                });
    }


 /*   public void add(ChatHelpModel chatHelpModel) {
        boolean bool = chattingDetailsList.add(chatHelpModel);
        if (bool) Log.d(TAG, "add: Item added to list");
        notifyDataSetChanged();
    }*/

    public Bitmap createVideoThumbNail(String path) {
        return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
    }

}
