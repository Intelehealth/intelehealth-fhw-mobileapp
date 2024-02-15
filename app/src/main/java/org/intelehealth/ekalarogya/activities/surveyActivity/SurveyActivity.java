package org.intelehealth.ekalarogya.activities.surveyActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.activities.surveyActivity.fragments.FirstScreenFragment;
import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.models.dto.PatientAttributesDTO;
import org.intelehealth.ekalarogya.utilities.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SurveyActivity extends AppCompatActivity {

    public static List<PatientAttributesDTO> patientAttributesDTOList = new ArrayList<>();
    private SessionManager sessionManager = null;
    private Context updatedContext = getBaseContext();
    private Resources updatedResources, originalResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        startFragmentTransactions();
        setUpTranslationTools();
    }

    private void startFragmentTransactions() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_container, new FirstScreenFragment())
                .commit();
    }

    private void setUpTranslationTools() {
        sessionManager = new SessionManager(this);
        Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
        configuration.setLocale(new Locale("en"));
        updatedContext = getBaseContext().createConfigurationContext(configuration);
        updatedResources = updatedContext.getResources();
        originalResources = this.getResources();
    }

    public Resources getUpdatedResources() {
        return updatedResources;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}