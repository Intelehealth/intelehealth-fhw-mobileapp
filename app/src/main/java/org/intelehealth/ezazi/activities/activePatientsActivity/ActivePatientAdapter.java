package org.intelehealth.ezazi.activities.activePatientsActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.models.ActivePatientModel;
import org.intelehealth.ezazi.utilities.DateAndTimeUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class ActivePatientAdapter extends RecyclerView.Adapter<ActivePatientAdapter.ActivePatientViewHolder> {

    public interface OnActionListener {
        void onEndVisitClicked(ActivePatientModel activePatientModel, boolean hasPrescription);
    }

    private OnActionListener actionListener;
    public List<ActivePatientModel> activePatientModels;
    public List<ActivePatientModel> filteractivePatient;
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<String> listPatientUUID;
    SessionManager sessionManager;

    public ActivePatientAdapter(List<ActivePatientModel> activePatientModels, List<ActivePatientModel> filteractivePatient, Context context,
                                ArrayList<String> _listPatientUUID, SessionManager sessionManager) {
        this.activePatientModels = activePatientModels;
        this.context = context;
        this.listPatientUUID = _listPatientUUID;
        this.sessionManager = sessionManager;
        this.filteractivePatient = filteractivePatient;
    }

    /**
     * Added by Vaghela Mithun
     * To calculate the total active cases
     */
    public int activeCasesCount() {
        int count = 0;
        for (ActivePatientModel activePatientModel : activePatientModels) {
            if (activePatientModel.getObsExistsFlag()) count++;
        }

        return count;
    }

    @Override
    public ActivePatientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_active_patient_ezazi, parent, false);
        ActivePatientViewHolder viewHolder = new ActivePatientViewHolder(row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ActivePatientViewHolder holder, int position) {
        final ActivePatientModel activePatientModel = filteractivePatient.get(position);
//        final ActivePatientModel filteractivePatient=filteractivePatient.get(position);
        String header;
        if (activePatientModel.getOpenmrs_id() != null) {
            header = String.format("%s %s, %s", activePatientModel.getFirst_name(),
                    activePatientModel.getLast_name(), activePatientModel.getOpenmrs_id());

//            holder.getTv_not_uploaded().setVisibility(View.GONE);
        } else {
            header = String.format("%s %s", activePatientModel.getFirst_name(),
                    activePatientModel.getLast_name());

//            holder.getTv_not_uploaded().setVisibility(View.VISIBLE);
//            holder.getTv_not_uploaded().setText(context.getResources().getString(R.string.visit_not_uploaded));
//            holder.getTv_not_uploaded().setBackgroundColor(context.getResources().getColor(R.color.lite_red));
        }

        if (activePatientModel.getSync().equalsIgnoreCase("0")) {
            holder.getTv_not_uploaded().setVisibility(View.VISIBLE);
            holder.getTv_not_uploaded().setText(context.getResources().getString(R.string.visit_not_uploaded));
            holder.getTv_not_uploaded().setBackgroundColor(context.getResources().getColor(R.color.lite_red));
        } else {
            holder.getTv_not_uploaded().setVisibility(View.GONE);
        }


//        int age = DateAndTimeUtils.getAge(activePatientModel.getDate_of_birth());

        //get date of birth and convert it into years and months
        String age = DateAndTimeUtils.getAgeInYears(activePatientModel.getDate_of_birth(), context);
//        String dob = DateAndTimeUtils.SimpleDatetoLongDate(activePatientModel.getDate_of_birth());
//        String body = String.format("%s %s (%s)", context.getString(R.string.identification_screen_prompt_age), age, activePatientModel.getGender());
//        Spanned body = Html.fromHtml(context.getString(R.string.identification_screen_prompt_age) + " <b>" + age + " (" + StringUtils.getLocaleGender(context, activePatientModel.getGender()) + ")</b>");
        String ageInYear = context.getString(R.string.identification_screen_prompt_age) + " " + age;
        holder.getHeadTextView().setText(String.format("%s %s", activePatientModel.getFirst_name(), activePatientModel.getLast_name()));
        holder.getBodyTextView().setText(activePatientModel.getOpenmrs_id());
        holder.tvAgeGender.setText(ageInYear);
        holder.tvStageNameTextView.setText(activePatientModel.getStageName());
        if (activePatientModel.getEnddate() == null) {
            holder.getIndicatorTextView().setText(R.string.active);
            holder.getIndicatorTextView().setBackgroundColor(Color.GREEN);
        } else {
            holder.getIndicatorTextView().setText(R.string.closed);
            holder.getIndicatorTextView().setBackgroundColor(Color.RED);
        }

        // alert -> start
        int count = activePatientModel.getAlertFlagTotal();
        holder.ivPriscription.setText(String.valueOf(count));
        if (count > 22) { // red
            // holder.cardView_todaysVisit.setCardBackgroundColor(context.getResources().getColor(R.color.red_1));
            holder.ivPriscription.setBackground(context.getResources().getDrawable(R.drawable.ic_high_alert));
            holder.ivPriscription.setTextColor(ContextCompat.getColor(context, R.color.colorHighAlert));
        } else if (count >= 15) { // yellow
            // holder.cardView_todaysVisit.setCardBackgroundColor(context.getResources().getColor(R.color.darkYellow2));
            holder.ivPriscription.setBackground(context.getResources().getDrawable(R.drawable.ic_yellow_alert));
            holder.ivPriscription.setTextColor(ContextCompat.getColor(context, R.color.colorMediumAlert));
        } else { // green
            // holder.cardView_todaysVisit.setCardBackgroundColor(context.getResources().getColor(R.color.green2));
            holder.ivPriscription.setBackground(context.getResources().getDrawable(R.drawable.ic_normal_alert));
            holder.ivPriscription.setTextColor(ContextCompat.getColor(context, R.color.colorNormalAlert));
        }
        // alert -> end

        if (activePatientModel.getObsExistsFlag()) {
            Animation anim = new AlphaAnimation(1.0f, 0.2f);
            anim.setDuration(1500); // more no means slow eg. 0 = fast blink && 1500 = slow blink.
            anim.setRepeatMode(Animation.INFINITE);
            anim.setRepeatCount(Animation.INFINITE);
            holder.cardView_todaysVisit.startAnimation(anim);
            holder.cardView_todaysVisit.setCardBackgroundColor(context.getResources().getColor(R.color.blinkCardColor));
        }

        if (activePatientModel.getBirthOutcomeValue() != null &&
                !activePatientModel.getBirthOutcomeValue().equalsIgnoreCase("")) {
//            holder.btnEndVisit.setVisibility(View.VISIBLE);
//            holder.btnEndVisit.setText(activePatientModel.getBirthOutcomeValue());
        } else {
//            holder.btnEndVisit.setVisibility(View.GONE);
        }

//            // indicator for next interval
//            String latestencounterTime = activePatientModel.getLatestencounterTime();
//            Calendar cal = Calendar.getInstance();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
//            SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//            try {
//                cal.setTime(sdf.parse(latestencounterTime));
//            } catch (ParseException e) {
//                e.printStackTrace();
//                try {
//                    cal.setTime(sdf_.parse(latestencounterTime));
//                } catch (ParseException ex) {
//                    ex.printStackTrace();
//                }
//            }
//
//            if (cal.equals(Calendar.getInstance())) {
//           /* Animation anim = new AlphaAnimation(0.0f, 1.0f);
//            anim.setDuration(50); //You can manage the blinking time with this parameter
//            anim.setStartOffset(20);
//            anim.setRepeatMode(Animation.REVERSE);
//            anim.setRepeatCount(Animation.INFINITE);
//            holder.cardView_todaysVisit.startAnimation(anim);*/
//                Toast.makeText(context, "Time to Visit: " + activePatientModel.getFirst_name() + " " +
//                        activePatientModel.getLast_name(), Toast.LENGTH_SHORT).show();
//            }


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent visitSummary = new Intent(context, TimelineVisitSummaryActivity.class);
                String patientUuid = activePatientModel.getPatientuuid();

                String patientSelection = "uuid = ?";
                String[] patientArgs = {patientUuid};
                String[] patientColumns = {"first_name", "middle_name", "last_name", "gender",
                        "date_of_birth"};
                SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
                String visit_id = "";

                String end_date = "", dob = "", mGender = "", patientName = "";
                float float_ageYear_Month = 0;
                if (idCursor.moveToFirst()) {
                    do {
                        mGender = idCursor.getString(idCursor.getColumnIndexOrThrow("gender"));
                        patientName = idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")) + " " +
                                idCursor.getString(idCursor.getColumnIndexOrThrow("last_name"));
                        dob = idCursor.getString((idCursor.getColumnIndexOrThrow("date_of_birth")));
                    } while (idCursor.moveToNext());
                }
                idCursor.close();

                String visitSelection = "patientuuid = ?";
                String[] visitArgs = {patientUuid};
                String[] visitColumns = {"uuid, startdate", "enddate"};
                String visitOrderBy = "startdate";
                Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);

                if (visitCursor.getCount() >= 1) {
                    if (visitCursor.moveToLast() && visitCursor != null) {
                        do {
                            if (visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid")).equalsIgnoreCase("" + activePatientModel.getUuid())) {
                                end_date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"));
                                visit_id = visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"));
                            } else {

                            }
                        } while (visitCursor.moveToPrevious());
                    }
                }
                visitCursor.close();

                String encounterlocalAdultintial = "";
                String encountervitalsLocal = null;
                String encounterIDSelection = "visituuid = ?";

                String[] encounterIDArgs = {visit_id};

                EncounterDAO encounterDAO = new EncounterDAO();
                Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                if (encounterCursor != null && encounterCursor.moveToFirst()) {
                    do {
                        if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                            encountervitalsLocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        }
                        if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                            encounterlocalAdultintial = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        }
                    } while (encounterCursor.moveToNext());
                }
                encounterCursor.close();

                Boolean past_visit = false;
                if (end_date == null || end_date.isEmpty()) {
                    past_visit = false;
                } else {
                    past_visit = true;
                }

                float_ageYear_Month = DateAndTimeUtils.getFloat_Age_Year_Month(dob);

                visitSummary.putExtra("visitUuid", visit_id);
                visitSummary.putExtra("patientUuid", patientUuid);
                visitSummary.putExtra("encounterUuidVitals", encountervitalsLocal);
                visitSummary.putExtra("encounterUuidAdultIntial", encounterlocalAdultintial);
                visitSummary.putExtra("EncounterAdultInitial_LatestVisit", encounterlocalAdultintial);
                visitSummary.putExtra("patientNameTimeline", patientName);
                visitSummary.putExtra("gender", mGender);
                visitSummary.putExtra("float_ageYear_Month", float_ageYear_Month);
                visitSummary.putExtra("tag", "");
                visitSummary.putExtra("providerID", sessionManager.getProviderID());
                visitSummary.putExtra("pastVisit", past_visit);

                if (holder.ivPriscription.getTag().equals("1")) {
                    visitSummary.putExtra("hasPrescription", "true");
                } else {
                    visitSummary.putExtra("hasPrescription", "false");
                }
                context.startActivity(visitSummary);
                /*String patientStatus = "returning";
                Intent intent = new Intent(context, PatientDetailActivity.class);
                intent.putExtra("patientUuid", activePatientModel.getPatientuuid());
                intent.putExtra("status", patientStatus);
                intent.putExtra("tag", "");

                if (holder.ivPriscription.getTag().equals("1")) {
                    intent.putExtra("hasPrescription", "true");
                } else {
                    intent.putExtra("hasPrescription", "false");
                }
                context.startActivity(intent);*/
            }
        };
        holder.getRootView().setOnClickListener(listener);
