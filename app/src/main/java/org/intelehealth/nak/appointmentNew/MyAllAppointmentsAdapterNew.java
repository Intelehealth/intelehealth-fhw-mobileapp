package org.intelehealth.nak.appointmentNew;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.nak.R;

import java.util.List;

public class MyAllAppointmentsAdapterNew extends RecyclerView.Adapter<MyAllAppointmentsAdapterNew.MyViewHolder> {
    Context context;
    List<String> modelList;

    public MyAllAppointmentsAdapterNew(Context context, List<String> modelList) {
        this.context = context;
        this.modelList = modelList;

    }

    @Override
    public MyAllAppointmentsAdapterNew.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_appointments_ui2_new, parent, false);
        MyAllAppointmentsAdapterNew.MyViewHolder myViewHolder = new MyAllAppointmentsAdapterNew.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyAllAppointmentsAdapterNew.MyViewHolder holder, int position) {
       /* holder.cardParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AppointmentDetailsActivity.class);
                context.startActivity(intent);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return 3;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardParent;

        public MyViewHolder(View itemView) {
            super(itemView);
          //  cardParent = itemView.findViewById(R.id.card_all_appointments);

        }
    }


}
