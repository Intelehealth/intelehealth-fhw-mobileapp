package org.intelehealth.app.activities.chatHelp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.app.R;

import java.util.List;

public class ChatHelpAdapter_New extends RecyclerView.Adapter<ChatHelpAdapter_New.MyViewHolder> {
    Context context;
    List<ChatHelpModel> chattingDetailsList;

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
        if (chatHelpModel.isOutgoingMsgText()) {
            holder.tvOutgoingMsg.setVisibility(View.VISIBLE);
            holder.cardOutgoingImage.setVisibility(View.GONE);
            holder.tvOutgoingMsg.setText(chatHelpModel.getOutgoingMsg());
        } else if (chatHelpModel.isOutgoingMsgImage() || chatHelpModel.isOutgoingMsgVideo()) {
            holder.tvOutgoingMsg.setVisibility(View.GONE);
            holder.cardOutgoingImage.setVisibility(View.VISIBLE);

           /* if (chatHelpModel.get() != null && !chatHelpModel.getPatientProfilePhoto().isEmpty()) {
                Glide.with(context)
                        .load(appointmentInfoModel.getPatientProfilePhoto())
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.ivProfileImage);
            } else {
                holder.ivProfileImage.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }*/
        } else if (chatHelpModel.isOutgoingMsgDocument()) {

        }


    }

    @Override
    public int getItemCount() {
        return chattingDetailsList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSelectedImage;
        CardView cardOutgoingImage;
        TextView tvOutgoingMsg;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivSelectedImage = itemView.findViewById(R.id.iv_outgoing_image_or_video);
            cardOutgoingImage = itemView.findViewById(R.id.card_outgoing_image_or_video);
            tvOutgoingMsg = itemView.findViewById(R.id.tv_sent_msg_new);


        }
    }


}
