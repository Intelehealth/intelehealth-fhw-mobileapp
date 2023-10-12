package org.intelehealth.app.activities.medicationAidActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.additionalDocumentsActivity.AdditionalDocumentsActivity;
import org.intelehealth.app.activities.visitSummaryActivity.HorizontalAdapter;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.MedicationAidModel;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdministerDispenseActivity extends AppCompatActivity {
    private TextInputEditText tie_medNotes, tie_aidNotes,
            tie_totalCost, tie_vendorDiscount, tie_coveredCost, tie_outOfPocket, tie_others;
    private TextView tv_medData, tv_aidData, tvSave;
    private String tag = "";
    private FrameLayout fl_med, fl_aid;
    private List<MedicationAidModel> medList, aidList;
    private ImageButton imgbtn_uploadDocs;
    private Context context;
    private RecyclerView rv_docs;
    private RecyclerView.LayoutManager docsLayoutManager;
    private String patientUuid, visitUuid, encounterVitals, encounterAdultIntials;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer_dispense);

        context = AdministerDispenseActivity.this;
        initUI();

        tie_medNotes.setHint(getString(R.string.enter_details_here));
        tie_aidNotes.setHint(getString(R.string.enter_details_here));

        tie_medNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tie_medNotes.setHint("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equalsIgnoreCase(""))
                    tie_medNotes.setHint(getString(R.string.enter_details_here));
                else
                    tie_medNotes.setHint("");
            }
        });

        tie_aidNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tie_aidNotes.setHint("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equalsIgnoreCase(""))
                    tie_aidNotes.setHint(getString(R.string.enter_details_here));
                else
                    tie_aidNotes.setHint("");
            }
        });


    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        tie_medNotes = findViewById(R.id.tie_medNotes);
        tie_aidNotes = findViewById(R.id.tie_aidNotes);

        tie_totalCost = findViewById(R.id.tie_totalCost);
        tie_vendorDiscount = findViewById(R.id.tie_vendorDiscount);
        tie_coveredCost = findViewById(R.id.tie_coveredCost);
        tie_outOfPocket = findViewById(R.id.tie_outOfPocket);
        tie_others = findViewById(R.id.tie_others);

        tv_medData = findViewById(R.id.tv_medData);
        tv_aidData = findViewById(R.id.tv_aidData);

        fl_aid = findViewById(R.id.fl_aid);
        fl_med = findViewById(R.id.fl_med);

        imgbtn_uploadDocs = findViewById(R.id.imgbtn_uploadDocs);

        rv_docs = findViewById(R.id.rv_docs);
        tvSave = findViewById(R.id.tvSave);

        medList = new ArrayList<>();
        aidList = new ArrayList<>();

        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        patientUuid = intent.getStringExtra("patientUuid");
        visitUuid = intent.getStringExtra("visitUuid");
        encounterVitals = intent.getStringExtra("encounterUuidVitals");
        encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");

        medList = (List<MedicationAidModel>) intent.getSerializableExtra("med");
        aidList = (List<MedicationAidModel>) intent.getSerializableExtra("aid");    // null on empty.

        setImagesToRV();    // TODO: handle this later with new concept id for UPLOAD_DOCS obs.
        // TODO: here max 4 images will only come.

        if (medList != null && medList.size() > 0) {
            fl_med.setVisibility(View.VISIBLE);
            String medData = "";
            for (MedicationAidModel med : medList) {
                medData = medData + (Node.bullet + " " + med.getValue()) + "\n\n";
            }
            tv_medData.setText(medData.substring(0, medData.length() - 2));
        }
        else fl_med.setVisibility(View.GONE);

        if (tag.equalsIgnoreCase("administer")) {
            getSupportActionBar().setTitle(getString(R.string.administer_medication));
            fl_aid.setVisibility(View.GONE);
        }
        else {  // ie. dispense
            getSupportActionBar().setTitle(getString(R.string.dispense_medication_and_aid));

            if (aidList != null && aidList.size() > 0) {
                fl_aid.setVisibility(View.VISIBLE);
                String aidData = "";
                for (MedicationAidModel aid : aidList) {
                    aidData = aidData + (Node.bullet + " " + aid.getValue()) + "\n\n";
                }
                tv_aidData.setText(aidData.substring(0, aidData.length() - 2));
            }
            else fl_aid.setVisibility(View.GONE);

        }

        // Edit Text - start
        tie_totalCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!tie_totalCost.getText().toString().trim().isEmpty())
                    calculateOtherAids();
            }
        });
        tie_vendorDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!tie_vendorDiscount.getText().toString().trim().isEmpty())
                    calculateOtherAids();
            }
        });
        tie_coveredCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!tie_coveredCost.getText().toString().trim().isEmpty())
                    calculateOtherAids();
            }
        });

        tie_outOfPocket.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!tie_outOfPocket.getText().toString().trim().isEmpty())
                    calculateOtherAids();
            }
        });
        // Edit Text - end

        imgbtn_uploadDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent docIntent = new Intent(context, AdditionalDocumentsActivity.class);
                docIntent.putExtra("patientUuid", patientUuid);
                docIntent.putExtra("visitUuid", visitUuid);
                docIntent.putExtra("encounterUuidVitals", encounterVitals);
                docIntent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                startActivity(docIntent);
            }
        });

        tvSave.setOnClickListener(v -> {
            checkValidation();
        });
    }

    private void setImagesToRV() {
        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        try {
            fileuuidList = imagesDAO.getImageUuid(encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_AD);  // Todo: here uploads docs new concept Id will come.
            for (String fileuuid : fileuuidList) {
                String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                if (new File(filename).exists()) {
                    fileList.add(new File(filename));
                }
            }
        }
        catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception file) {
            Logger.logD("TAG", file.getMessage());
        }
        HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
        docsLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_docs.setLayoutManager(docsLayoutManager);
        rv_docs.setAdapter(horizontalAdapter);
    }

    private void checkValidation() {
        if (medList != null && medList.size() > 0) {
            if (tie_medNotes.getText().toString().isEmpty()) {
                tie_medNotes.requestFocus();
                tie_medNotes.setError(getString(R.string.error_field_required));
                return;
            }
        }

        if (aidList != null && aidList.size() > 0) {
            if (tie_totalCost.getText().toString().isEmpty()) {
                tie_totalCost.requestFocus();
                tie_totalCost.setError(getString(R.string.error_field_required));
                return;
            }
            if (tie_vendorDiscount.getText().toString().isEmpty()) {
                tie_vendorDiscount.requestFocus();
                tie_vendorDiscount.setError(getString(R.string.error_field_required));
                return;
            }
            if (tie_coveredCost.getText().toString().isEmpty()) {
                tie_coveredCost.requestFocus();
                tie_coveredCost.setError(getString(R.string.error_field_required));
                return;
            }
            if (tie_outOfPocket.getText().toString().isEmpty()) {
                tie_outOfPocket.requestFocus();
                tie_outOfPocket.setError(getString(R.string.error_field_required));
                return;
            }
            if (tie_aidNotes.getText().toString().isEmpty()) {
                tie_aidNotes.requestFocus();
                tie_aidNotes.setError(getString(R.string.error_field_required));
                return;
            }
        }

        saveDataToDB();
    }

    private void saveDataToDB() {
        String medicineValue = tv_medData.getText().toString().trim();
        String medNotesValue = tie_medNotes.getText().toString().trim();

        String aidValue = tv_aidData.getText().toString().trim();
        String aidNotesValue = tie_aidNotes.getText().toString().trim();
        String totalCostValue = tie_totalCost.getText().toString().trim();
        String vendorDiscountValue = tie_vendorDiscount.getText().toString().trim();
        String coveredCostValue = tie_coveredCost.getText().toString().trim();
        String outOfPocketValue = tie_outOfPocket.getText().toString().trim();

        if (tag.equalsIgnoreCase("dispense")) {
            if (medList != null && medList.size() > 0) {

            }

            if (aidList != null && aidList.size() > 0) {

            }
            Toast.makeText(this, "Dispense Data Saved.", Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase("administer")) {
            if (aidList != null && aidList.size() > 0) {

            }
            Toast.makeText(this, "Administer Data Saved.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_today_patient, menu);
        inflater.inflate(R.menu.today_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void calculateOtherAids() {
        int totalCost = 0, vendorDiscount = 0, coveredCost = 0, outPocket = 0;
        int otherAid = 0;

        if (!tie_totalCost.getText().toString().isEmpty())
            totalCost = Integer.parseInt(tie_totalCost.getText().toString().trim());

        if (!tie_vendorDiscount.getText().toString().isEmpty())
            vendorDiscount = Integer.parseInt(tie_vendorDiscount.getText().toString().trim());

        if (!tie_coveredCost.getText().toString().isEmpty())
            coveredCost = Integer.parseInt(tie_coveredCost.getText().toString().trim());

        if (!tie_outOfPocket.getText().toString().isEmpty())
            outPocket = Integer.parseInt(tie_outOfPocket.getText().toString().trim());

        otherAid = totalCost - vendorDiscount - coveredCost - outPocket;

        tie_others.setText(String.valueOf(otherAid));
    }
}