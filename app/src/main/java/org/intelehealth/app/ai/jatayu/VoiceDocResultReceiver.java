package org.intelehealth.app.ai.jatayu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import timber.log.Timber;
import org.intelehealth.jatayu.audiorecording.VisitVoiceRecorder;

/**
 * App-wide listener for Jatayu VoiceDoc results. Registered once from
 * IntelehealthApplication.onCreate, so it hears results for as long as the app process lives,
 * whichever screen is open (or none) - including results that finish in the background after an
 * offline recording or an app restart.
 */
public class VoiceDocResultReceiver extends BroadcastReceiver {

    private static final String TAG = "VoiceDocResultReceiver";

    @SuppressWarnings("deprecation")
    public static void register(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(VisitVoiceRecorder.ACTION_RESULT_CAPTURED);
        filter.addAction(VisitVoiceRecorder.ACTION_PATIENT_SUMMARY_CAPTURED);
        LocalBroadcastManager.getInstance(context.getApplicationContext())
                .registerReceiver(new VoiceDocResultReceiver(), filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String visitUuid = intent.getStringExtra(VisitVoiceRecorder.EXTRA_VISIT_UUID);
        String sessionName = intent.getStringExtra(VisitVoiceRecorder.EXTRA_SESSION_NAME);
        // log
        Timber.tag(TAG).d("Received result for visit " + visitUuid + " (" + sessionName + ")");
        if (VisitVoiceRecorder.ACTION_RESULT_CAPTURED.equals(intent.getAction())) {
            onResultCaptured(visitUuid, sessionName,
                    intent.getStringExtra(VisitVoiceRecorder.EXTRA_TRANSCRIPT),
                    intent.getStringExtra(VisitVoiceRecorder.EXTRA_SUMMARY_JSON));
        } else if (VisitVoiceRecorder.ACTION_PATIENT_SUMMARY_CAPTURED.equals(intent.getAction())) {
            onPatientSummaryCaptured(visitUuid, sessionName,
                    intent.getStringExtra(VisitVoiceRecorder.EXTRA_PATIENT_SUMMARY_JSON));
        }
    }

    /** VoiceDoc transcript and section summary for a finished recording. */
    private void onResultCaptured(String visitUuid, String sessionName, String transcript, String summaryJson) {
        Timber.tag(TAG).d("Result captured for visit " + visitUuid + " (" + sessionName + "), transcript "
                + (transcript == null ? 0 : transcript.length()) + " chars, summary " + summaryJson);
        SaveHWPatientConversationAudioSummary.saveResult(visitUuid, sessionName, transcript, summaryJson);
    }

    /** Patient summary from /api/patient-visit-summary for the same recording. */
    private void onPatientSummaryCaptured(String visitUuid, String sessionName, String patientSummaryJson) {
        Timber.tag(TAG).d("Patient summary captured for visit " + visitUuid + " (" + sessionName + "): "
                + patientSummaryJson);
        SaveHWPatientConversationAudioSummary.savePatientSummary(visitUuid, patientSummaryJson);
    }
}
