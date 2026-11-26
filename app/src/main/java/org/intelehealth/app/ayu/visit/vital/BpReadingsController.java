package org.intelehealth.app.ayu.visit.vital;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.ayu.visit.common.InputLock;
import org.intelehealth.app.ayu.visit.ncd.BpReading;
import org.intelehealth.app.ayu.visit.ncd.BpScreeningRules;

import java.util.Locale;

/**
 * Up to 3 BP readings on the Vitals screen of an NCD screening visit (Figma Section 3). Reading 1 uses the
 * screen's own BP fields. When the rules call for reading 2 or 3, a 2-minute countdown starts and the
 * record button only appears once it ends. Once that countdown starts, the earlier readings can't be changed,
 * and saved readings can't be changed from the Vitals or Visit summary. Locked fields look disabled.
 * The countdown lives with the screen, so leaving the Vitals step and coming back starts it again.
 */
class BpReadingsController {

    interface Listener {
        void onReadingsChanged();
    }

    private static final long TICK_MS = 1000;

    private final Context context;
    private final Listener listener;
    private final View card, reading1TitleRow, reading1FinalLabel, waitBox;
    private final EditText sys1, dia1;
    private final ExtraReading reading2, reading3;
    private final TextView waitMessage, waitCountdown, recordButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = this::tick;

    private boolean active;
    private boolean saved; // vitals reopened after they were saved
    private int waitingFor; // reading the countdown is for; 0 when none
    private long waitEndsAt;

    BpReadingsController(View root, EditText sys1, EditText dia1, Listener listener) {
        this.context = root.getContext();
        this.listener = listener;
        this.sys1 = sys1;
        this.dia1 = dia1;
        card = root.findViewById(R.id.ll_bp_readings_card);
        reading1TitleRow = root.findViewById(R.id.bp_reading_1_title_row);
        ((TextView) reading1TitleRow.findViewById(R.id.tv_bp_reading_title)).setText(context.getString(R.string.bp_reading_number, 1));
        reading1FinalLabel = reading1TitleRow.findViewById(R.id.tv_bp_final_reading);
        reading2 = new ExtraReading(root.findViewById(R.id.bp_reading_2), 2);
        reading3 = new ExtraReading(root.findViewById(R.id.bp_reading_3), 3);
        waitBox = root.findViewById(R.id.bp_wait_box);
        waitMessage = waitBox.findViewById(R.id.tv_bp_wait_message);
        waitCountdown = waitBox.findViewById(R.id.tv_bp_wait_countdown);
        recordButton = waitBox.findViewById(R.id.btn_bp_record_reading);
        recordButton.setOnClickListener(v -> record(waitingFor));
    }

    // Turns the card on for visits that are offered HTN screening; other visits keep the single BP
    void setActive(boolean active) {
        this.active = active;
        int padding = context.getResources().getDimensionPixelSize(R.dimen.padding_10);
        card.setBackgroundResource(active ? R.drawable.bg_bp_readings_card : 0);
        card.setPadding(active ? padding : 0, active ? padding : 0, active ? padding : 0, active ? padding : 0);
        reading1TitleRow.setVisibility(active ? View.VISIBLE : View.GONE);
        if (!active) {
            reading2.clear();
            reading3.clear();
            cancelWait();
        }
        // Quietly: the screen is still being set up
        evaluate(false);
    }

    boolean isActive() {
        return active;
    }

    // Vitals reopened from the Vitals or Visit summary: the readings already saved stay locked
    void markSaved() {
        saved = true;
        updateLocks();
    }

    // Saved readings 2 and 3 back on screen, e.g. when the vitals are edited
    void restore(@Nullable String sys2, @Nullable String dia2, @Nullable String sys3, @Nullable String dia3) {
        if (!active) return;
        reading2.restore(sys2, dia2);
        reading3.restore(sys3, dia3);
        evaluate();
    }

    // Shows, hides and clears readings 2 and 3 to match the rules
    void evaluate() {
        evaluate(true);
    }

