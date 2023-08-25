package org.intelehealth.unicef.activities.informativeVideos.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.unicef.activities.informativeVideos.adapters.HealthInfoMoreVideosAdapter_New;
import org.intelehealth.unicef.activities.informativeVideos.adapters.HealthInfoMostSearchedVideosAdapter_New;

import java.util.Objects;

public class HealthInfoVideosFragment extends Fragment {
    View view;
    public HomeScreenActivity_New activity1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_health_info_videos_ui2, container, false);
        initUI();
        return view;
    }

    private void initUI() {
        View layoutToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_home);
        ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        ivBackArrow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_arrow_back_new));
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  FragmentManager fm = Objects.requireNonNull(getActivity()).getFragmentManager();
                fm.popBackStack();*/
                Intent intent = new Intent(getActivity(), HomeScreenActivity_New.class);
                startActivity(intent);
            }
        });

        RecyclerView rvSearchedVideos = view.findViewById(R.id.rv_most_searched_info_health);
        RecyclerView rvMoreVideos = view.findViewById(R.id.rv_more_videos_info_health);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvSearchedVideos.setLayoutManager(layoutManager);
        HealthInfoMostSearchedVideosAdapter_New mostSearchedVideosAdapter_new = new HealthInfoMostSearchedVideosAdapter_New(getActivity());
        rvSearchedVideos.setAdapter(mostSearchedVideosAdapter_new);

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
        rvMoreVideos.setLayoutManager(layoutManager1);
        HealthInfoMoreVideosAdapter_New adapter_new = new HealthInfoMoreVideosAdapter_New(getActivity());
        rvMoreVideos.setAdapter(adapter_new);


    }
    @Override
    public void onResume() {
        super.onResume();
        initUI();

    }
}