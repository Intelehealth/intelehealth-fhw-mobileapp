package org.intelehealth.apprtc.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.apprtc.R;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Vaghela Mithun R. on 30-05-2023 - 13:07.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public abstract class ChatDateHeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int CHAT_DATE_HEADER = 1000;
    protected List<JSONObject> mItemList = new ArrayList<>();
    protected Context mContext;

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public ChatDateHeaderAdapter(Context context, List<JSONObject> itemList) {
        this.mItemList = itemList;
        this.mContext = context;
    }

    protected String parseDate(String rawTime) throws ParseException {
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
        return temp1.split(",")[1].equals(temp2.split(",")[1]) ? temp1.split(",")[0] : temp1;
    }
}

class ChatDateHeaderViewHolder extends RecyclerView.ViewHolder {
    private TextView tvChatDate;

    public ChatDateHeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        tvChatDate = itemView.findViewById(R.id.tvChatDate);
    }

    public void bind(String date) {
        tvChatDate.setText(date);
    }
}