    private void evaluate(boolean notify) {
        if (!active) {
            updateFinalLabels();
            updateLocks();
            return;
        }
        // A reading restored or recorded while its countdown ran needs no countdown
        if ((waitingFor == 2 && reading2.recorded) || (waitingFor == 3 && reading3.recorded)) cancelWait();
        BpReading r1 = BpReading.of(1, text(sys1), text(dia1));
        if (r1 != null && !isInRange(r1)) r1 = null;
        if (r1 != null) {
            if (!BpScreeningRules.atOrAboveThreshold(r1)) {
                reading2.clear();
                reading3.clear();
                cancelWait();
            } else if (!reading2.recorded) {
                startWait(2);
            } else {
                BpReading r2 = reading2.reading();
                if (r2 != null) {
                    if (!BpScreeningRules.differ(r1, r2)) {
                        reading3.clear();
                        if (waitingFor == 3) cancelWait();
                    } else if (!reading3.recorded) {
                        startWait(3);
                    }
                }
            }
        }
        updateFinalLabels();
        updateLocks();
        if (notify) listener.onReadingsChanged();
    }

    // A reading is locked once the wait for the next one has started, or when it was saved before
    private void updateLocks() {
        boolean lock1 = active && (saved || waitingFor != 0 || reading2.recorded);
        boolean lock2 = active && (reading2.saved || waitingFor == 3 || reading3.recorded);
        boolean lock3 = active && reading3.saved;
        InputLock.set(sys1, lock1);
        InputLock.set(dia1, lock1);
        InputLock.set(reading2.sys, lock2);
        InputLock.set(reading2.dia, lock2);
        InputLock.set(reading3.sys, lock3);
        InputLock.set(reading3.dia, lock3);
    }

    // All readings the rules call for are entered and valid
    boolean isComplete() {
        if (!active) return true;
        BpReading r1 = BpReading.of(1, text(sys1), text(dia1));
        if (r1 == null || !isInRange(r1)) return false;
        BpReading r2 = reading2.recorded ? reading2.reading() : null;
        BpReading r3 = reading3.recorded ? reading3.reading() : null;
        return BpScreeningRules.finalReading(r1, r2, r3) != null;
    }

    String systolic(int number) {
        return extra(number).recorded ? text(extra(number).sys) : "";
    }

    String diastolic(int number) {
        return extra(number).recorded ? text(extra(number).dia) : "";
    }

    void release() {
        handler.removeCallbacks(tick);
    }

    private ExtraReading extra(int number) {
        return number == 2 ? reading2 : reading3;
    }

    private void updateFinalLabels() {
        BpReading r1 = BpReading.of(1, text(sys1), text(dia1));
        BpReading finalReading = active ? BpScreeningRules.finalReading(r1,
                reading2.recorded ? reading2.reading() : null, reading3.recorded ? reading3.reading() : null) : null;
        int finalNumber = finalReading == null ? 0 : finalReading.getNumber();
        // No label when reading 1 is the only one
        reading1FinalLabel.setVisibility(View.GONE);
        reading2.finalLabel.setVisibility(finalNumber == 2 ? View.VISIBLE : View.GONE);
        reading3.finalLabel.setVisibility(finalNumber == 3 ? View.VISIBLE : View.GONE);
    }

    private void startWait(int number) {
        if (waitingFor == number) return;
        waitingFor = number;
        waitEndsAt = SystemClock.elapsedRealtime() + BpScreeningRules.WAIT_SECONDS * 1000L;
        waitMessage.setText(number == 2 ? R.string.bp_take_second_reading : R.string.bp_take_third_reading);
        recordButton.setText(context.getString(R.string.bp_record_reading, number));
        waitBox.setVisibility(View.VISIBLE);
        handler.removeCallbacks(tick);
        tick();
    }

    private void cancelWait() {
        waitingFor = 0;
        handler.removeCallbacks(tick);
        waitBox.setVisibility(View.GONE);
    }

    private void tick() {
        long remaining = waitEndsAt - SystemClock.elapsedRealtime();
        if (remaining > 0) {
            long seconds = (remaining + 999) / 1000;
            waitCountdown.setText(String.format(Locale.ENGLISH, "%d:%02d", seconds / 60, seconds % 60));
            waitCountdown.setVisibility(View.VISIBLE);
            recordButton.setVisibility(View.GONE);
            handler.postDelayed(tick, Math.min(TICK_MS, remaining));
        } else {
            waitCountdown.setVisibility(View.GONE);
            recordButton.setVisibility(View.VISIBLE);
        }
    }

    private void record(int number) {
        if (number != 2 && number != 3) return;
        cancelWait();
        ExtraReading reading = extra(number);
        reading.recorded = true;
        reading.root.setVisibility(View.VISIBLE);
        reading.sys.requestFocus();
        evaluate();
    }

