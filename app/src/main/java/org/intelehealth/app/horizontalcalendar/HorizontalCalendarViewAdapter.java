package org.intelehealth.app.horizontalcalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.List;

public class HorizontalCalendarViewAdapter extends RecyclerView.Adapter<HorizontalCalendarViewAdapter.MyViewHolder> {
    private static final String TAG = "MyAllAppointmentsAdapte";
    Context context;
    List<CalendarModel> listOfDates;

    public HorizontalCalendarViewAdapter(Context context, List<CalendarModel> listOfDates) {
        this.context = context;
        this.listOfDates = listOfDates;
    }

    @Override
    public HorizontalCalendarViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horizontal_cal_view, parent, false);
        HorizontalCalendarViewAdapter.MyViewHolder myViewHolder = new HorizontalCalendarViewAdapter.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(HorizontalCalendarViewAdapter.MyViewHolder holder, int position) {
        CalendarModel calendarModel = listOfDates.get(position);
        holder.tvDate.setText(calendarModel.getDate() + "");
        holder.tvDay.setText(calendarModel.getDay());

        if (calendarModel.isCurrentDate) {
            holder.cardParent.setBackground(context.getResources().getDrawable(R.drawable.bg_horizontal_cal_view_selected));
            holder.tvDate.setTextColor(context.getResources().getColor(R.color.textColorWhite));
            holder.tvDay.setTextColor(context.getResources().getColor(R.color.textColorWhite));
        }else{

            holder.cardParent.setBackground(context.getResources().getDrawable(R.drawable.bg_horizontal_cal_view_ui2));
            holder.tvDate.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            holder.tvDay.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }

    }

    @Override
    public int getItemCount() {
        return listOfDates.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDay;
        CardView cardParent;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date_cal);
            tvDay = itemView.findViewById(R.id.tv_date_day);
            cardParent = itemView.findViewById(R.id.card_horizontal_view);

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
