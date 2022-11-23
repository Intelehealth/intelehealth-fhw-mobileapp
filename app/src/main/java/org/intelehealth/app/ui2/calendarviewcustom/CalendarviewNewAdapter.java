package org.intelehealth.app.ui2.calendarviewcustom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.horizontalcalendar.CalendarModel;

import java.util.Calendar;
import java.util.List;

public class CalendarviewNewAdapter extends RecyclerView.Adapter<CalendarviewNewAdapter.MyViewHolder> {
    private static final String TAG = "CalendarviewNewAdapter";
    Context context;
    List<CalendarviewModel> listOfDates;
    Calendar calendar;
    OnItemClickListener listener;
    private int selectedPos = 0;

    public CalendarviewNewAdapter(Context context, List<CalendarviewModel> listOfDates, CalendarviewNewAdapter.OnItemClickListener listener) {
        this.context = context;
        this.listOfDates = listOfDates;
        this.listener = listener;

        calendar = Calendar.getInstance();

    }

    @Override
    public CalendarviewNewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_calendarview_ui2, parent, false);
        CalendarviewNewAdapter.MyViewHolder myViewHolder = new CalendarviewNewAdapter.MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(CalendarviewNewAdapter.MyViewHolder holder, int position) {
        CalendarviewModel calendarModel = listOfDates.get(position);
        holder.tvDate.setText(calendarModel.getDate()+"");

    }


    @Override
    public int getItemCount() {
        return listOfDates.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date_newview);

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