    // Same limits as the screen's own BP validation
    private static boolean isInRange(BpReading reading) {
        return reading.getSystolic() >= Integer.parseInt(AppConstants.MINIMUM_BP_SYS)
                && reading.getSystolic() <= Integer.parseInt(AppConstants.MAXIMUM_BP_SYS)
                && reading.getDiastolic() >= Integer.parseInt(AppConstants.MINIMUM_BP_DSYS)
                && reading.getDiastolic() <= Integer.parseInt(AppConstants.MAXIMUM_BP_DSYS)
                && reading.getDiastolic() < reading.getSystolic();
    }

    private static String text(EditText editText) {
        return editText.getText().toString().trim();
    }

    // Reading 2 or 3: shown once recorded, checked with the same rules as reading 1
    private class ExtraReading implements TextWatcher {
        final View root;
        final View finalLabel;
        final EditText sys, dia;
        final TextView sysError, diaError;
        boolean recorded;
        boolean saved; // restored from saved vitals

        ExtraReading(View root, int number) {
            this.root = root;
            View titleRow = root.findViewById(R.id.bp_reading_title_row);
            ((TextView) titleRow.findViewById(R.id.tv_bp_reading_title)).setText(context.getString(R.string.bp_reading_number, number));
            finalLabel = titleRow.findViewById(R.id.tv_bp_final_reading);
            // Required once shown, like reading 1 on these visits
            ((TextView) root.findViewById(R.id.tv_bp_reading_sys_lbl)).append("*");
            ((TextView) root.findViewById(R.id.tv_bp_reading_dia_lbl)).append("*");
            sys = root.findViewById(R.id.etv_bp_reading_sys);
            dia = root.findViewById(R.id.etv_bp_reading_dia);
            sysError = root.findViewById(R.id.etv_bp_reading_sys_error);
            diaError = root.findViewById(R.id.etv_bp_reading_dia_error);
            sys.addTextChangedListener(this);
            dia.addTextChangedListener(this);
        }

        // The reading when both values are filled and valid, otherwise null
        @Nullable
        BpReading reading() {
            String s = text(sys), d = text(dia);
            showError(sysError, sys, null);
            showError(diaError, dia, null);
            if (s.isEmpty() && d.isEmpty()) return null;
            if (s.isEmpty()) return showError(sysError, sys, context.getString(R.string.error_field_required));
            if (d.isEmpty()) return showError(diaError, dia, context.getString(R.string.error_field_required));
            BpReading reading = BpReading.of(0, s, d);
            if (reading == null) return null;
            if (reading.getSystolic() < Integer.parseInt(AppConstants.MINIMUM_BP_SYS) || reading.getSystolic() > Integer.parseInt(AppConstants.MAXIMUM_BP_SYS))
                return showError(sysError, sys, context.getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
            if (reading.getDiastolic() < Integer.parseInt(AppConstants.MINIMUM_BP_DSYS) || reading.getDiastolic() > Integer.parseInt(AppConstants.MAXIMUM_BP_DSYS))
                return showError(diaError, dia, context.getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
            if (reading.getDiastolic() >= reading.getSystolic())
                return showError(diaError, dia, context.getString(R.string.bp_validation_dia));
            return BpReading.of(this == reading2 ? 2 : 3, s, d);
        }

        void restore(@Nullable String s, @Nullable String d) {
            if (s == null || s.trim().isEmpty() || d == null || d.trim().isEmpty()) return;
            recorded = true;
            saved = true;
            root.setVisibility(View.VISIBLE);
            sys.setText(s.trim());
            dia.setText(d.trim());
        }

        void clear() {
            if (!recorded) return;
            recorded = false;
            saved = false;
            sys.setText("");
            dia.setText("");
            root.setVisibility(View.GONE);
        }

        @Nullable
        private BpReading showError(TextView errorView, EditText field, @Nullable String message) {
            errorView.setText(message);
            errorView.setVisibility(message == null ? View.GONE : View.VISIBLE);
            field.setBackgroundResource(message == null ? R.drawable.bg_input_field_lockable : R.drawable.input_field_error_bg_ui2);
            if (message != null) errorView.setTextColor(ContextCompat.getColor(context, R.color.red));
            return null;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (recorded) evaluate();
        }
    }
}