//        holder.btnVisitDetails.setOnClickListener(listener);

        boolean enableEndVisit = true;
/*
        for (int i = 0; i < listPatientUUID.size(); i++) {
            if (activePatientModels.get(position).getPatientuuid().equalsIgnoreCase(listPatientUUID.get(i))) {
                holder.ivPriscription.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_prescription_green));
                holder.ivPriscription.setTag("1");
                enableEndVisit = true;
            }
        }
*/

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.btnVisitDetails.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
        } else {
            holder.btnEndVisit.setBackgroundResource(R.drawable.round_corner_yellow);
        }*/
//        holder.btnVisitDetails.setBackgroundResource(R.drawable.round_corner_yellow);
        //  holder.btnEndVisit.setBackgroundResource(R.drawable.round_corner_red);

        //  holder.btnEndVisit.setEnabled(enableEndVisit);
        //if (enableEndVisit) {
       /* if (activePatientModel.getEnddate() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.btnEndVisit.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            } else {
                holder.btnEndVisit.setBackgroundResource(R.drawable.bg_end_visit);
            }
            holder.btnEndVisit.setText(context.getString(R.string.action_end_visit));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.btnEndVisit.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                holder.btnEndVisit.setBackgroundResource(R.drawable.bg_visit_closed);
            }

            holder.btnEndVisit.setText(context.getString(R.string.visit_closed));
        }*/

