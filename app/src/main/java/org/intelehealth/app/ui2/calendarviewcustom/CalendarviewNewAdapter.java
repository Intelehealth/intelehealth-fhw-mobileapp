package org.intelehealth.app.ui2.calendarviewcustom;

import android.content.Context;
import android.os.Build;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.horizontalcalendar.CalendarModel;
import org.intelehealth.app.horizontalcalendar.HorizontalCalendarViewAdapter;

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
    int currentDay, currentYear, currentMonth, currentMonthNew;
    int todayDatePosition = 100, count = 0;
    String whichDate;

    public CalendarviewNewAdapter(Context context, List<CalendarviewModel> listOfDates,
                                  CalendarviewNewAdapter.OnItemClickListener listener, String whichDate) {
        this.context = context;
        this.listOfDates = listOfDates;
        this.listener = listener;
        this.whichDate = whichDate;

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
        if (calendarModel.getSelectedYear() == currentYear && calendarModel.getSelectedMonth() == currentMonthNew && tag.equalsIgnoreCase("unclicked")) {
            if (calendarModel.getDate() == currentDay) {
                if (calendarModel.getDate() < 15 && count == 0) {
                    selectedPos = position;
                    todayDatePosition = position;
                } else if (calendarModel.getDate() > 24 && count == 1) {
                    selectedPos = position;
                    todayDatePosition = position;
                } else if (calendarModel.getDate() >= 15 && calendarModel.getDate() <= 24) {
                    selectedPos = position;
                    todayDatePosition = position;
                }
                count = 1;
            }
            calendarModel.setCurrentMonthCompletedDate(false);
        }

        changeToSelect(selectedPos, position, holder, calendarModel);

        holder.layoutParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    tag = "clicked";
                    if (holder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onItemClick(calendarModel);
                        notifyItemChanged(selectedPos);
                        selectedPos = holder.getBindingAdapterPosition();
                        notifyItemChanged(selectedPos);
                    }
                }
            }
        });


        if (calendarModel.getSelectedYear() == currentYear && calendarModel.getSelectedMonth() == currentMonthNew) {
            if (calendarModel.isPrevMonth || calendarModel.isNextMonth || calendarModel.isCurrentMonthCompletedDate()) {
                holder.tvDate.setTextColor(ContextCompat.getColor(context, R.color.edittextBorder));
            }
        } else {
            if (calendarModel.isPrevMonth || calendarModel.isNextMonth) {
                holder.tvDate.setTextColor(ContextCompat.getColor(context, R.color.edittextBorder));
            }
        }

        if (calendarModel.getSelectedYear() == currentYear && calendarModel.getSelectedMonth() > currentMonthNew) {
            if (!whichDate.equalsIgnoreCase("fromdate") && !whichDate.equalsIgnoreCase("todate")) {
                holder.tvDate.setTextColor(ContextCompat.getColor(context, R.color.edittextBorder));
                holder.tvDate.setClickable(false);
                holder.layoutParent.setClickable(false);
            }
        }

        if (calendarModel.getSelectedYear() == currentYear && calendarModel.getSelectedMonth() == currentMonthNew && position > todayDatePosition) {
            if (!whichDate.equalsIgnoreCase("fromdate") && !whichDate.equalsIgnoreCase("todate")) {
                holder.tvDate.setTextColor(ContextCompat.getColor(context, R.color.edittextBorder));
                holder.tvDate.setClickable(false);
                holder.layoutParent.setClickable(false);
            }
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
        CustomLog.d(TAG, "changeToSelect: position : " + position);
        holder.layoutParent.setSelected(selectedPos == position);
        if (calendarModel.isPrevMonth || calendarModel.isNextMonth) {
            holder.layoutParent.setSelected(false);
        }
//        if (selectedPos == position) {
//            CustomLog.d(TAG, "changeToSelect: in true");
////            holder.layoutParent.setBackground(ContextCompat.getDrawable(context,R.drawable.bg_selected_date_custom_calview_ui2));
////            holder.tvDate.setTextColor(ContextCompat.getColor(context, R.color.textColorBlack));
//            if (calendarModel.isPrevMonth || calendarModel.isNextMonth) {
////                holder.layoutParent.setBackground(null);
//                holder.layoutParent.setSelected(false);
//            }
//        } else {
//            CustomLog.d(TAG, "changeToSelect: in false");
////            holder.layoutParent.setBackground(null);
////            holder.tvDate.setTextColor(ContextCompat.getColor(context, R.color.textColorBlack));
//        }
    }

}
