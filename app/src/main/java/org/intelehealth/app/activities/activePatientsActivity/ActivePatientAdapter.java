package org.intelehealth.app.activities.activePatientsActivity;

import static org.intelehealth.app.utilities.RTLUtils.isArabicText;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
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
import org.intelehealth.app.models.ActivePatientModel;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.StringUtils;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class ActivePatientAdapter extends RecyclerView.Adapter<ActivePatientAdapter.ActivePatientViewHolder> {

    interface OnActionListener {
        void onEndVisitClicked(ActivePatientModel activePatientModel, boolean hasPrescription);
    }

    private OnActionListener actionListener;
    List<ActivePatientModel> activePatientModels;
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<String> listPatientUUID;

    public ActivePatientAdapter(List<ActivePatientModel> activePatientModels, Context context, ArrayList<String> _listPatientUUID) {
        this.activePatientModels = activePatientModels;
        this.context = context;
        this.listPatientUUID = _listPatientUUID;
    }

    @Override
    public ActivePatientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_active_patient, parent, false);
        ActivePatientViewHolder viewHolder = new ActivePatientViewHolder(row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ActivePatientViewHolder holder, int position) {
        final ActivePatientModel activePatientModel = activePatientModels.get(position);
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
        String dob = DateAndTimeUtils.SimpleDatetoLongDate(activePatientModel.getDate_of_birth());
//        String body = String.format("%s %s (%s)", context.getString(R.string.identification_screen_prompt_age), age, activePatientModel.getGender());
        Spanned body = Html.fromHtml(context.getString(R.string.identification_screen_prompt_age) + " <b>" + age + " (" + StringUtils.getLocaleGender(context, activePatientModel.getGender()) + ")</b>");

        holder.getHeadTextView().setText(String.format("%s %s", activePatientModel.getFirst_name(), activePatientModel.getLast_name()));
        if (isArabicText(String.format("%s %s", activePatientModel.getFirst_name(), activePatientModel.getLast_name())))
            holder.getHeadTextView().setGravity(Gravity.END);
        holder.getBodyTextView().setText(activePatientModel.getOpenmrs_id());
        holder.tvAgeGender.setText(body);
        if (activePatientModel.getEnddate() == null) {
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
                String patientUuid = activePatientModel.getPatientuuid();

                String patientSelection = "uuid = ?";
                String[] patientArgs = {patientUuid};
                String[] patientColumns = {"first_name", "middle_name", "last_name", "gender",
                        "date_of_birth",};
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
                String visitOrderBy = "startdate DESC"; // Limit and DESC added by Arpan Sircar - Upon clicking the visit, we were shown the previous visits instead of current visit
                Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy, "1");

                if (visitCursor.getCount() >= 1) {
                    if (visitCursor.moveToLast() && visitCursor != null) {
                        do {
                            end_date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"));
                            visit_id = visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"));
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
                visitSummary.putExtra("name", patientName);
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
//        holder.getRootView().setOnClickListener(listener);
        holder.btnVisitDetails.setOnClickListener(listener);

        boolean enableEndVisit = false;
        for (int i = 0; i < listPatientUUID.size(); i++) {
            if (activePatientModels.get(position).getHasPrescription()) {
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
            if (activePatientModel.getEnddate() == null) {
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
                    if (activePatientModel.getEnddate() != null)
                        return;

                    if (actionListener != null)
                        actionListener.onEndVisitClicked(activePatientModel, "1".equals(holder.ivPriscription.getTag()));
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return activePatientModels.size();
    }

    public class ActivePatientViewHolder extends RecyclerView.ViewHolder {
        private TextView headTextView;
        private TextView bodyTextView;
        private TextView indicatorTextView;
        private View rootView;
        private ImageView ivPriscription;
        private TextView tv_not_uploaded;
        Button btnEndVisit, btnVisitDetails;
        TextView tvAgeGender;

        public ActivePatientViewHolder(View itemView) {
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
