package org.intelehealth.app.ayu.visit.reason;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.intelehealth.app.ayu.visit.model.ReasonGroupData;
import org.intelehealth.app.ayu.visit.reason.adapter.ReasonListingAdapter;
import org.intelehealth.app.ayu.visit.reason.adapter.SelectedChipsGridAdapter;
import org.intelehealth.app.utilities.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VisitReasonCaptureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisitReasonCaptureFragment extends Fragment {

    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private AutoCompleteTextView mVisitReasonAutoCompleteTextView;
    private RecyclerView mSelectedComplainRecyclerView;
    private TextView mEmptyReasonLabelTextView;
    //private ImageView mClearImageView;

    private List<String> mSelectedComplains = new ArrayList<>();
    private List<ReasonGroupData> mVisitReasonItemList;
    private ReasonListingAdapter mReasonListingAdapter;

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
        mSelectedComplainRecyclerView = view.findViewById(R.id.rcv_selected_container);
        //mClearImageView = view.findViewById(R.id.iv_clear);
        mEmptyReasonLabelTextView = view.findViewById(R.id.tv_empty_reason_lbl);
        mVisitReasonAutoCompleteTextView = view.findViewById(R.id.actv_reasons);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelectedComplains.isEmpty()) {
                    Toast.makeText(getActivity(), "Please select at least one complain!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION, mSelectedComplains); // send the selected mms
            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.rcv_all_reason);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mVisitReasonItemList = getVisitReasonList();
        mReasonListingAdapter = new ReasonListingAdapter(recyclerView, getActivity(), mVisitReasonItemList, new ReasonListingAdapter.OnItemSelection() {
            @Override
            public void onSelect(String name) {
                if (!mSelectedComplains.contains(name)) {
                    mSelectedComplains.add(name);
                    showSelectedComplains();
                }
            }
        });
        recyclerView.setAdapter(mReasonListingAdapter);

        String[] mindmapsNames = getVisitReasonFilesNamesOnly();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(), R.layout.ui2_select_dialog_item, mindmapsNames);

        mVisitReasonAutoCompleteTextView.setThreshold(2);
        mVisitReasonAutoCompleteTextView.setAdapter(adapter);
        mVisitReasonAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String name = (String) adapterView.getItemAtPosition(position);
                if (name != null && !name.isEmpty()) {
                    mSelectedComplains.add(name);
                    showSelectedComplains();
                    mVisitReasonAutoCompleteTextView.setText("");
                }
            }
        });
       /* mClearImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedComplains.clear();
                mEmptyReasonLabelTextView.setVisibility(View.VISIBLE);
                mClearImageView.setVisibility(View.GONE);
                mSelectedComplainRecyclerView.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Selection clear!", Toast.LENGTH_SHORT).show();
            }
        });*/

        return view;
    }

    private void showSelectedComplains() {
        if (mSelectedComplains.isEmpty()) {
            mEmptyReasonLabelTextView.setVisibility(View.VISIBLE);
            //mClearImageView.setVisibility(View.GONE);
            mSelectedComplainRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyReasonLabelTextView.setVisibility(View.GONE);
            //mClearImageView.setVisibility(View.VISIBLE);
            mSelectedComplainRecyclerView.setVisibility(View.VISIBLE);
        }


        mSelectedComplainRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        SelectedChipsGridAdapter reasonChipsGridAdapter = new SelectedChipsGridAdapter(mSelectedComplainRecyclerView, getActivity(), mSelectedComplains, new SelectedChipsGridAdapter.OnItemSelection() {
            @Override
            public void onSelect(String data) {

            }

            @Override
            public void onRemoved(String data) {
                for (int i = 0; i < mVisitReasonItemList.size(); i++) {
                    List<ReasonData> reasonDataList = mVisitReasonItemList.get(i).getReasons();
                    for (int j = 0; j < reasonDataList.size(); j++) {
                        ReasonData reasonData = reasonDataList.get(j);
                        if (reasonData.getReasonName().equalsIgnoreCase(data)) {
                            mVisitReasonItemList.get(i).getReasons().get(j).setSelected(false);
                            break;
                        }
                    }
                }
                mReasonListingAdapter.refresh(mVisitReasonItemList);
            }
        });
        mSelectedComplainRecyclerView.setAdapter(reasonChipsGridAdapter);

        /*for (String value : mSelectedComplains) {
            View itemView = View.inflate(getActivity(), R.layout.ui2_chips_for_reason_item_view, null);
            TextView nameTextView = itemView.findViewById(R.id.tv_name);
            nameTextView.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            nameTextView.setTextColor(getResources().getColor(R.color.white));
            nameTextView.setText(value);
            mSelectedComplainLinearLayout.addView(itemView);
            Space space = new Space(getActivity());
            space.setMinimumWidth(16);
            mSelectedComplainLinearLayout.addView(space);
        }*/
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
            List<ReasonData> list = new ArrayList<ReasonData>();
            for (int i = 0; i < fileNames.length; i++) {

                if (fileNames[i].toUpperCase().startsWith(String.valueOf(c))) {
                    ReasonData reasonData = new ReasonData();
                    reasonData.setReasonName(fileNames[i]);
                    list.add(reasonData);
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