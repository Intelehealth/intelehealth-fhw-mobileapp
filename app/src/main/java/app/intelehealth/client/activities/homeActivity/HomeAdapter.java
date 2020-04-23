package app.intelehealth.client.activities.homeActivity;

import android.content.Context;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.intelehealth.client.R;
import app.intelehealth.client.activities.activePatientsActivity.ActivePatientActivity;
import app.intelehealth.client.activities.identificationActivity.IdentificationActivity;
import app.intelehealth.client.activities.privacyNoticeActivity.PrivacyNotice_Activity;
import app.intelehealth.client.activities.searchPatientActivity.SearchPatientActivity;
import app.intelehealth.client.activities.todayPatientActivity.TodayPatientActivity;
import app.intelehealth.client.activities.videoLibraryActivity.VideoLibraryActivity;
import app.intelehealth.client.app.IntelehealthApplication;
import app.intelehealth.client.utilities.ConfigUtils;

import app.intelehealth.client.activities.activitySync.ActivitySync;

/**
 * Created by tusharjois on 9/20/16.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.IconViewHolder> {
    final static String TAG = HomeAdapter.class.getSimpleName();

    final String[] options = {IntelehealthApplication.getAppContext().getString(R.string.new_patient),
            IntelehealthApplication.getAppContext().getString(R.string.find_patient),
            IntelehealthApplication.getAppContext().getString(R.string.today_visits),
            IntelehealthApplication.getAppContext().getString(R.string.active_visits),
            IntelehealthApplication.getAppContext().getString(R.string.video_library),
            IntelehealthApplication.getAppContext().getString(R.string.lastsync_normal)

            // , IntelehealthApplication.getAppContext().getString(R.string.action_sync_all)
    };

//   TODO: Change placeholder icon "android.R.drawable.ic_menu_my_calendar"

    final int[] icons = {R.drawable.ic_person_add_24dp, R.drawable.ic_search_24dp,
            R.drawable.ic_calendar_intele_24dp, R.drawable.ic_active_patients_black, R.drawable.ic_play_circle_filled_black_24dp
            , android.R.drawable.ic_menu_preferences
    };

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_home, parent, false);
        return new IconViewHolder(v, parent.getContext());
    }

    @Override
    public void onBindViewHolder(IconViewHolder holder, int position) {
        holder.optionName.setText(options[position]);
        holder.optionName.setId(position);
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
            this.cardView = itemView.findViewById(R.id.cardview_home);
            this.optionName = itemView.findViewById(R.id.option_name);
            this.icon = itemView.findViewById(R.id.option_icon);
            this.context = activityContext;
        }

        @Override
        public void onClick(View v) {
            switch (this.optionName.getId()) {
                case 0: {

                    //Loads the config file values and check for the boolean value of privacy key.
                    ConfigUtils configUtils = new ConfigUtils(this.context);
                    if(configUtils.privacy_notice())
                    {
                        Intent intent = new Intent(this.context, PrivacyNotice_Activity.class);
                        this.context.startActivity(intent);
                        break;
                    }
                    else
                    {
                        Intent intent = new Intent(this.context, IdentificationActivity.class);
                        this.context.startActivity(intent);
                        break;
                    }
                }
                case 1: {
                    Intent intent = new Intent(this.context, SearchPatientActivity.class);
                    this.context.startActivity(intent);
                    break;
                }
                case 2: {
                    Intent intent = new Intent(this.context, TodayPatientActivity.class);
                    this.context.startActivity(intent);
                    break;
                }
                case 3: {
                    Intent intent = new Intent(this.context, ActivePatientActivity.class);
                    this.context.startActivity(intent);
                    break;
                }
                case 4: {
                    Intent intent = new Intent(this.context, VideoLibraryActivity.class);
                    this.context.startActivity(intent);
                    break;
                }
                case 5: {
                    Intent intent = new Intent(this.context, ActivitySync.class);
                    this.context.startActivity(intent);
                    break;
                }
                case 6: {
                    Intent intent = new Intent(this.context, ActivitySync.class);
                    this.context.startActivity(intent);
                    break;
                }
                default:
                    Log.i(TAG, "Matching class not found");
            }

        }

    }

}
