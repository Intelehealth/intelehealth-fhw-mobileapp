package org.intelehealth.app.activities.chooseLanguageActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import org.intelehealth.app.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LanguageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<JSONObject> mItemList = new ArrayList<JSONObject>();
    ChooseLanguageActivity.ItemSelectionListener mItemSelectionListener;

    public LanguageListAdapter(Context context, List<JSONObject> itemList, ChooseLanguageActivity.ItemSelectionListener itemSelectionListener) {
        mContext = context;
        mItemList = itemList;
        mItemSelectionListener = itemSelectionListener;
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void setLabelJSON(JSONObject json) {
        mThisScreenLanguageJsonObject = json;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.language_list_item_view, parent, false);

        return new GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.index = position;
            genericViewHolder.jsonObject = mItemList.get(position);
            try {
                genericViewHolder.nameTextView.setText(genericViewHolder.jsonObject.getString("name"));

                if (genericViewHolder.jsonObject.getBoolean("selected")) {
                    genericViewHolder.statusImageView.setVisibility(View.VISIBLE);
                    genericViewHolder.nameTextView.setTextColor(mContext.getResources().getColor(R.color.gray_6));
                    genericViewHolder.nameTextView.setTypeface(genericViewHolder.nameTextView.getTypeface(), Typeface.BOLD);
                    genericViewHolder.rlRoot.setBackgroundResource(R.drawable.round_corner_gray_light);
                } else {
                    genericViewHolder.statusImageView.setVisibility(View.INVISIBLE);
                    genericViewHolder.nameTextView.setTextColor(mContext.getResources().getColor(org.intelehealth.apprtc.R.color.gray_4));
                    genericViewHolder.nameTextView.setTypeface(genericViewHolder.nameTextView.getTypeface(), Typeface.NORMAL);
                    genericViewHolder.rlRoot.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView statusImageView;
        JSONObject jsonObject;
        RelativeLayout rlRoot;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            rlRoot = itemView.findViewById(R.id.rlRoot);
            nameTextView = itemView.findViewById(R.id.text_tv);
            statusImageView = itemView.findViewById(R.id.status_imv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        for (int i = 0; i < mItemList.size(); i++) {
                            if (i == index) {
                                mItemList.get(i).put("selected", true);
                            } else {
                                mItemList.get(i).put("selected", false);
                            }
                        }
                        mItemSelectionListener.onSelect(jsonObject, index);
                        notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }


    }


}