/*
        holder.btnEndVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activePatientModel.getEnddate() != null)
                    return;

                if (actionListener != null)
                    actionListener.onEndVisitClicked(activePatientModel, "1".equals(holder.ivPriscription.getTag()));
            }
        });
*/
        //}
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return filteractivePatient.size();
    }

    public class ActivePatientViewHolder extends RecyclerView.ViewHolder {
        private TextView headTextView;
        private TextView bodyTextView;
        private TextView indicatorTextView;
        private View rootView;
        private TextView ivPriscription;
        private TextView tv_not_uploaded;
        TextView tvAgeGender, tvStageNameTextView;
        private CardView cardView_todaysVisit;

        public ActivePatientViewHolder(View itemView) {
            super(itemView);
            tvStageNameTextView = itemView.findViewById(R.id.tvStageName);
            headTextView = itemView.findViewById(R.id.list_item_head_text_view);
            bodyTextView = itemView.findViewById(R.id.list_item_body_text_view);
            indicatorTextView = itemView.findViewById(R.id.list_item_indicator_text_view);
            ivPriscription = itemView.findViewById(R.id.tvAlertCount);
            tv_not_uploaded = (TextView) itemView.findViewById(R.id.tv_not_uploaded);
//            btnEndVisit = itemView.findViewById(R.id.btn_end_visit);
//            btnVisitDetails = itemView.findViewById(R.id.btn_visit_details);
            tvAgeGender = itemView.findViewById(R.id.tv_age_gender);
            cardView_todaysVisit = itemView.findViewById(R.id.cardView_todaysVisit);
            rootView = itemView;
        }

        public TextView getHeadTextView() {
            return headTextView;
        }

        public void setHeadTextView(TextView headTextView) {
            this.headTextView = headTextView;
        }

        public TextView getBodyTextView() {
            return bodyTextView;
        }

        public void setBodyTextView(TextView bodyTextView) {
            this.bodyTextView = bodyTextView;
        }

        public TextView getIndicatorTextView() {
            return indicatorTextView;
        }

        public void setIndicatorTextView(TextView indicatorTextView) {
            this.indicatorTextView = indicatorTextView;
        }

        public View getRootView() {
            return rootView;
        }

        public TextView getTv_not_uploaded() {
            return tv_not_uploaded;
        }

        public void setTv_not_uploaded(TextView tv_not_uploaded) {
            this.tv_not_uploaded = tv_not_uploaded;
        }
    }

    public void setActionListener(OnActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String Key = charSequence.toString();
                if (Key.isEmpty()) {
                    filteractivePatient = activePatientModels;

                } else {
                    List<ActivePatientModel> listfiltered = new ArrayList<>();
                    for (ActivePatientModel row : activePatientModels) {
                        if (row.getFirst_name().toLowerCase().contains(Key.toLowerCase()) || row.getLast_name().toLowerCase().contains(Key.toLowerCase())) {
                            listfiltered.add(row);
                        }
                    }
                    filteractivePatient = listfiltered;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteractivePatient;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteractivePatient = (List<ActivePatientModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}