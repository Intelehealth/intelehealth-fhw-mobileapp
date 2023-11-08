package org.intelehealth.app.activities.medicationAidActivity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.models.PatientAttributeLanguageModel;
import org.intelehealth.app.models.dispenseAdministerModel.AidModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationAidModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationModel;
import org.intelehealth.app.utilities.LocaleHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MedicationAidAdapter extends RecyclerView.Adapter<MedicationAidAdapter.MyViewHolder> {
    private Context context;
    private List<MedicationAidModel> list, checkedList;
    private HashSet<MedicationAidModel> hash_checkedList = new HashSet<>();
    private boolean isArabic;
  //  private List<MedicationAidModel> update_medUuidList, update_aidUuidList, updateUUID_List;
    private List<MedicationAidModel> updateUUID_List;
  //  private List<AidModel> update_aidUuidList;
    private String tag;

    public MedicationAidAdapter(Context context, List<MedicationAidModel> list, boolean isArabic, List<MedicationAidModel> updateUUID_List, String tag) {
        this.context = context;
        this.list = list;
        this.isArabic = isArabic;
        this.updateUUID_List = updateUUID_List;
        this.tag = tag;

        checkedList = new ArrayList<>();
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

        PatientAttributeLanguageModel patientAttributeLanguageModel = getPatientAttributeFromJSON(model.getValue());
        String value = "";
        if (isArabic)
            value = patientAttributeLanguageModel.getAr().replaceAll("\n", "");
        else
            value = patientAttributeLanguageModel.getEn().replaceAll("\n", "");

        Log.d("TAG", "medication adapter: " + value + "\n");
        holder.tvMedAidName.setText(value);

        // med - start
        if (updateUUID_List != null && updateUUID_List.size() > 0) {
            for (int i = 0; i < updateUUID_List.size(); i++) {
                MedicationModel medicationModel = new Gson().fromJson(updateUUID_List.get(i).getValue(), MedicationModel.class);
                if (medicationModel.getMedicationUuidList() != null && medicationModel.getMedicationUuidList().contains(list.get(position).getUuid())) {
                    if (tag.equalsIgnoreCase("dispense")) {
                        holder.cb_value.setChecked(true);
                        holder.cb_value.setEnabled(false);
                    }

                  //  checkedList.add(model);
                }
                else {
                   // holder.cb_value.setChecked(false);
                  //  checkedList.remove(model);
                }
            }
        }
        // med - start

        // aid - start
        if (updateUUID_List != null && updateUUID_List.size() > 0) {
            for (int i = 0; i < updateUUID_List.size(); i++) {
                AidModel aidModel = new Gson().fromJson(updateUUID_List.get(i).getValue(), AidModel.class);
                if (aidModel.getAidUuidList() != null && aidModel.getAidUuidList().contains(list.get(position).getUuid())) {
                    if (tag.equalsIgnoreCase("dispense")) {
                        holder.cb_value.setChecked(true);
                        holder.cb_value.setEnabled(false);
                    }
                  //  checkedList.add(model);
                }
                else {
                   // holder.cb_value.setChecked(false);
                  //  checkedList.remove(model);
                }
            }
        }
        // aid - start
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

            cb_value.setOnClickListener(v -> {
                if (((CheckBox) v).isChecked()) {
                    list.get(getAdapterPosition()).setChecked(true);
                    checkedList.add(list.get(getAdapterPosition()));
                }
                else {
                    list.get(getAdapterPosition()).setChecked(false);
                    checkedList.remove(list.get(getAdapterPosition()));
                }
            });

/*
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
*/
        }
    }

    public HashSet<MedicationAidModel> getFinalList() {
        hash_checkedList.addAll(checkedList);
        return hash_checkedList;

      //  return checkedList;
    }

    private PatientAttributeLanguageModel getPatientAttributeFromJSON(String jsonString) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.fromJson(jsonString, PatientAttributeLanguageModel.class);
    }

}
