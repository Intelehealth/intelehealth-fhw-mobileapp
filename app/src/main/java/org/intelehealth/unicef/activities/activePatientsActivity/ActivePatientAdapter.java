package org.intelehealth.unicef.activities.activePatientsActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
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

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.unicef.models.ActivePatientModel;
import org.intelehealth.unicef.utilities.DateAndTimeUtils;
import org.intelehealth.unicef.utilities.StringUtils;

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
                String patientStatus = "returning";
                Intent intent = new Intent(context, PatientDetailActivity.class);
                intent.putExtra("patientUuid", activePatientModel.getPatientuuid());
                intent.putExtra("status", patientStatus);
                intent.putExtra("tag", "");

                if (holder.ivPriscription.getTag().equals("1")) {
                    intent.putExtra("hasPrescription", "true");
                } else {
                    intent.putExtra("hasPrescription", "false");
                }
                context.startActivity(intent);
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
