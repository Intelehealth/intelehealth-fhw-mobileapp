package org.intelehealth.app.activities.patientDetailActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.List;

import org.intelehealth.app.R;
import org.intelehealth.app.models.FamilyMemberRes;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.FamilyMemberViewHolder> {

    List<FamilyMemberRes> listPatientNames;
    Context context;

    public FamilyMemberAdapter(List<FamilyMemberRes> listPatientNames, Context context) {
        this.listPatientNames = listPatientNames;
        this.context = context;
    }

    @NonNull
    @Override
    public FamilyMemberAdapter.FamilyMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_family_member, parent, false);
        return new FamilyMemberViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyMemberAdapter.FamilyMemberViewHolder holder, int position) {
        FamilyMemberRes member = listPatientNames.get(position);
        String patientUUID = member.getUuid();
        boolean isHead = member.isHeadOfHousehold();
        holder.ivHeadOfHousehold.setVisibility(isHead ? View.VISIBLE : View.GONE);

        holder.tvFamilyName.setText(member.getName());
        holder.tvOpenMRSID.setText(member.getOpenMRSID());
        holder.tvOpenMRSID.setPaintFlags(holder.tvOpenMRSID.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        holder.tvOpenMRSID.setOnClickListener(view -> {
            Intent intent = new Intent(context, PatientDetailActivity2.class);
            intent.putExtra("patientUuid", patientUUID);
            intent.putExtra("tag", "familyMember");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listPatientNames.size();
    }

    public class FamilyMemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivHeadOfHousehold;
        private TextView tvFamilyName;
        private TextView tvOpenMRSID;

        public FamilyMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivHeadOfHousehold = itemView.findViewById(R.id.iv_head_of_household);
            tvFamilyName = itemView.findViewById(R.id.tv_name);
            tvOpenMRSID = itemView.findViewById(R.id.tv_openMRSID);
        }
    }
}
