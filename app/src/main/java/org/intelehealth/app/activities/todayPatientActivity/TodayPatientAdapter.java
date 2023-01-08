package org.intelehealth.app.activities.todayPatientActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.TodayPatientModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.StringUtils;


/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class TodayPatientAdapter extends RecyclerView.Adapter<TodayPatientAdapter.TodayPatientViewHolder> {

    interface OnActionListener {
        void onEndVisitClicked(TodayPatientModel todayPatientModel, boolean hasPrescription);
    }
    private OnActionListener actionListener;
    List<TodayPatientModel> todayPatientModelList;
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<String> listPatientUUID;

    public TodayPatientAdapter(List<TodayPatientModel> todayPatientModelList, Context context, ArrayList<String> _listPatientUUID) {
        this.todayPatientModelList = todayPatientModelList;
        this.context = context;
        this.listPatientUUID = _listPatientUUID;
    }

    @Override
    public TodayPatientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_today_patient, parent, false);
        TodayPatientViewHolder viewHolder = new TodayPatientViewHolder(row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TodayPatientViewHolder holder, int position) {
        final TodayPatientModel todayPatientModel = todayPatientModelList.get(position);
        String header;
        if (todayPatientModel.getOpenmrs_id() != null) {
            header = String.format("%s %s, %s", todayPatientModel.getFirst_name(),
                    todayPatientModel.getLast_name(), todayPatientModel.getOpenmrs_id());
        } else {
            header = String.format("%s %s", todayPatientModel.getFirst_name(),
                    todayPatientModel.getLast_name());
        }
//        int age = DateAndTimeUtils.getAge(todayPatientModel.getDate_of_birth());
        String age = DateAndTimeUtils.getAgeInYears(todayPatientModel.getDate_of_birth(), context);
        String dob = DateAndTimeUtils.SimpleDatetoLongDate(todayPatientModel.getDate_of_birth());
//        String body = context.getString(R.string.identification_screen_prompt_age) + " " + age;
        Spanned body = Html.fromHtml(context.getString(R.string.identification_screen_prompt_age) + " <b>" + age + " (" + StringUtils.getLocaleGender(context, todayPatientModel.getGender()) + ")</b>");

        if (todayPatientModel.getSync().equalsIgnoreCase("0")){
            holder.getTv_not_uploaded().setVisibility(View.VISIBLE);
            holder.getTv_not_uploaded().setText(context.getResources().getString(R.string.visit_not_uploaded));
            holder.getTv_not_uploaded().setBackgroundColor(context.getResources().getColor(R.color.lite_red));
        } else {
            holder.getTv_not_uploaded().setVisibility(View.GONE);
        }

        holder.getHeadTextView().setText(String.format("%s %s", todayPatientModel.getFirst_name(), todayPatientModel.getLast_name()));
        holder.getBodyTextView().setText(todayPatientModel.getOpenmrs_id());
        holder.tvAgeGender.setText(body);
        if (todayPatientModel.getEnddate() == null) {
            holder.getIndicatorTextView().setText(R.string.active);
            holder.getIndicatorTextView().setBackgroundColor(Color.GREEN);
        } else {
            holder.getIndicatorTextView().setText(R.string.closed);
            holder.getIndicatorTextView().setBackgroundColor(Color.RED);
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent visitSummary = new Intent(context, VisitSummaryActivity.class);
                String patientUuid = todayPatientModel.getPatientuuid();

                String patientSelection = "uuid = ?";
                String[] patientArgs = {patientUuid};
                String[] patientColumns = {"first_name", "middle_name", "last_name", "gender",
                        "date_of_birth",};
                SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
                String visit_id = "";

                String end_date = "", dob = "", mGender = "", patientName = "", patientLName = "", patientFName = "";
                float float_ageYear_Month = 0;
                if (idCursor.moveToFirst()) {
                    do {
                        mGender = idCursor.getString(idCursor.getColumnIndexOrThrow("gender"));
                        patientName = idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")) + " " +
                                idCursor.getString(idCursor.getColumnIndexOrThrow("last_name"));
                        patientFName = idCursor.getString(idCursor.getColumnIndexOrThrow("first_name"));
                        patientLName = idCursor.getString(idCursor.getColumnIndexOrThrow("last_name"));
                        dob=idCursor.getString((idCursor.getColumnIndexOrThrow("date_of_birth")));
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
                            if(visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid")).equalsIgnoreCase(""+todayPatientModel.getUuid())){
                            end_date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"));
                            visit_id = visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"));}
                            else{

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

                float_ageYear_Month=DateAndTimeUtils.getFloat_Age_Year_Month(dob);

                visitSummary.putExtra("visitUuid", visit_id);
                visitSummary.putExtra("patientUuid", patientUuid);
                visitSummary.putExtra("encounterUuidVitals", encountervitalsLocal);
                visitSummary.putExtra("encounterUuidAdultIntial", encounterlocalAdultintial);
                visitSummary.putExtra("EncounterAdultInitial_LatestVisit", encounterlocalAdultintial);
                visitSummary.putExtra("name", patientName);
                visitSummary.putExtra("patientFirstName", patientFName);
                visitSummary.putExtra("patientLastName", patientLName);
                visitSummary.putExtra("gender", mGender);
                visitSummary.putExtra("float_ageYear_Month", float_ageYear_Month);
                visitSummary.putExtra("tag", "");
                visitSummary.putExtra("pastVisit", past_visit);

                if (holder.ivPriscription.getTag().equals("1")) {
                    visitSummary.putExtra("hasPrescription", "true");
                } else {
                    visitSummary.putExtra("hasPrescription", "false");
                }
                context.startActivity(visitSummary);

                /*String patientStatus = "returning";
                Intent intent = new Intent(context, PatientDetailActivity.class);
                intent.putExtra("patientUuid", todayPatientModel.getPatientuuid());
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
//        holder.getRootView().setOnClickListener(listener);
        holder.btnVisitDetails.setOnClickListener(listener);

        boolean enableEndVisit = false;
        for (int i = 0; i < listPatientUUID.size(); i++) {
            if (todayPatientModelList.get(position).getPatientuuid().equalsIgnoreCase(listPatientUUID.get(i))) {
                holder.ivPriscription.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_prescription_green));
                holder.ivPriscription.setTag("1");
                enableEndVisit = true;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.btnVisitDetails.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
        } else {
            holder.btnEndVisit.setBackgroundResource(R.drawable.bg_visit_details);
        }

        holder.btnEndVisit.setEnabled(enableEndVisit);
        if (enableEndVisit) {
            if (todayPatientModel.getEnddate() == null) {
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
            }

            holder.btnEndVisit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (todayPatientModel.getEnddate() != null)
                        return;

                    if (actionListener != null)
                        actionListener.onEndVisitClicked(todayPatientModel, "1".equals(holder.ivPriscription.getTag()));
                }
            });
        }
        else
        {
            if(todayPatientModel.getEnddate()!=null)
                holder.btnEndVisit.setText(context.getString(R.string.visit_closed));

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return todayPatientModelList.size();
    }

    class TodayPatientViewHolder extends RecyclerView.ViewHolder {

        private TextView headTextView;
        private TextView bodyTextView;
        private TextView indicatorTextView;
        private View rootView;
        private ImageView ivPriscription;
        private TextView tv_not_uploaded;
        Button btnEndVisit, btnVisitDetails;
        TextView tvAgeGender;

        public TodayPatientViewHolder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head_text_view);
            bodyTextView = itemView.findViewById(R.id.list_item_body_text_view);
            indicatorTextView = itemView.findViewById(R.id.list_item_indicator_text_view);
            ivPriscription = itemView.findViewById(R.id.iv_prescription);
            tv_not_uploaded = (TextView) itemView.findViewById(R.id.tv_not_uploaded);
            btnEndVisit = itemView.findViewById(R.id.btn_end_visit);
            btnVisitDetails = itemView.findViewById(R.id.btn_visit_details);
            tvAgeGender = itemView.findViewById(R.id.tv_age_gender);
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
}
