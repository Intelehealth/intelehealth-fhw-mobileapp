package org.intelehealth.app.activities.visit;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Referrals tab of the Prescriptions screen.
 *
 * Shows visits the doctor referred to a specialist/NAMCO. There is no backend
 * referral API yet (NAS-1731 backend integration is pending), so — following
 * this app's existing offline-first convention (same as the Received/Pending
 * tabs, which are also derived from local encounter/obs data, not a network
 * call) — a visit is treated as "referred" purely from local data: a
 * non-empty {@link UuidDictionary#REFERRED_SPECIALIST} obs recorded on the
 * visit's ENCOUNTER_VISIT_NOTE encounter. That's the same obs already used to
 * render the "Referred Specialist" card in {@link PrescriptionActivity} and
 * the exported prescription PDF/print/OTP-share flows — this tab just
 * surfaces visits that have it, instead of leaving it buried in those flows.
 *
 * Tapping a row opens {@link VisitDetailsActivity} — the same screen
 * Received/Pending rows open (see {@link VisitAdapter}'s row click) — which
 * independently shows the "Referred to NAMCO/specialist" banner for any
 * referred visit (see {@code VisitDetailsActivity#bindReferralInfo}), so the
 * banner also appears if this same visit is opened via Received/Pending.
 *
 * The obs value is a colon-joined "Specialty:Hospital:Type/Priority:Notes"
 * string; {@link #parseDestination(String)} pulls the hospital/destination
 * segment out of it for the badge.
 *
 * Once Backend defines a real referral/transfer status contract, replace
 * {@link #loadReferrals()}'s query (and the always-"waiting" badge) with
 * that — this local-only version is an interim implementation, not the
 * final data source.
 *
 * Uses the shared {@link VisitStatusAdapter} item design (same as Received/Pending).
 */
public class VisitReferralFragment extends Fragment {

    private TextView bannerText;
    private TextView noDataText;
    private RecyclerView recyclerReferrals;
    private VisitStatusAdapter adapter;

    /** Extra per-row data {@link VisitStatusAdapter.VisitStatusItem} doesn't carry, keyed by visitUuid. */
    private static class ReferralMeta {
        boolean hasPrescription;
        boolean isEmergency;
        String obsservermodifieddate;
        String visitStartDate;
        /** Raw REFERRED_SPECIALIST obs value — passed through to VisitDetailsActivity
         *  so it can show the "Referred to NAMCO" banner for Referral-tab visits only. */
        String referralValue;
    }

    private final Map<String, ReferralMeta> metaByVisit = new HashMap<>();

    public VisitReferralFragment() {
    }

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

        adapter = new VisitStatusAdapter(requireContext(), new ArrayList<>(), this::onReferralClicked);
        recyclerReferrals.setAdapter(adapter);

        loadReferralsInBackground();
    }

    private void loadReferralsInBackground() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            List<VisitStatusAdapter.VisitStatusItem> referrals = loadReferrals();
            new Handler(Looper.getMainLooper()).post(() -> bindReferrals(referrals));
        });
    }

    private void bindReferrals(List<VisitStatusAdapter.VisitStatusItem> referrals) {
        if (!isAdded() || adapter == null) return;

        adapter.setData(referrals);

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

    /**
     * Opens the same Visit Details screen a Received/Pending row click opens
     * (see {@link VisitAdapter}'s row click listener) — built with the same
     * intent extras, so VisitDetailsActivity behaves identically regardless
     * of which tab the visit was opened from.
     */
    private void onReferralClicked(int position) {
        if (adapter == null || !isAdded()) return;
        VisitStatusAdapter.VisitStatusItem item = adapter.getItem(position);
        if (item == null || item.getVisitUuid() == null) return;

        ReferralMeta meta = metaByVisit.get(item.getVisitUuid());

        Intent intent = new Intent(requireActivity(), VisitDetailsActivity.class);
        intent.putExtra("patientname", item.getPatientName());
        intent.putExtra("patientUuid", item.getPatientUuid());
        intent.putExtra("gender", item.getGender());
        intent.putExtra("dob", item.getDob());
        intent.putExtra("age", item.getDob() != null && !item.getDob().isEmpty()
                ? DateAndTimeUtils.getAge_FollowUp(item.getDob(), requireContext()) : "");
        intent.putExtra("priority_tag", meta != null && meta.isEmergency);
        intent.putExtra("hasPrescription", meta != null && meta.hasPrescription);
        intent.putExtra("openmrsID", item.getOpenmrsId());
        intent.putExtra("visit_ID", item.getVisitUuid());
        intent.putExtra("visit_startDate", meta != null ? meta.visitStartDate : "");
        intent.putExtra("patient_photo", item.getPhotoUrl());
        intent.putExtra("obsservermodifieddate", meta != null ? meta.obsservermodifieddate : "");
        // Referral-tab-only signal — see VisitDetailsActivity#bindReferralInfo's javadoc.
        intent.putExtra("referralValue", meta != null ? meta.referralValue : "");
        startActivity(intent);
    }

    /**
     * Visits where the doctor recorded a Referred Specialist obs on the
     * visit-note encounter — one row per referred visit, most recently
     * referred first. Mirrors the join shape used by
     * {@link VisitReceivedFragment}/{@link VisitPendingFragment}'s own
     * queries, just against the REFERRED_SPECIALIST concept instead of the
     * visit-complete encounter type.
     */
    private List<VisitStatusAdapter.VisitStatusItem> loadReferrals() {
        List<VisitStatusAdapter.VisitStatusItem> list = new ArrayList<>();
        metaByVisit.clear();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "select p.uuid as patientuuid, p.openmrs_id, p.patient_photo, p.first_name, " +
                        "p.middle_name, p.last_name, p.gender, p.date_of_birth, " +
                        "v.uuid as visituuid, v.startdate, o.value as referral_value, " +
                        "o.obsservermodifieddate " +
                        "from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o " +
                        "where p.uuid = v.patientuuid and v.uuid = e.visituuid and e.uuid = o.encounteruuid " +
                        "and e.encounter_type_uuid = ? " +
                        "and o.conceptuuid = ? " +
                        "and o.voided = 0 and o.value is not null and trim(o.value) <> '' " +
                        "and (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') " +
                        "group by v.uuid " +
                        "order by o.obsservermodifieddate DESC",
                new String[]{UuidDictionary.ENCOUNTER_VISIT_NOTE, UuidDictionary.REFERRED_SPECIALIST});

        if (cursor.moveToFirst()) {
            do {
                try {
                    list.add(toVisitStatusItem(cursor));
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private VisitStatusAdapter.VisitStatusItem toVisitStatusItem(Cursor cursor) {
        String patientUuid = cursor.getString(cursor.getColumnIndexOrThrow("patientuuid"));
        String visitUuid = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
        String firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name"));
        String middleName = cursor.getString(cursor.getColumnIndexOrThrow("middle_name"));
        String lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name"));
        String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
        String dob = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"));
        String photo = cursor.getString(cursor.getColumnIndexOrThrow("patient_photo"));
        String openmrsId = cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id"));
        String referralValue = cursor.getString(cursor.getColumnIndexOrThrow("referral_value"));
        String obsDate = cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate"));
        String startDate = cursor.getString(cursor.getColumnIndexOrThrow("startdate"));

        ReferralMeta meta = new ReferralMeta();
        meta.visitStartDate = startDate;
        meta.referralValue = referralValue;
        try {
            meta.hasPrescription = new EncounterDAO().isPrescriptionReceived(visitUuid);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        meta.obsservermodifieddate = EncounterDAO.fetchEncounterModifiedDateForPrescGiven(visitUuid);
        try {
            EncounterDAO encounterDAO = new EncounterDAO();
            String emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
            meta.isEmergency = emergencyUuid != null && !emergencyUuid.isEmpty();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        metaByVisit.put(visitUuid, meta);

        StringBuilder nameBuilder = new StringBuilder();
        if (firstName != null) nameBuilder.append(firstName);
        if (middleName != null && !middleName.trim().isEmpty()) nameBuilder.append(" ").append(middleName);
        if (lastName != null) nameBuilder.append(" ").append(lastName);

        String genderAge = dob != null && !dob.isEmpty()
                ? StringUtils.setGenderAgeLocal(requireContext(), dob, gender != null ? gender : "")
                : (gender != null ? gender : "");

        VisitStatusAdapter.Badge badge = new VisitStatusAdapter.Badge(
                getString(R.string.waiting_for_specialist, parseDestination(referralValue)),
                VisitStatusAdapter.BadgeColor.ORANGE);

        return new VisitStatusAdapter.VisitStatusItem(
                nameBuilder.toString().trim(), genderAge, formatReferralDate(obsDate, startDate), photo,
                badge, null,
                patientUuid, visitUuid, openmrsId, gender, dob);
    }

    private String formatReferralDate(@Nullable String obsDate, @Nullable String visitStartDate) {
        if (obsDate != null && !obsDate.trim().isEmpty()) {
            String formatted = DateAndTimeUtils.date_formatter(obsDate,
                    "yyyy-MM-dd HH:mm:ss", "dd MMM 'at' HH:mm a");
            if (formatted != null) return formatted;
        }
        if (visitStartDate != null && !visitStartDate.trim().isEmpty()) {
            String formatted = DateAndTimeUtils.date_formatter(visitStartDate,
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMM 'at' HH:mm a");
            if (formatted != null) return formatted;
        }
        return "";
    }

    /**
     * REFERRED_SPECIALIST obs value is a colon-joined
     * "Specialty:Hospital:Type/Priority:Notes" string (e.g.
     * "Namco_Dermatology:NAMCO Hospital:Elective:TEST RM") — pull out the
     * hospital/destination segment for the badge, falling back to the first
     * segment (or the raw value) if it isn't in that shape.
     */
    private String parseDestination(@Nullable String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) return "";
        String[] parts = rawValue.split(":");
        if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
            return parts[1].trim();
        }
        return parts[0].trim();
    }
}
