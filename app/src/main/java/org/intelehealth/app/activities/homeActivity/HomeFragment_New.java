package org.intelehealth.app.activities.homeActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;

import java.util.Objects;

public class HomeFragment_New extends Fragment {
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_ui2, container, false);
initUI();
        return view;
    }

    private void initUI() {
       ImageView viewHamburger = Objects.requireNonNull(getActivity()).findViewById(R.id.iv_hamburger);
       viewHamburger.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_hamburger));

    }

}
