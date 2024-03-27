package org.intelehealth.app.activities.chooseLanguageActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ChooseLanguageAdapterNew extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<JSONObject> mItemList;
    int selectedPosition1 = -1;
    SplashScreenActivity.ItemSelectionListener mItemSelectionListener;

    public ChooseLanguageAdapterNew(Context context,
                                    List<JSONObject> itemList,
                                    SplashScreenActivity.ItemSelectionListener itemSelectionListener
                                          ) {
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
                .inflate(R.layout.language_list_item_view_ui2, parent, false);

        return new ChooseLanguageAdapterNew.GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChooseLanguageAdapterNew.GenericViewHolder) {
            ChooseLanguageAdapterNew.GenericViewHolder genericViewHolder = (ChooseLanguageAdapterNew.GenericViewHolder) holder;
            genericViewHolder.index = position;
            genericViewHolder.jsonObject = mItemList.get(position);

            try {
                genericViewHolder.rbChooseLanguage.setText(genericViewHolder.jsonObject.getString("name"));

                if (genericViewHolder.jsonObject.getBoolean("selected")) {
                    genericViewHolder.layoutRb.setBackgroundColor(ContextCompat.getColor(mContext,R.color.cardTintLightGreen));
                    genericViewHolder.rbChooseLanguage.setButtonDrawable(ContextCompat.getDrawable(mContext,R.drawable.ui2_ic_selected_green));

                } else {
                    genericViewHolder.layoutRb.setBackgroundColor(ContextCompat.getColor(mContext,R.color.white));
                    genericViewHolder.rbChooseLanguage.setButtonDrawable(ContextCompat.getDrawable(mContext,R.drawable.ui2_ic_circle));

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            /*
            old code
            try {
                genericViewHolder.nameTextView.setText(genericViewHolder.jsonObject.getString("name"));

                if (genericViewHolder.jsonObject.getBoolean("selected")) {
                    genericViewHolder.statusImageView.setVisibility(View.VISIBLE);
                    genericViewHolder.nameTextView.setTextColor(mContext.getResources().getColor(R.color.gray_6));
                    genericViewHolder.nameTextView.setTypeface(genericViewHolder.nameTextView.getTypeface(), Typeface.BOLD);
                    genericViewHolder.nameTextView.setBackgroundResource(R.drawable.round_corner_gray_light);
                } else {
                    genericViewHolder.statusImageView.setVisibility(View.INVISIBLE);
                    genericViewHolder.nameTextView.setTextColor(mContext.getResources().getColor(R.color.gray_4));
                    genericViewHolder.nameTextView.setTypeface(genericViewHolder.nameTextView.getTypeface(), Typeface.NORMAL);
                    genericViewHolder.nameTextView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbChooseLanguage;
        JSONObject jsonObject;
        int index;
        LinearLayout layoutRb;

        GenericViewHolder(View itemView) {
            super(itemView);
            rbChooseLanguage = itemView.findViewById(R.id.rb_choose_language);
            layoutRb = itemView.findViewById(R.id.layout_rb_choose_language);


            rbChooseLanguage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        for (int i = 0; i < mItemList.size(); i++) {
                            if (i == index) {
                                mItemList.get(i).put("selected", true);
                            } else {
                                mItemList.get(i).put("selected", false);
                            }
                        }
                       int  selectedPosition = getBindingAdapterPosition();
                        String name = mItemList.get(selectedPosition).getString("name");
                    //    Toast.makeText(mContext, "Selected language : " + name, Toast.LENGTH_SHORT).show();
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

