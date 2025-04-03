package org.intelehealth.klivekit.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.klivekit.R;
import org.intelehealth.klivekit.chat.model.ItemHeader;
import org.intelehealth.klivekit.chat.model.MessageStatus;
import org.intelehealth.klivekit.model.ChatMessage;
import org.intelehealth.klivekit.utils.Constants;

import java.util.List;

public class ChatListingAdapter extends DateHeaderAdapter {
// Medicine adapter <- Test <- Aid adapter <- Header adapter <- Recycler.Adapter
    public interface AttachmentClickListener {
        void onClick(String url);
    }

    private final AttachmentClickListener mAttachmentClickListener;

    public ChatListingAdapter(Context context, List<ItemHeader> itemList, AttachmentClickListener clickListener) {
        super(context, itemList);
        mAttachmentClickListener = clickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == Constants.LEFT_ITEM_DOCT) {
            View itemView = inflater.inflate(R.layout.left_chat_layout, parent, false);
            return new LeftViewHolder(itemView);
        } else if (viewType == Constants.RIGHT_ITEM_HW) {
            View itemView = inflater.inflate(R.layout.right_chat_layout, parent, false);
            return new RightViewHolder(itemView);
        } else return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        if (mItemList.get(position).isHeader()) return DATE_HEADER;
        else if (mItemList.get(position) instanceof ChatMessage) {
            ChatMessage message = (ChatMessage) mItemList.get(position);
            return message.getLayoutType();
        } else return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mItemList.get(position) instanceof ChatMessage) {
            ChatMessage message = (ChatMessage) mItemList.get(position);
            if (holder instanceof LeftViewHolder && message.getLayoutType() == Constants.LEFT_ITEM_DOCT) {
                LeftViewHolder leftViewHolder = (LeftViewHolder) holder;
                leftViewHolder.bind(message);
            } else if (holder instanceof RightViewHolder && message.getLayoutType() == Constants.RIGHT_ITEM_HW) {
                RightViewHolder rightViewHolder = (RightViewHolder) holder;
                rightViewHolder.bind(message);
            }
        } else super.onBindViewHolder(holder, position);
    }

//    private String parseDate(String rawTime) {
//        //SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        //Fri, 16 Apr 2021 06:37:30 GM
//        Date date = DateTimeUtils.parseDate(rawTime, DateTimeUtils.DB_FORMAT, null);
//        SimpleDateFormat sdf = DateTimeUtils.getSimpleDateFormat(DateTimeUtils.MESSAGE_TIME_FORMAT, DateTimeUtils.TIME_ZONE_ISD);
//        Date todayDate = new Date();
//        String temp1 = sdf.format(date);
//        String temp2 = sdf.format(todayDate);
//        return temp1.split(",")[1].equals(temp2.split(",")[1]) ? temp1.split(",")[0] : temp1;
//    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(List<ItemHeader> chatList) {
        mItemList = chatList;
        notifyDataSetChanged();
    }

    public List<ItemHeader> getList() {
        return mItemList;
    }

    public void addMessage(ItemHeader message) {
        mItemList.add(0, message);
        notifyDataSetChanged();
//        notifyItemChanged(0);
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
            timeTextView.setText(message.getMessageTime());

            if (message.getType() != null && message.getType().equalsIgnoreCase("attachment")) {
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
            timeTextView.setText(message.getMessageTime());
            if (message.getType() != null && message.getType().equalsIgnoreCase("attachment")) {
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

            MessageStatus status = MessageStatus.getStatus(message.getMessageStatus());
            if (getAdapterPosition() == 0) {
                Log.e("ChatAdapter", "bind: status" + status.getValue());
                statusTextView.setVisibility(View.VISIBLE);
                if (status.isRead()) {
                    statusTextView.setText(mContext.getString(R.string.read));
                    statusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_msg_read, 0, 0, 0);
                } else if (status.isDelivered()) {
                    statusTextView.setText(mContext.getString(R.string.msg_delivered));
                    statusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_msg_delivered, 0, 0, 0);
                } else if (status.isSent()) {
                    statusTextView.setText(mContext.getString(R.string.sent));
                    statusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_msg_sent, 0, 0, 0);
                } else {
                    statusTextView.setText(mContext.getString(R.string.sending));
                    statusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_msg_sending, 0, 0, 0);
                }
            } else statusTextView.setVisibility(View.GONE);
        }
    }

    public void markMessageAsRead(int id) {
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i) instanceof ChatMessage) {
                ChatMessage chatMessage = (ChatMessage) mItemList.get(i);
//                if (id == chatMessage.getId()) {
                chatMessage.setIsRead(true);
                chatMessage.setMessageStatus(MessageStatus.READ.getValue());
                notifyItemChanged(i);
//                    break;
//                }
            }
        }
    }

    public void markMessageAsDelivered(int id) {
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i) instanceof ChatMessage) {
                ChatMessage chatMessage = (ChatMessage) mItemList.get(i);
                if (id == chatMessage.getId()) {
                    chatMessage.setIsRead(false);
                    chatMessage.setMessageStatus(MessageStatus.DELIVERED.getValue());
                    Log.e("ChatAdapter", "markMessageAsDelivered: " + chatMessage.getMessage());
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public void updatedMessage(ChatMessage message) {
        for (int i = 0; i < mItemList.size(); i++) {
            if (mItemList.get(i) instanceof ChatMessage) {
                ChatMessage chatMessage = (ChatMessage) mItemList.get(i);
                if (message.getMessage().equals(chatMessage.getMessage())) {
                    chatMessage.setId(message.getId());
                    chatMessage.setMessageStatus(message.getMessageStatus());
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }
}

