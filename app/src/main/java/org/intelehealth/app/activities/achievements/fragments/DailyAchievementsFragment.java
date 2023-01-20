package org.intelehealth.app.activities.achievements.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import java.util.Objects;

public class DailyAchievementsFragment extends Fragment {
    View view;
    public HomeScreenActivity_New activity1;
    private TextView patientsCreatedToday;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_daily_achievements_ui2, container, false);
        initUI();
        return view;
    }

    private void initUI() {
        View layoutToolbar = requireActivity().findViewById(R.id.toolbar_home);
        ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        ivBackArrow.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ui2_ic_arrow_back_new));
        ivBackArrow.setOnClickListener(v -> {

          /*  FragmentManager fm = Objects.requireNonNull(getActivity()).getFragmentManager();
            fm.popBackStack();*/
            Intent intent = new Intent(getActivity(), HomeScreenActivity_New.class);
            startActivity(intent);
        });

        TextView tvTodaysDate = view.findViewById(R.id.tv_todays_date);
        tvTodaysDate.setText(DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMMM, yyyy"));

        patientsCreatedToday = view.findViewById(R.id.tv_patients_created_today);
    }


}