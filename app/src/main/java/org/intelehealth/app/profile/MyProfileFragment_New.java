package org.intelehealth.app.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.forgotPasswordNew.ForgotPasswordActivity_New;

import java.util.Objects;

public class MyProfileFragment_New extends Fragment {
    private static final String TAG = "MyProfileFragment_New";
    View view;
    String[] textArray = {"+91", "+00", "+20", "+22"};
    Integer[] imageArray = {R.drawable.ui2_ic_country_flag_india, R.drawable.ic_flag_black_24dp,
            R.drawable.ic_account_box_black_24dp, R.drawable.ic_done_24dp};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_profile_ui2, container, false);
        initUI();
        return view;
    }

    private void initUI() {
        View layoutToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_home);
        TextView tvLocation = layoutToolbar.findViewById(R.id.tv_user_location_home);
        TextView tvLastSyncApp = layoutToolbar.findViewById(R.id.tv_app_sync_time);
        ImageView ivNotification = layoutToolbar.findViewById(R.id.imageview_notifications_home);
        ImageView ivIsInternet = layoutToolbar.findViewById(R.id.imageview_is_internet);
        ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        ivBackArrow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_arrow_back_new));
        tvLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        tvLastSyncApp.setVisibility(View.GONE);
        ivNotification.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivIsInternet.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        ivIsInternet.setLayoutParams(params);

        Spinner spinner = view.findViewById(R.id.spinner_countries_profile);

        SpinnerAdapter adapter = new SpinnerAdapter(getActivity(), R.layout.spinner_value_layout, textArray, imageArray);
        spinner.setAdapter(adapter);

        RadioGroup rgGroupGender = view.findViewById(R.id.radioGroup_gender);
        rgGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton =  group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) {
                    Log.d(TAG, "onCheckedChanged: selected  " + checkedRadioButton.getText().toString());
                    checkedRadioButton.setButtonDrawable(getActivity().getDrawable(R.drawable.ui2_ic_selected_green));

                }
            }
        });
    }
    public class SpinnerAdapter extends ArrayAdapter<String> {

        private Context ctx;
        private String[] contentArray;
        private Integer[] imageArray;

        public SpinnerAdapter(Context context, int resource, String[] objects,
                              Integer[] imageArray) {
            super(context, R.layout.spinner_value_layout, R.id.spinnerTextView, objects);
            this.ctx = context;
            this.contentArray = objects;
            this.imageArray = imageArray;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.spinner_value_layout, parent, false);

            TextView textView = (TextView) row.findViewById(R.id.spinnerTextView);
            textView.setText(contentArray[position]);

            ImageView imageView = (ImageView) row.findViewById(R.id.spinnerImages);
            imageView.setImageResource(imageArray[position]);

            return row;
        }
    }


}
