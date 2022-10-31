package org.intelehealth.app.ui2.visit.reason;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.appointment.model.SlotInfo;
import org.intelehealth.app.ui2.visit.model.ReasonGroupData;
import org.intelehealth.app.ui2.visit.reason.adapter.ReasonListingAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VisitReasonCaptureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisitReasonCaptureFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public VisitReasonCaptureFragment() {
        // Required empty public constructor
    }

    public static VisitReasonCaptureFragment newInstance(Intent intent) {
        VisitReasonCaptureFragment fragment = new VisitReasonCaptureFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_visit_reason_capture, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rcv_all_reason);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        List<ReasonGroupData> itemList = getVisitReasonList();
        ReasonListingAdapter reasonListingAdapter = new ReasonListingAdapter(recyclerView, getActivity(), itemList, new ReasonListingAdapter.OnItemSelection() {
            @Override
            public void onSelect(SlotInfo slotInfo) {

            }
        });
        recyclerView.setAdapter(reasonListingAdapter);
        return view;
    }

    private List<ReasonGroupData> getVisitReasonList() {
        List<ReasonGroupData> itemList = new ArrayList<>();
        String[] fileNames = new String[0];
        try {
            fileNames = getActivity().getApplicationContext().getAssets().list("engines");
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        for (char c = 'A'; c <= 'Z'; ++c) {
            ReasonGroupData reasonGroupData = new ReasonGroupData();
            reasonGroupData.setAlphabet(String.valueOf(c));
            List<String> list = new ArrayList<>();
            for (int i = 0; i < fileNames.length; i++) {
                if (fileNames[i].toUpperCase().startsWith(String.valueOf(c))) {
                    list.add(fileNames[i]);
                }
            }
            reasonGroupData.setReasons(list);
            if (!list.isEmpty()) {
                itemList.add(reasonGroupData);
            }
        }


        return itemList;
    }
}