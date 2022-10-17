package org.intelehealth.app.activities.identificationActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;

public class Fragment_FirstScreen extends Fragment {
    private View view;
    private Button frag1_nxt_btn_main;
    private ImageView personal_icon, address_icon, other_icon;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registration_firstscreen, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        frag1_nxt_btn_main = view.findViewById(R.id.frag1_nxt_btn_main);
        personal_icon = getActivity().findViewById(R.id.addpatient_icon);
        address_icon = getActivity().findViewById(R.id.addresslocation_icon);
        other_icon = getActivity().findViewById(R.id.other_icon);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        personal_icon.setImageDrawable(getResources().getDrawable(R.drawable.addpatient_icon));
        address_icon.setImageDrawable(getResources().getDrawable(R.drawable.addresslocation_icon_unselected));
        other_icon.setImageDrawable(getResources().getDrawable(R.drawable.other_icon_unselected));

        frag1_nxt_btn_main.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
         //   bundle.putString("");
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_firstscreen, new Fragment_SecondScreen())
                    .commit();
        });
    }
}
