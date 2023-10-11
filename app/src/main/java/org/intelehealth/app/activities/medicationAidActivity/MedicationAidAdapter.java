package org.intelehealth.app.activities.medicationAidActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.prescription.AdvicePrescAdapter;
import org.intelehealth.app.models.MedicationAidModel;

import java.util.ArrayList;
import java.util.List;

public class MedicationAidAdapter extends RecyclerView.Adapter<MedicationAidAdapter.MyViewHolder> {
    private Context context;
    private List<MedicationAidModel> list, checkedList;
    private OnSelectedItems onSelectedItems;

    public MedicationAidAdapter(Context context, List<MedicationAidModel> list/*, OnSelectedItems onSelectedItems*/) {
        this.context = context;
        this.list = list;
        checkedList = new ArrayList<>();
      //  this.onSelectedItems = onSelectedItems;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication_aid, 
                parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MedicationAidModel model = list.get(position);
        holder.tvMedAidName.setText(model.getValue());

       /* if (model.contains("Type")) {
            holder.cb_administer.setVisibility(View.GONE);
        }
        else
            holder.cb_administer.setVisibility(View.VISIBLE);*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMedAidName;
        private MaterialCheckBox cb_value/*, cb_administer*/;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMedAidName = itemView.findViewById(R.id.tvMedAidName);
            cb_value = itemView.findViewById(R.id.cb_value);
          //  cb_administer = itemView.findViewById(R.id.cb_administer);

            cb_value.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (compoundButton.isChecked()) {
                        list.get(getAdapterPosition()).setChecked(b);
                        checkedList.add(list.get(getAdapterPosition()));
                    }
                    else {
                        list.get(getAdapterPosition()).setChecked(b);
                        checkedList.remove(list.get(getAdapterPosition()));
                    }

                }
            });
        }
    }

    public List<MedicationAidModel> getFinalList() {
        return checkedList;
    }

    public interface OnSelectedItems {

        public void getCheckedValues(ArrayList<MedicationAidModel> model);


      /*  public void getCheckedMedData();
        public void getCheckedAidData();*/
    }
}
