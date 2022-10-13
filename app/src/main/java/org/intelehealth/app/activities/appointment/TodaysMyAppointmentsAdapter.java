package org.intelehealth.app.activities.appointment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

public class TodaysMyAppointmentsAdapter extends RecyclerView.Adapter<TodaysMyAppointmentsAdapter.MyViewHolder> {
    Context context;

    public TodaysMyAppointmentsAdapter(Context context) {
        this.context = context;

    }

    @Override
    public TodaysMyAppointmentsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todays_appointments_ui2_new, parent, false);
        TodaysMyAppointmentsAdapter.MyViewHolder myViewHolder = new TodaysMyAppointmentsAdapter.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(TodaysMyAppointmentsAdapter.MyViewHolder holder, int position) {
        holder.cardParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AppointmentDetailsActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 3;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardParent;

        public MyViewHolder(View itemView) {
            super(itemView);
            cardParent = itemView.findViewById(R.id.card_all_appointments);

        }
    }


}
