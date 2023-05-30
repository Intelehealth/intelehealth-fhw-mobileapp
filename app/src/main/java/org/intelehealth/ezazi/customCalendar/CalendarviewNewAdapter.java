package org.intelehealth.ezazi.customCalendar;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;

import java.util.Calendar;
import java.util.List;

public class CalendarviewNewAdapter extends RecyclerView.Adapter<CalendarviewNewAdapter.MyViewHolder> {
    private static final String TAG = "CalendarviewNewAdapter";
    Context context;
    List<CalendarviewModel> listOfDates;
    Calendar calendar;
    OnItemClickListener listener;
    private int selectedPos = -1;
    String tag = "unclicked";
    int currentDay, currentYear,currentMonth, currentMonthNew;

    public CalendarviewNewAdapter(Context context, List<CalendarviewModel> listOfDates,
                                  CalendarviewNewAdapter.OnItemClickListener listener) {
        this.context = context;
        this.listOfDates = listOfDates;
        this.listener = listener;

        calendar = Calendar.getInstance();
        currentDay = calendar.get(Calendar.DATE);
        currentMonthNew = calendar.get(Calendar.MONTH) + 1;
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.getActualMaximum(Calendar.MONTH) + 1;

    }

    @Override
    public CalendarviewNewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_calendarview_ui2, parent, false);
        CalendarviewNewAdapter.MyViewHolder myViewHolder = new CalendarviewNewAdapter.MyViewHolder(view);

        return myViewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(CalendarviewNewAdapter.MyViewHolder holder, int position) {
        CalendarviewModel calendarModel = listOfDates.get(position);
        holder.tvDate.setText(calendarModel.getDate() + "");

        if(calendarModel.getSelectedYear() ==  currentYear && calendarModel.getSelectedMonth() ==  currentMonthNew && tag.equalsIgnoreCase("unclicked")) {
            selectedPos = currentDay - 1;
            calendarModel.setCurrentMonthCompletedDate(false);
        }

        holder.layoutParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    tag = "clicked";
                    //  int position = getAdapterPosition();
                    if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onItemClick(calendarModel);
                        notifyItemChanged(selectedPos);
                        selectedPos = holder.getAdapterPosition();
                        notifyItemChanged(selectedPos);
                    }
                }
            }
        });

        changeToSelect(selectedPos, position, holder, calendarModel);

        if(calendarModel.getSelectedYear() ==  currentYear && calendarModel.getSelectedMonth() == currentMonthNew){
            if (calendarModel.isPrevMonth || calendarModel.isNextMonth || calendarModel.isCurrentMonthCompletedDate()) {
                holder.tvDate.setTextColor(context.getColor(R.color.edittextBorder));
            }
        } else{
            if (calendarModel.isPrevMonth || calendarModel.isNextMonth){
                holder.tvDate.setTextColor(context.getColor(R.color.edittextBorder));
            }
        }

        if(calendarModel.getSelectedYear() == currentYear && calendarModel.getSelectedMonth() > currentMonthNew)
        {
            holder.tvDate.setTextColor(context.getResources().getColor(R.color.edittextBorder));
            holder.tvDate.setClickable(false);
            holder.layoutParent.setClickable(false);
        }

        if(calendarModel.getSelectedYear() == currentYear && calendarModel.getSelectedMonth() == currentMonthNew && position >= currentDay)
        {
            holder.tvDate.setTextColor(context.getResources().getColor(R.color.edittextBorder));
            holder.tvDate.setClickable(false);
            holder.layoutParent.setClickable(false);
        }

    }


    @Override
    public int getItemCount() {
        return listOfDates.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        RelativeLayout layoutParent;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date_newview);
            layoutParent = itemView.findViewById(R.id.parent_calview);

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface OnItemClickListener {
        void onItemClick(CalendarviewModel calendarModel);
    }

    public void changeToSelect(int selectedPos, int position, MyViewHolder holder, CalendarviewModel calendarModel) {
        Log.d(TAG, "changeToSelect: position : " + position);

        if (selectedPos == position) {
            Log.d(TAG, "changeToSelect: in true");
            holder.layoutParent.setBackground(context.getResources().getDrawable(R.drawable.bg_selected_date_custom_calview_ui2));
            holder.tvDate.setTextColor(context.getResources().getColor(R.color.font_black_0));
            if (calendarModel.isPrevMonth || calendarModel.isNextMonth) {
                holder.layoutParent.setBackground(null);
            }

        } else {
            Log.d(TAG, "changeToSelect: in false");
            holder.layoutParent.setBackground(null);
            holder.tvDate.setTextColor(context.getResources().getColor(R.color.font_black_0));
        }
    }

}