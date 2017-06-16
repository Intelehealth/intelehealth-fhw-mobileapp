package io.intelehealth.client;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by tusharjois on 9/20/16.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.IconViewHolder> {

    final static String TAG = HomeAdapter.class.getSimpleName();
    final String[] options = {"New Patient", "Lookup Patient", "Today's Patient"};

    //TODO: Change placeholder icon "android.R.drawable.ic_menu_my_calendar"
    final int[] icons = {R.drawable.ic_person_add_24dp, R.drawable.ic_search_24dp, android.R.drawable.ic_menu_my_calendar};

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_home, parent, false);
        return new IconViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(IconViewHolder holder, int position) {
        holder.optionName.setText(options[position]);
        holder.icon.setImageResource(icons[position]);
    }

    @Override
    public int getItemCount() {
        return this.options.length;
    }

    public static class IconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardView;
        TextView optionName;
        ImageView icon;
        Context context;

        IconViewHolder(View itemView, Context activityContext) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.cardView = (CardView) itemView.findViewById(R.id.cardview_home);
            this.optionName = (TextView) itemView.findViewById(R.id.option_name);
            this.icon = (ImageView) itemView.findViewById(R.id.option_icon);
            this.context = activityContext;
        }

        @Override
        public void onClick(View v) {
            switch (this.optionName.getText().toString()) {
                case "New Patient": {
                    Intent intent = new Intent(this.context, IdentificationActivity.class);
                    this.context.startActivity(intent);
                    break;
                }
                case "Lookup Patient": {
                    Intent intent = new Intent(this.context, SearchPatientActivity.class);
                    this.context.startActivity(intent);
                    break;
                }
                case "Today's Patient": {

                    //TODO: Change Activity after coding is done.

                    // Query for today's patient
                    // SELECT * FROM visit WHERE start_datetime LIKE "2017-05-08T%" ORDER BY start_datetime ASC
                    Intent intent = new Intent(this.context, TodayPatientActivity.class);
                    this.context.startActivity(intent);
                    break;
                }
                default:
                    Log.i(TAG, "Matching class not found");
            }

        }

    }

}
