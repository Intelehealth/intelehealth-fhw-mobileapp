package org.intelehealth.app.ui2.visit.reason;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.ui2.visit.VisitCreationActionListener;
import org.intelehealth.app.ui2.visit.VisitCreationActivity;
import org.intelehealth.app.ui2.visit.model.ReasonGroupData;
import org.intelehealth.app.ui2.visit.reason.adapter.ReasonListingAdapter;
import org.intelehealth.app.utilities.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VisitReasonCaptureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisitReasonCaptureFragment extends Fragment {

    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private AutoCompleteTextView mVisitReasonAutoCompleteTextView;
    private LinearLayout mSelectedComplainLinearLayout;
    private TextView mEmptyReasonLabelTextView;

    private Set<String> mSelectedComplains = new HashSet<String>();

    public VisitReasonCaptureFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
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
        mSelectedComplainLinearLayout = view.findViewById(R.id.ll_selected_container);
        mEmptyReasonLabelTextView = view.findViewById(R.id.tv_empty_reason_lbl);
        mVisitReasonAutoCompleteTextView = view.findViewById(R.id.actv_reasons);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION, mSelectedComplains); // send the selected mms
            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.rcv_all_reason);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        List<ReasonGroupData> itemList = getVisitReasonList();
        ReasonListingAdapter reasonListingAdapter = new ReasonListingAdapter(recyclerView, getActivity(), itemList, new ReasonListingAdapter.OnItemSelection() {
            @Override
            public void onSelect(String name) {
                mSelectedComplains.add(name);
                showSelectedComplains();
            }
        });
        recyclerView.setAdapter(reasonListingAdapter);

        String[] mindmapsNames = getVisitReasonFilesNamesOnly();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.select_dialog_item, mindmapsNames);

        mVisitReasonAutoCompleteTextView.setThreshold(2);
        mVisitReasonAutoCompleteTextView.setAdapter(adapter);
        mVisitReasonAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String name = (String) adapterView.getItemAtPosition(position);
                mSelectedComplains.add(name);
                showSelectedComplains();
            }
        });



        return view;
    }

    private void showSelectedComplains() {
        mSelectedComplainLinearLayout.removeAllViews();
        if (mSelectedComplains.isEmpty()) {
            mEmptyReasonLabelTextView.setVisibility(View.VISIBLE);
            mSelectedComplainLinearLayout.setVisibility(View.GONE);
        } else {
            mEmptyReasonLabelTextView.setVisibility(View.GONE);
            mSelectedComplainLinearLayout.setVisibility(View.VISIBLE);
        }

        for (String value : mSelectedComplains) {
            View itemView = View.inflate(getActivity(), R.layout.ui2_chips_for_reason_item_view, null);
            TextView nameTextView = itemView.findViewById(R.id.tv_name);
            nameTextView.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            nameTextView.setTextColor(getResources().getColor(R.color.white));
            nameTextView.setText(value);
            mSelectedComplainLinearLayout.addView(itemView);
            Space space = new Space(getActivity());
            space.setMinimumWidth(16);
            mSelectedComplainLinearLayout.addView(space);
        }
    }

    private String[] getVisitReasonFilesNamesOnly() {
        String[] fileNames = new String[0];
        try {
            String[] temp = getActivity().getApplicationContext().getAssets().list("engines");
            fileNames = new String[temp.length];
            for (int i = 0; i < temp.length; i++) {
                fileNames[i] = temp[i].split(".json")[0];
            }
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return fileNames;
    }

    /**
     * @return
     */
    private String[] getVisitReasonFiles() {
        String[] fileNames = new String[0];
        try {
            fileNames = getActivity().getApplicationContext().getAssets().list("engines");
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return fileNames;
    }

    /**
     * @return
     */
    private List<ReasonGroupData> getVisitReasonList() {
        List<ReasonGroupData> itemList = new ArrayList<>();
        String[] fileNames = getVisitReasonFilesNamesOnly();
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