package org.intelehealth.app.activities.searchPatientActivity;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Objects;

import org.intelehealth.app.R;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;

import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity;
import org.intelehealth.app.utilities.SessionManager;

public class SearchPatientAdapter extends RecyclerView.Adapter<SearchPatientAdapter.Myholder> {
    List<PatientDTO> patients;
    Context context;
    LayoutInflater layoutInflater;

    public SearchPatientAdapter(List<PatientDTO> patients, Context context) {
        this.patients = patients;
        this.context = context;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_search, parent, false);
        return new Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchPatientAdapter.Myholder holder, int position) {
        final PatientDTO patinet = patients.get(position);
        if (patinet != null) {
            //int age = DateAndTimeUtils.getAge(patinet.getDateofbirth(),context);

            String age = DateAndTimeUtils.getAgeInYearMonth(patinet.getDateofbirth(), context);
            //String dob = DateAndTimeUtils.SimpleDatetoLongDate(patinet.getDateofbirth());
            String body = context.getString(R.string.identification_screen_prompt_age) + " " + age;

            if (patinet.getOpenmrsId() != null)
                holder.headTextView.setText(patinet.getFirstname() + " " + patinet.getLastname() + ", " + patinet.getOpenmrsId());
            else holder.headTextView.setText(patinet.getFirstname() + " " + patinet.getLastname());

            holder.bodyTextView.setText(body);
        }
        holder.linearLayout.setOnClickListener(v -> fetchCreatorIDOfPatient(patinet));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class Myholder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        private TextView headTextView;
        private TextView bodyTextView;
        //  private TextView indicator;


        public Myholder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head);
            bodyTextView = itemView.findViewById(R.id.list_item_body);
            // indicator = itemView.findViewById(R.id.indicator);

            linearLayout = itemView.findViewById(R.id.searchlinear);
        }
    }

    private void fetchCreatorIDOfPatient(PatientDTO patientDTO) {
        SessionManager sessionManager = new SessionManager(context);
        PatientsDAO patientsDAO = new PatientsDAO();
        String creatorHealthWorkerUuid = patientsDAO.fetchHealthWorkerUuid(patientDTO.getUuid());
        if (sessionManager.getProviderID().equalsIgnoreCase(creatorHealthWorkerUuid)) {
            navigateToPatientDetailActivity(patientDTO);
        } else {
            displayDisclaimerDialog(patientDTO);
        }
    }

    private void displayDisclaimerDialog(PatientDTO patientDTO) {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_dialog_patient_pin, null);
        materialAlertDialogBuilder.setView(view);

        AppCompatTextView disclaimerTextView = view.findViewById(R.id.tv_title);
        Button saveButton = view.findViewById(R.id.button_save);
        TextInputEditText pinEditText = view.findViewById(R.id.et_pin);
        AlertDialog alertDialog = materialAlertDialogBuilder.create();

        disclaimerTextView.setText(context.getString(R.string.incorrect_healthworker_disclaimer));
        saveButton.setOnClickListener(v -> {
            if (isPinFieldValid(pinEditText) && isPinCorrect(pinEditText, patientDTO)) {
                navigateToPatientDetailActivity(patientDTO);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private boolean isPinFieldValid(TextInputEditText pinEditText) {
        String enteredPin = pinEditText.getText().toString();

        if (enteredPin.isEmpty()) {
            pinEditText.setError(context.getString(R.string.empty_pin_error));
            return false;
        }

        if (enteredPin.length() < 4) {
            pinEditText.setError(context.getString(R.string.please_enter_four_digit_pin));
            return false;
        }

        return true;
    }

    private boolean isPinCorrect(TextInputEditText editText, PatientDTO patientDTO) {
        String enteredPin = Objects.requireNonNull(editText.getText()).toString();
        PatientsDAO patientsDAO = new PatientsDAO();
        String patientPin = patientsDAO.fetchPatientPin(patientDTO.getUuid());
        if (patientPin.equals(enteredPin)) {
            return true;
        } else {
            Toast.makeText(context, context.getString(R.string.incorrect_pin_entered), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void navigateToPatientDetailActivity(PatientDTO patinet) {
        Log.d("search adapter", "patientuuid" + patinet.getUuid());
        String patientStatus = "returning";
        Intent intent = new Intent(context, PatientDetailActivity.class);
        intent.putExtra("patientUuid", patinet.getUuid());
        intent.putExtra("patientName", patinet.getFirstname() + "" + patinet.getLastname());
        intent.putExtra("status", patientStatus);
        intent.putExtra("tag", "search");
        intent.putExtra("hasPrescription", "false");
        context.startActivity(intent);
    }
}