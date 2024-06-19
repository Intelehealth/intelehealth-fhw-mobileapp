package org.intelehealth.app.abdm.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.model.CheckBoxRecyclerModel;

import java.util.List;

/**
 * Created by - Prajwal W. on 19/06/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class CheckboxAdapter extends RecyclerView.Adapter<CheckboxAdapter.MyViewHolder> {
    private List<CheckBoxRecyclerModel> modelList;
    private Context context;
    private CheckboxAdapter.OnCheckboxChecked onCheckboxChecked;

    public CheckboxAdapter(Context context, List<CheckBoxRecyclerModel> modelList, CheckboxAdapter.OnCheckboxChecked onCheckboxChecked) {
        this.context = context;
        this.modelList = modelList;
        this.onCheckboxChecked = onCheckboxChecked;
    }

    @NonNull
    @Override
    public CheckboxAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_checkbox_recycler, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckboxAdapter.MyViewHolder holder, int position) {
        CheckBoxRecyclerModel model = modelList.get(position);
        holder.chkBox.setText(Html.fromHtml(model.getCheckboxText()));
        holder.chkBox.setChecked(model.isChecked());

        holder.chkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setChecked(isChecked);
                onCheckboxChecked.onOptionChecked(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public boolean areAllItemsChecked() {
        for (CheckBoxRecyclerModel model : modelList) {
            if (!model.isChecked()) {
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
