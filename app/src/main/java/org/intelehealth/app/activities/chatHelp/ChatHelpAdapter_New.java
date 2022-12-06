package org.intelehealth.app.activities.chatHelp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;

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

    public ChatHelpAdapter_New(Context context) {
        this.context = context;

    }

    public ChatHelpAdapter_New(Context context, List<ChatHelpModel> chattingDetailsList) {
        this.context = context;
        this.chattingDetailsList = chattingDetailsList;

    }

    @Override
    public ChatHelpAdapter_New.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_chat_text_ui2, parent, false);
        ChatHelpAdapter_New.MyViewHolder myViewHolder = new ChatHelpAdapter_New.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(ChatHelpAdapter_New.MyViewHolder holder, int position) {
        ChatHelpModel chatHelpModel = chattingDetailsList.get(position);
        Log.d(TAG, "onBindViewHolder: image path : " + chatHelpModel.getOutgoingMediaPath());
        Log.d(TAG, "onBindViewHolder: isimage : "+chatHelpModel.isOutgoingMsgImage());
        Log.d(TAG, "onBindViewHolder: istext : "+chatHelpModel.isOutgoingMsgText());

        if (chatHelpModel.isOutgoingMsgText()) {
            Log.d(TAG, "onBindViewHolder: in if");

            holder.tvOutgoingMsg.setVisibility(View.VISIBLE);
            holder.cardOutgoingImage.setVisibility(View.GONE);
            holder.tvOutgoingMsg.setText(chatHelpModel.getOutgoingMsg());
        } else if (chatHelpModel.isOutgoingMsgImage() || chatHelpModel.isOutgoingMsgVideo()) {
            holder.tvOutgoingMsg.setVisibility(View.GONE);
            holder.cardOutgoingImage.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder: in else");

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

        } else if (chatHelpModel.isOutgoingMsgDocument()) {

        }

        //for incoming msg
        if (chatHelpModel.isIncomingMsgText()) {
            Log.d(TAG, "onBindViewHolder: in if");

            holder.tvIncomingMsg.setVisibility(View.VISIBLE);
            holder.cardIncomingImage.setVisibility(View.GONE);
            holder.tvIncomingMsg.setText(chatHelpModel.getOutgoingMsg());
        } else if (chatHelpModel.isOutgoingMsgImage() || chatHelpModel.isOutgoingMsgVideo()) {
            holder.tvIncomingMsg.setVisibility(View.GONE);
            holder.cardIncomingImage.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder: in else");

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
                        .into(holder.ivIncomingMedia);
            } else {
                holder.ivIncomingMedia.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }

        } else if (chatHelpModel.isOutgoingMsgDocument()) {

        }

    }

    @Override
    public int getItemCount() {
        return chattingDetailsList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSelectedImage,ivIncomingMedia;
        CardView cardOutgoingImage,cardIncomingImage;
        TextView tvOutgoingMsg,tvIncomingMsg;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivSelectedImage = itemView.findViewById(R.id.iv_outgoing_image_or_video);
            cardOutgoingImage = itemView.findViewById(R.id.card_outgoing_image_or_video);
            tvOutgoingMsg = itemView.findViewById(R.id.tv_sent_msg_new);
            cardIncomingImage = itemView.findViewById(R.id.card_incoming_image_or_video);
            ivIncomingMedia = itemView.findViewById(R.id.iv_incoming_image_or_video);
            tvIncomingMsg = itemView.findViewById(R.id.tv_incoming_msg);


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
}
