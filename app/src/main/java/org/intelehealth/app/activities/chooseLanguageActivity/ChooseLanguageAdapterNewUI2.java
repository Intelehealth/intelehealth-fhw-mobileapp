package org.intelehealth.app.activities.chooseLanguageActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChooseLanguageAdapterNewUI2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<JSONObject> mItemList = new ArrayList<JSONObject>();
    int selectedPosition = -1;
    ChooseLanguageAdapterNewUI2.ItemClickListener itemClickListener;
    //ChooseLanguageActivity.ItemSelectionListener mItemSelectionListener;

  /*  public ChooseLanguageAdapterNewUI2(Context context,
                                    List<JSONObject> itemList,
                                    ChooseLanguageActivity.ItemSelectionListener
                                            itemSelectionListener, ItemClickListener itemClickListener) {
        mContext = context;
        mItemList = itemList;
        mItemSelectionListener = itemSelectionListener;
        this.itemClickListener = itemClickListener;


    }*/

    public ChooseLanguageAdapterNewUI2(Context context,
                                    List<JSONObject> itemList,
                                    ChooseLanguageAdapterNewUI2.ItemClickListener itemClickListener) {
        mContext = context;
        mItemList = itemList;
        this.itemClickListener = itemClickListener;


    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void setLabelJSON(JSONObject json) {
        mThisScreenLanguageJsonObject = json;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.language_list_item_view_ui2, parent, false);

        return new ChooseLanguageAdapterNewUI2.GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChooseLanguageAdapterNewUI2.GenericViewHolder) {
            ChooseLanguageAdapterNewUI2.GenericViewHolder genericViewHolder = (ChooseLanguageAdapterNewUI2.GenericViewHolder) holder;
            genericViewHolder.index = position;
            genericViewHolder.jsonObject = mItemList.get(position);

            try {
                genericViewHolder.rbChooseLanguage.setText(genericViewHolder.jsonObject.getString("name"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            genericViewHolder.rbChooseLanguage.setChecked(position == selectedPosition);

            ((ChooseLanguageAdapterNewUI2.GenericViewHolder) holder).rbChooseLanguage.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(
                                CompoundButton compoundButton,
                                boolean b) {
                            if (b) {
                                selectedPosition
                                        = holder.getBindingAdapterPosition();
                                itemClickListener.onClick(
                                        ((ChooseLanguageAdapterNewUI2.GenericViewHolder) holder).rbChooseLanguage.getText()
                                                .toString(), holder.getBindingAdapterPosition());
                            }
                        }
                    });

            genericViewHolder.rbChooseLanguage.setChecked(position == selectedPosition);


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
        //TextView nameTextView;
        // ImageView statusImageView;
        RadioButton rbChooseLanguage;
        JSONObject jsonObject;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            rbChooseLanguage = itemView.findViewById(R.id.rb_choose_language);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = getBindingAdapterPosition();
                    notifyDataSetChanged();
                }
            });
            /* nameTextView = itemView.findViewById(R.id.text_tv);
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
            });*/

        }


    }

    public interface ItemClickListener {
        void onClick(String s, int position);
    }
}

