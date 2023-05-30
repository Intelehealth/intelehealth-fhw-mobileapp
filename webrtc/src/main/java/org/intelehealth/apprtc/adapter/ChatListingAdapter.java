package org.intelehealth.apprtc.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


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
        if (viewType == Constants.LEFT_ITEM) {
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
                setDateVisibility(position, rawTime, leftViewHolder.timeTextView);
//                String displayDateTime = parseDate(rawTime);
//                leftViewHolder.timeTextView.setText(displayDateTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (holder instanceof RightViewHolder) {
            RightViewHolder rightViewHolder = (RightViewHolder) holder;
            rightViewHolder.jsonObject = mItemList.get(position);

            try {
                rightViewHolder.messageTextView.setText(rightViewHolder.jsonObject.getString("message"));
                String rawTime = rightViewHolder.jsonObject.getString("createdAt"); // 2021-04-16T06:36:35.000Z
                setDateVisibility(position, rawTime, rightViewHolder.timeTextView);
//                String displayDateTime = parseDate(rawTime);
//                rightViewHolder.timeTextView.setText(displayDateTime);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setDateVisibility(int position, String date, TextView textView) {
        try {
            String displayDateTime = parseDate(date);
            textView.setVisibility(View.GONE);
            if (position == 0) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(displayDateTime);
            } else if (position > 0) {
                JSONObject object = mItemList.get(position - 1);
                String prevDate = parseDate(object.getString("createdAt"));
                if (!displayDateTime.equalsIgnoreCase(prevDate)) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(displayDateTime);
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String parseDate(String rawTime) throws ParseException {
        //SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        //Fri, 16 Apr 2021 06:37:30 GM
        SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        //rawSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = rawSimpleDateFormat.parse(rawTime);
        //Log.v("date", date.toString());

        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM 'at' h:mm a");
        displayFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        Date todayDate = new Date();
        String temp1 = displayFormat.format(date);
        String temp2 = displayFormat.format(todayDate);
        return temp1; //temp1.split(",")[1].equals(temp2.split(",")[1]) ? temp1.split(",")[0] : temp1;
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
        JSONObject jsonObject;

        LeftViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.text_tv);
            timeTextView = itemView.findViewById(R.id.time_tv);
        }
    }

    private class RightViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timeTextView;
        JSONObject jsonObject;

        RightViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.text_tv);
            timeTextView = itemView.findViewById(R.id.time_tv);
        }
    }


}

