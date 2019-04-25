package io.intelehealth.client.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.intelehealth.client.R;
import io.intelehealth.client.app.IntelehealthApplication;
import io.intelehealth.client.dao.PullDataDAO;
import io.intelehealth.client.databinding.CardviewHomeBinding;
import io.intelehealth.client.views.activites.ActivePatientActivity;
import io.intelehealth.client.views.activites.ActivitySync;
import io.intelehealth.client.views.activites.IdentificationActivity;
import io.intelehealth.client.views.activites.SearchPatientActivity;
import io.intelehealth.client.views.activites.TodayPatientActivity;

/**
 * Created by tusharjois on 9/20/16.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.IconViewHolder> {
    Context context;

    public HomeAdapter(Context context) {
        this.context = context;

    }

    final static String TAG = HomeAdapter.class.getSimpleName();
    final String[] options = {IntelehealthApplication.getAppContext().getString(R.string.new_patient),
            IntelehealthApplication.getAppContext().getString(R.string.find_patient),
            IntelehealthApplication.getAppContext().getString(R.string.today_patient),
            IntelehealthApplication.getAppContext().getString(R.string.active_patient),
            IntelehealthApplication.getAppContext().getString(R.string.video_library)
            // , IntelehealthApplication.getAppContext().getString(R.string.action_sync_all)
    };
    final int[] icons = {R.drawable.ic_person_add_24dp, R.drawable.ic_search_24dp,
            R.drawable.ic_calendar_intele_24dp, R.drawable.ic_calendar_intele_24dp, R.drawable.ic_play_circle_filled_black_24dp
            //,android.R.drawable.ic_menu_preferences
    };

    //TODO: Change placeholder icon "android.R.drawable.ic_menu_my_calendar"
    LayoutInflater layoutInflater;

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        CardviewHomeBinding cardviewHomeBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cardview_home, parent, false);
        return new IconViewHolder(cardviewHomeBinding);
    }

    @Override
    public void onBindViewHolder(IconViewHolder holder, int position) {
        holder.cardviewHomeBinding.optionName.setText(options[position]);
        holder.cardviewHomeBinding.optionName.setId(position);
        holder.cardviewHomeBinding.optionIcon.setImageResource(icons[position]);
        holder.cardviewHomeBinding.cardviewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (holder.cardviewHomeBinding.optionName.getId()) {
                    case 0: {
                        Intent intent = new Intent(context, IdentificationActivity.class);
                        context.startActivity(intent);
                        break;
                    }
                    case 1: {
                        Intent intent = new Intent(context, SearchPatientActivity.class);
                        context.startActivity(intent);
                        break;
                    }
                    case 2: {

                        //TODO: Change Activity after coding is done.

                        // Query for today's patient
                        // SELECT * FROM visit WHERE start_datetime LIKE "2017-05-08T%" ORDER BY start_datetime ASC
                        Intent intent = new Intent(context, TodayPatientActivity.class);
                        context.startActivity(intent);
                        break;
                    }
                    case 3: {

                        //TODO: Change Activity after coding is done.

                        // Query for today's patient
                        // SELECT * FROM visit WHERE start_datetime LIKE "2017-05-08T%" ORDER BY start_datetime ASC
                        Intent intent = new Intent(context, ActivePatientActivity.class);
                        context.startActivity(intent);
                        break;
                    }
                    case 4: {
                        PullDataDAO pullDataDAO = new PullDataDAO();
                        pullDataDAO.pullData(context);
//                        Intent intent = new Intent(context, VideoLibraryActivity.class);
//                        context.startActivity(intent);
                        break;
                    }
                    case 5: {
                        Intent intent = new Intent(context, ActivitySync.class);
                        context.startActivity(intent);
                        break;
                    }
                    default:
                        Log.i(TAG, "Matching class not found");
                }
            }
        });
//        holder.optionName.setText(options[position]);
//        holder.optionName.setId(position);
//        holder.icon.setImageResource(icons[position]);
    }

    @Override
    public int getItemCount() {
        return this.options.length;
    }

    public static class IconViewHolder extends RecyclerView.ViewHolder {
        //        CardView cardView;
//        TextView optionName;
//        ImageView icon;
        Context context;

        CardviewHomeBinding cardviewHomeBinding;

        IconViewHolder(CardviewHomeBinding cardviewHomeBinding1) {
            super(cardviewHomeBinding1.getRoot());
            this.cardviewHomeBinding = cardviewHomeBinding1;

        }


    }

}
