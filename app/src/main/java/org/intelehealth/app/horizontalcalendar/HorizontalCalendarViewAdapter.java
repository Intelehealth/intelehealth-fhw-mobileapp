package org.intelehealth.app.horizontalcalendar;

import android.content.Context;

import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.Calendar;
import java.util.List;

public class HorizontalCalendarViewAdapter extends RecyclerView.Adapter<HorizontalCalendarViewAdapter.MyViewHolder> {
    private static final String TAG = "MyAllAppointmentsAdapte";
    Context context;
    List<CalendarModel> listOfDates;
    Calendar calendar;
    OnItemClickListener listener;
    private int selectedPos = 0;

    public HorizontalCalendarViewAdapter(Context context, List<CalendarModel> listOfDates, OnItemClickListener listener) {
        this.context = context;
        this.listOfDates = listOfDates;
        this.listener = listener;

        calendar = Calendar.getInstance();

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
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        holder.tvDay.setText(calendarModel.getDay());

        //changeToSelect(selectedPos == position ? Color.parseColor("#ca3854") : Color.BLACK, holder);
        CustomLog.d(TAG, "onBindViewHolder: selected month : " + calendarModel.getSelectedMonthForDays());
        CustomLog.d(TAG, "onBindViewHolder: currentMonth : " + currentMonth);
//        makeTodaysDateSelected(calendarModel, holder, currentMonth);

        holder.cardParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    //  int position = getAdapterPosition();
                    if (holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onItemClick(calendarModel);
                        notifyItemChanged(selectedPos);
                        selectedPos = holder.getBindingAdapterPosition();
                        notifyItemChanged(selectedPos);
                    }
                }
            }
        });
        changeToSelect(selectedPos, position, holder);

    }

    private void makeTodaysDateSelected(CalendarModel calendarModel, HorizontalCalendarViewAdapter.MyViewHolder holder, int currentMonth) {
        if (String.valueOf(currentMonth).trim().equals(calendarModel.getSelectedMonthForDays().trim()) && calendarModel.isCurrentDate) {
            holder.tvDay.setText(context.getString(R.string.today));
            holder.cardParent.setSelected(true);
//            holder.cardParent.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_horizontal_cal_view_selected));
//            holder.tvDate.setTextColor(ContextCompat.getColor(context,R.color.textColorWhite));
//            holder.tvDay.setTextColor(ContextCompat.getColor(context,R.color.textColorWhite));
        } else {
            holder.cardParent.setSelected(false);
//            holder.cardParent.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_horizontal_cal_view_ui2));
//            holder.tvDate.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
//            holder.tvDay.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
        }
    }

    /*public void changeToSelect(int colorBackground, HorizontalCalendarViewAdapter.MyViewHolder holder) {
        holder.cardParent.setBackgroundColor(colorBackground);
    }*/
    public void changeToSelect(int selectedPos, int position, HorizontalCalendarViewAdapter.MyViewHolder holder) {
        CustomLog.d(TAG, "changeToSelect: selectedPos : " + selectedPos);
        CustomLog.d(TAG, "changeToSelect: position : " + position);
        holder.cardParent.setSelected(selectedPos == position);
//        if (selectedPos == position) {
//            CustomLog.d(TAG, "changeToSelect: in true");
//            holder.cardParent.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_horizontal_cal_view_selected));
//            holder.tvDate.setTextColor(ContextCompat.getColor(context,R.color.textColorWhite));
//            holder.tvDay.setTextColor(ContextCompat.getColor(context,R.color.textColorWhite));
//        } else {
//            CustomLog.d(TAG, "changeToSelect: in false");
//
//            holder.cardParent.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_horizontal_cal_view_ui2));
//            holder.tvDate.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
//            holder.tvDay.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
//        }
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

    public interface OnItemClickListener {
        void onItemClick(CalendarModel calendarModel);
    }
}
