package org.intelehealth.abdm.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.abdm.R;

import com.google.android.material.checkbox.MaterialCheckBox;

import org.intelehealth.abdm.model.CheckBoxRecyclerModel;

import java.util.List;

/**
 * Created by - Prajwal W. on 19/06/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class CheckboxAdapter extends RecyclerView.Adapter<CheckboxAdapter.MyViewHolder> {
    private List<CheckBoxRecyclerModel> modelList;
    private Context context;
    private OnCheckboxChecked onCheckboxChecked;
    public static final int LEFT_MARGIN = 50;

    public CheckboxAdapter(Context context, List<CheckBoxRecyclerModel> modelList, OnCheckboxChecked onCheckboxChecked) {
        this.context = context;
        this.modelList = modelList;
        this.onCheckboxChecked = onCheckboxChecked;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_consent_checkbox, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CheckBoxRecyclerModel model = modelList.get(position);
        holder.chkBox.setText(Html.fromHtml(model.getCheckboxText()));
        holder.chkBox.setChecked(model.isChecked());
        holder.chkBox.setEnabled(model.isCheckboxEnabled());

        if (position == (modelList.size() - 1) || position == (modelList.size() - 2)) {
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(LEFT_MARGIN, 0, 0, 0);
            holder.chkBox.setLayoutParams(layoutParams);
        }

        if (model.isCheckboxEnabled()) {
            holder.chkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    model.setChecked(isChecked);
                    onCheckboxChecked.onOptionChecked(model);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public boolean areAllItemsChecked() {
        for (CheckBoxRecyclerModel model : modelList) {
            if (model.isCheckboxEnabled() && !model.isChecked()) {
                return false;
            }
        }
        return true;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private MaterialCheckBox chkBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            chkBox = itemView.findViewById(R.id.chkBox);
        }
    }

    public interface OnCheckboxChecked {
        public void onOptionChecked(CheckBoxRecyclerModel model);
    }
}
