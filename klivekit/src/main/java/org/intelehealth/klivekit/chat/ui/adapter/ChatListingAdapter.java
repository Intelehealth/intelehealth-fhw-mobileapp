package org.intelehealth.klivekit.chat.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.klivekit.R;
import org.intelehealth.klivekit.model.ChatMessage;
import org.intelehealth.klivekit.utils.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private List<ChatMessage> mItemList;

    public interface AttachmentClickListener {
        void onClick(String url);
    }

    private AttachmentClickListener mAttachmentClickListener;

    public ChatListingAdapter(Context context, List<ChatMessage> itemList, AttachmentClickListener clickListener) {
        mContext = context;
        mItemList = itemList;
        mAttachmentClickListener = clickListener;
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == Constants.LEFT_ITEM_DOCT) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_chat_layout, parent, false);

            return new LeftViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_chat_layout, parent, false);

            return new RightViewHolder(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position).getLayoutType();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LeftViewHolder) {
            LeftViewHolder leftViewHolder = (LeftViewHolder) holder;
            leftViewHolder.bind(mItemList.get(position));
        } else if (holder instanceof RightViewHolder) {
            RightViewHolder rightViewHolder = (RightViewHolder) holder;
            rightViewHolder.bind(mItemList.get(position));
        }
    }

    private String parseDate(String rawTime) {
        //SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        //Fri, 16 Apr 2021 06:37:30 GM
        SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        //rawSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = rawSimpleDateFormat.parse(rawTime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        //Log.v("date", date.toString());
        if (date == null) date = Calendar.getInstance().getTime();

        SimpleDateFormat displayFormat = new SimpleDateFormat("h:mm a, MMM d");
        displayFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        Date todayDate = new Date();
        String temp1 = displayFormat.format(date);
        String temp2 = displayFormat.format(todayDate);
        return temp1.split(",")[1].equals(temp2.split(",")[1]) ? temp1.split(",")[0] : temp1;
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public void refresh(List<ChatMessage> chatList) {
        mItemList = chatList;
        notifyDataSetChanged();
    }

    public List<ChatMessage> getList() {
        return mItemList;
    }

    public void addMessage(ChatMessage message) {
        mItemList.add(message);
        notifyItemChanged(mItemList.size());
    }

    private class LeftViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView, timeTextView;
        private RelativeLayout loaderRelativeLayout;
        private ImageView imageView;

        public LeftViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_iv);
            messageTextView = itemView.findViewById(R.id.text_tv);
            timeTextView = itemView.findViewById(R.id.time_tv);
            loaderRelativeLayout = itemView.findViewById(R.id.rl_loader);
        }

        public void bind(ChatMessage message) {
            imageView.setOnClickListener(view -> {
                mAttachmentClickListener.onClick(message.getMessage());
            });

            messageTextView.setText(message.getMessage());
            String displayDateTime = parseDate(message.getCreatedAt());
            timeTextView.setText(displayDateTime);
            Log.v("CHAT", "LEFT - ");

            if (message.getType() != null && message.getType().equalsIgnoreCase("attachment")) {
                    /*Glide.with(mContext)
                            .load(leftViewHolder.jsonObject.getString("message"))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .thumbnail(0.1f)
                            .into(leftViewHolder.imageView);*/
                if (message.getMessage().endsWith(".pdf")) {
                    imageView.setImageResource(R.drawable.pdf_icon);
                } else {
                    imageView.setImageResource(R.drawable.img_icon);
                }
                if (message.isLoading()) {
                    loaderRelativeLayout.setVisibility(View.VISIBLE);
                } else {
                    loaderRelativeLayout.setVisibility(View.GONE);
                }
                imageView.setVisibility(View.VISIBLE);
                messageTextView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.GONE);
                messageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    private class RightViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView, timeTextView, statusTextView;
        private RelativeLayout loaderRelativeLayout;
        private ImageView imageView;

        public RightViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_iv);
            messageTextView = itemView.findViewById(R.id.text_tv);
            timeTextView = itemView.findViewById(R.id.time_tv);
            statusTextView = itemView.findViewById(R.id.status_tv);
            loaderRelativeLayout = itemView.findViewById(R.id.rl_loader);
        }

        public void bind(ChatMessage message) {
            imageView.setOnClickListener(view -> mAttachmentClickListener.onClick(message.getMessage()));
            messageTextView.setText(message.getMessage());
            String displayDateTime = parseDate(message.getCreatedAt());
            timeTextView.setText(displayDateTime);
            Log.v("CHAT", "RIGHT - ");
            if (message.getType() != null && message.getType().equalsIgnoreCase("attachment")) {
                   /* Glide.with(mContext)
                            .load(rightViewHolder.jsonObject.getString("message"))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .thumbnail(0.1f)
                            .into(rightViewHolder.imageView);*/
                if (message.getType().endsWith(".pdf")) {
                    imageView.setImageResource(R.drawable.pdf_icon);
                } else {
                    imageView.setImageResource(R.drawable.img_icon);
                }
                if (message.isLoading()) {
                    loaderRelativeLayout.setVisibility(View.VISIBLE);
                } else {
                    loaderRelativeLayout.setVisibility(View.GONE);
                }
                imageView.setVisibility(View.VISIBLE);
                messageTextView.setVisibility(View.GONE);
            } else {
                imageView.setVisibility(View.GONE);
                messageTextView.setVisibility(View.VISIBLE);
            }

            if (message.getIsRead() == 1) {
                statusTextView.setText(mContext.getString(R.string.read));
                statusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.read_done_status_icon, 0, 0, 0);
            } else {
                statusTextView.setText(mContext.getString(R.string.sent));
                statusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.read_status_icon, 0, 0, 0);
            }
        }
    }
}

