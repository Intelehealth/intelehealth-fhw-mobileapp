package org.intelehealth.app.activities.chooseLanguageActivity;

import android.content.Context;
import android.util.Log;
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
                                        = holder.getAdapterPosition();
                                itemClickListener.onClick(
                                        ((ChooseLanguageAdapterNewUI2.GenericViewHolder) holder).rbChooseLanguage.getText()
                                                .toString(), holder.getAdapterPosition());
                            }
                        }
                    });

            genericViewHolder.rbChooseLanguage.setChecked(position == selectedPosition);
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

        GenericViewHolder(View itemView) {
            super(itemView);
            rbChooseLanguage = itemView.findViewById(R.id.rbChooseLanguageItem);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = getAdapterPosition();
                    v.setContentDescription(rbChooseLanguage.getText());
                    notifyDataSetChanged();
                }
            });
        }
    }

    public interface ItemClickListener {
        void onClick(String s, int position);
    }
}

