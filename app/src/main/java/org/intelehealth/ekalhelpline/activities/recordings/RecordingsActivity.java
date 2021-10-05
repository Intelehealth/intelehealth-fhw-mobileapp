package org.intelehealth.ekalhelpline.activities.recordings;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.app.AppConstants;
import org.intelehealth.ekalhelpline.networkApiCalls.ApiInterface;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import org.intelehealth.ekalhelpline.utilities.UrlModifiers;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordingsActivity extends AppCompatActivity {
    private RecordingsAdapter recycler;
    RecyclerView recyclerView;
    SessionManager sessionManager = null;
    TextView msg;
    MaterialAlertDialogBuilder dialogBuilder;
    private String TAG = RecordingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Get the intent, verify the action and get the query
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());


        msg = findViewById(R.id.textviewmessage);
        recyclerView = findViewById(R.id.recycle);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(reLayoutManager);

        UrlModifiers urlModifiers = new UrlModifiers();
        ApiInterface apiInterface = AppConstants.apiInterface;

        String encoded = "Basic " + sessionManager.getEncoded();
        apiInterface.getRecordings(urlModifiers.getRecordingListUrl("6264249481"), encoded).enqueue(new Callback<RecordingResponse>() {
            @Override
            public void onResponse(Call<RecordingResponse> call, Response<RecordingResponse> response) {
                if (response != null && response.body() != null) {
                    recyclerView.setAdapter(new RecordingsAdapter(response.body().data, RecordingsActivity.this));
                }
            }

            @Override
            public void onFailure(Call<RecordingResponse> call, Throwable t) {
                System.out.println(t);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.clearOnScrollListeners();
    }
}




