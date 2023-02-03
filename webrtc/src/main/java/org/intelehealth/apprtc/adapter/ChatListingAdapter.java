package org.intelehealth.apprtc.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.intelehealth.apprtc.R;
import org.intelehealth.apprtc.data.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private List<JSONObject> mItemList = new ArrayList<JSONObject>();


    public ChatListingAdapter(Context context, List<JSONObject> itemList) {
        mContext = context;
        mItemList = itemList;

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
        try {
            return mItemList.get(position).getInt("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LeftViewHolder) {
            LeftViewHolder leftViewHolder = (LeftViewHolder) holder;
            leftViewHolder.jsonObject = mItemList.get(position);

            try {
                leftViewHolder.messageTextView.setText(leftViewHolder.jsonObject.getString("message"));
                String rawTime = leftViewHolder.jsonObject.getString("createdAt"); // 2021-04-16T06:36:35.000Z
                String displayDateTime = parseDate(rawTime);
                leftViewHolder.timeTextView.setText(displayDateTime);
                Log.v("CHAT", "LEFT - "+leftViewHolder.jsonObject.toString());

                if (leftViewHolder.jsonObject.has("ContentType") && leftViewHolder.jsonObject.getString("ContentType").equalsIgnoreCase("IMAGE")) {
                    Glide.with(mContext)
                            .load(leftViewHolder.jsonObject.getString("filePath"))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .thumbnail(0.1f)
                            .into(leftViewHolder.imageView);
                    leftViewHolder.imageView.setVisibility(View.VISIBLE);
                    leftViewHolder.messageTextView.setVisibility(View.GONE);
                } else {
                    leftViewHolder.imageView.setVisibility(View.GONE);
                    leftViewHolder.messageTextView.setVisibility(View.VISIBLE);
                }


            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

        } else if (holder instanceof RightViewHolder) {
            RightViewHolder rightViewHolder = (RightViewHolder) holder;
            rightViewHolder.jsonObject = mItemList.get(position);

            try {
                rightViewHolder.messageTextView.setText(rightViewHolder.jsonObject.getString("message"));
                String rawTime = rightViewHolder.jsonObject.getString("createdAt"); // 2021-04-16T06:36:35.000Z

                String displayDateTime = parseDate(rawTime);
                rightViewHolder.timeTextView.setText(displayDateTime);
                Log.v("CHAT", "RIGHT - "+rightViewHolder.jsonObject.toString());
                if (rightViewHolder.jsonObject.has("ContentType") && rightViewHolder.jsonObject.getString("ContentType").equalsIgnoreCase("IMAGE")) {
                    Glide.with(mContext)
                            .load(rightViewHolder.jsonObject.getString("filePath"))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .thumbnail(0.1f)
                            .into(rightViewHolder.imageView);
                    rightViewHolder.imageView.setVisibility(View.VISIBLE);
                    rightViewHolder.messageTextView.setVisibility(View.GONE);
                } else {
                    rightViewHolder.imageView.setVisibility(View.GONE);
                    rightViewHolder.messageTextView.setVisibility(View.VISIBLE);
                }

                if (rightViewHolder.jsonObject.has("isRead") && rightViewHolder.jsonObject.getInt("isRead") ==1) {
                    rightViewHolder.statusTextView.setText("Read");
                    rightViewHolder.statusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.read_done_status_icon, 0,0,0);
                } else {
                    rightViewHolder.statusTextView.setText("Sent");
                    rightViewHolder.statusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.read_status_icon, 0,0,0);
                }


            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private String parseDate(String rawTime) throws ParseException {
        //SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        //Fri, 16 Apr 2021 06:37:30 GM
        SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        //rawSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = rawSimpleDateFormat.parse(rawTime);
        //Log.v("date", date.toString());

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

    public void refresh(List<JSONObject> chatList) {
        mItemList = chatList;
        notifyDataSetChanged();
    }

    private class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timeTextView;
        ImageView imageView;
        JSONObject jsonObject;

        LeftViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_iv);
            messageTextView = itemView.findViewById(R.id.text_tv);
            timeTextView = itemView.findViewById(R.id.time_tv);
        }
    }

    private class RightViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timeTextView, statusTextView;
        ImageView imageView;
        JSONObject jsonObject;

        RightViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_iv);
            messageTextView = itemView.findViewById(R.id.text_tv);
            timeTextView = itemView.findViewById(R.id.time_tv);
            statusTextView = itemView.findViewById(R.id.status_tv);
        }
    }


}

