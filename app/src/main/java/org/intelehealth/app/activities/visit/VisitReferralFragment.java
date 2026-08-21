package org.intelehealth.app.activities.visit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Referrals tab of the Prescriptions screen.
 *
 * Currently rendered with placeholder data — {@link #buildPlaceholderReferrals()}
 * stands in for a real referral data source (local DB query / API) which does
 * not exist yet in this app. Swap that method out once that data is available.
 *
 * Uses the shared {@link VisitStatusAdapter} item design (same as Received/Pending) —
 * which badges to show per row is passed in here for now; the real conditions
 * (e.g. referral-declined status from an encounter) aren't wired up yet.
 */
public class VisitReferralFragment extends Fragment {

    private TextView bannerText;
    private TextView noDataText;
    private RecyclerView recyclerReferrals;
    private VisitStatusAdapter adapter;

    public VisitReferralFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visit_referral, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bannerText = view.findViewById(R.id.referral_banner_text);
        noDataText = view.findViewById(R.id.referral_nodata);
        recyclerReferrals = view.findViewById(R.id.recycler_referrals);

        recyclerReferrals.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<VisitStatusAdapter.VisitStatusItem> referrals = buildPlaceholderReferrals();
        adapter = new VisitStatusAdapter(requireContext(), referrals, position -> {
            // No detail screen wired up for referrals yet.
        });
        recyclerReferrals.setAdapter(adapter);

        updateBanner(referrals.size());
        boolean hasReferrals = !referrals.isEmpty();
        recyclerReferrals.setVisibility(hasReferrals ? View.VISIBLE : View.GONE);
        noDataText.setVisibility(hasReferrals ? View.GONE : View.VISIBLE);

        if (getActivity() instanceof VisitActivity) {
            ((VisitActivity) getActivity()).updateReferralCount(referrals.size());
        }
    }

    private void updateBanner(int waitingCount) {
        if (waitingCount <= 0) {
            bannerText.setVisibility(View.GONE);
            return;
        }
        bannerText.setVisibility(View.VISIBLE);
        String text = waitingCount == 1
                ? getString(R.string.patient_waiting_for_specialist, waitingCount)
                : getString(R.string.patients_waiting_for_specialist, waitingCount);
        bannerText.setText(text);
    }

    private List<VisitStatusAdapter.VisitStatusItem> buildPlaceholderReferrals() {
        List<VisitStatusAdapter.VisitStatusItem> list = new ArrayList<>();
        list.add(new VisitStatusAdapter.VisitStatusItem(
                "Anthony G", "Female 21", "22 June at 18:01 PM", null,
                new VisitStatusAdapter.Badge(
                        getString(R.string.waiting_for_specialist, "Nemco"),
                        VisitStatusAdapter.BadgeColor.ORANGE),
                null));
        return list;
    }
}
