package org.intelehealth.ekalhelpline.activities.recordings;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.ekalhelpline.models.dto.PatientDTO;
import org.intelehealth.ekalhelpline.utilities.DateAndTimeUtils;
import org.intelehealth.ekalhelpline.utilities.NetworkConnection;

import java.util.List;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.Myholder> {
    List<Recording> patients;
    Context context;

    public RecordingsAdapter(List<Recording> recordings, Context context) {
        this.patients = recordings;
        this.context = context;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_recording, parent, false);
        return new Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        final Recording recording = patients.get(position);
        if (recording != null) {
            holder.headTextView.setText(recording.Caller);
            if (!TextUtils.isEmpty(recording.language)) {
                holder.bodyTextView.setText(recording.language);
                holder.bodyTextView.setVisibility(View.VISIBLE);
            }
            else {
                holder.bodyTextView.setVisibility(View.GONE);
            }
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkConnection.isOnline(context) && recording != null) {
                    AudioPlayerActivity.start(v.getContext(), recording.RecordingURL, recording.Caller);
                } else {
                    Toast.makeText(context, R.string.please_connect_to_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return patients.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        private TextView headTextView, bodyTextView;

        public Myholder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head);
            linearLayout = itemView.findViewById(R.id.searchlinear);
            bodyTextView = itemView.findViewById(R.id.list_item_body);
        }
    }

}
