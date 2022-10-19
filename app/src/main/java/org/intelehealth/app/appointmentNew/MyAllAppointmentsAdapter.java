package org.intelehealth.app.appointmentNew;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

public class MyAllAppointmentsAdapter extends RecyclerView.Adapter<MyAllAppointmentsAdapter.MyViewHolder> {
    Context context;

    public MyAllAppointmentsAdapter(Context context) {
        this.context = context;

    }

    @Override
    public MyAllAppointmentsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_appointments_ui2_new, parent, false);
        MyAllAppointmentsAdapter.MyViewHolder myViewHolder = new MyAllAppointmentsAdapter.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyAllAppointmentsAdapter.MyViewHolder holder, int position) {
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
