package org.intelehealth.ekalhelpline.activities.callDoctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.intelehealth.ekalhelpline.R;
import org.intelehealth.ekalhelpline.activities.searchPatientActivity.SearchPatientActivity;
import org.intelehealth.ekalhelpline.activities.searchPatientActivity.SearchSuggestionProvider;
import org.intelehealth.ekalhelpline.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.ekalhelpline.app.AppConstants;
import org.intelehealth.ekalhelpline.models.CallDoctorModel;
import org.intelehealth.ekalhelpline.models.DoctorDetailsModel;
import org.intelehealth.ekalhelpline.networkApiCalls.ApiClient;
import org.intelehealth.ekalhelpline.networkApiCalls.ApiInterface;
import org.intelehealth.ekalhelpline.utilities.NetworkConnection;
import org.intelehealth.ekalhelpline.utilities.SessionManager;
import org.intelehealth.ekalhelpline.utilities.UrlModifiers;
import org.intelehealth.ekalhelpline.widget.materialprogressbar.CustomProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallDoctorActivity extends AppCompatActivity {
    SearchView searchView;
    RecyclerView recyclerView;
    SessionManager sessionManager = null;
    Toolbar toolbar;
    Context context;
    private String TAG = CallDoctorActivity.class.getSimpleName();
    CustomProgressDialog customProgressDialog;
    List<DoctorDetailsModel> doctorList;
    CallDoctorAdapter callDoctorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_doctor);
        initView();
        getDoctors();

    }

    private void getDoctors() {
        if (!NetworkConnection.isOnline(this)) {
            customProgressDialog.dismiss();
            Toast.makeText(context, R.string.no_network, Toast.LENGTH_LONG).show();
            return;
        }

        customProgressDialog.show();
        UrlModifiers urlModifiers = new UrlModifiers();
        ApiInterface apiInterface = AppConstants.apiInterface;

        apiInterface.getDoctorDetails(urlModifiers.getDoctorDetails()).enqueue(new Callback<CallDoctorModel>() {
            @Override
            public void onResponse(Call<CallDoctorModel> call, Response<CallDoctorModel> response) {
                customProgressDialog.dismiss();
                System.out.println(response);
                if(response.body()!=null && response.body().getDoctorList()!= null && response.body().getDoctorList().size()>0)
                {
                    doctorList = new ArrayList<>();
                    for(DoctorDetailsModel doctorDetailsModel: response.body().getDoctorList())
                        doctorList.add(doctorDetailsModel);

                    customProgressDialog.dismiss();
                    callDoctorAdapter = new CallDoctorAdapter(doctorList, CallDoctorActivity.this);
                    recyclerView.setAdapter(callDoctorAdapter);
                }
                else
                {
                    customProgressDialog.dismiss();
                    Toast.makeText(CallDoctorActivity.this, "No doctors available", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<CallDoctorModel> call, Throwable t) {
                System.out.println(t);
            }
        });

    }

    private void initView() {
        context = CallDoctorActivity.this;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

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
        recyclerView = findViewById(R.id.recyclerDoctor);
        LinearLayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(reLayoutManager);
        customProgressDialog = new CustomProgressDialog(context);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); //to show numbers easily...

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("Hack", "in query text change");
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(CallDoctorActivity.this,
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.clearHistory();
                filter(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void filter(String text) {
        // creating a new array list to filter our data.
        ArrayList<DoctorDetailsModel> filteredlist = new ArrayList<>();

        for (DoctorDetailsModel item : doctorList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.getDoctorName().toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            callDoctorAdapter.filterList(filteredlist);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void callDoctorApi(String doctorPhoneNo)
    {
        UrlModifiers urlModifiers = new UrlModifiers();
        String encoded = "ZDc4OGUwYjYxOGIzMTQzZTBmMmRmNDY2ZmRhZDE1NTU2MWFhZWUzYjMyZTQzMjdkOjQ5ZGYxZTdhNjM1ZDljNTc1MzY1ZmM4MmNjMDdkMWFjM2ViNzcwZTIyODRmZDI1ZQ==";
        String callPatientUrl = urlModifiers.getCallPatientExotelUrl();
        HashMap<String, String> map = new HashMap<>();
        map.put("From", sessionManager.getProviderPhoneno());
        map.put("To", doctorPhoneNo);
        map.put("CallerId", "01141236457");
        ApiClient.changeApiBaseUrl(callPatientUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        apiService.callPatient("Basic " + encoded, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println(call);
                System.out.println(response);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                new AlertDialog.Builder(context).setMessage(t.getMessage()).setPositiveButton(R.string.generic_ok, null).show();
            }
        });
    }

    public void whatsAppDoctor(String doctorPhoneNo)
    {
        String phoneNumberWithCountryCode = "+91" + doctorPhoneNo;
        String message =
                getString(R.string.hello_my_name_is) + " " + sessionManager.getChwname() + " " +
                        /*" from " + sessionManager.getState() + */getString(R.string.i_need_assistance);

        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(
                        String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                phoneNumberWithCountryCode, message))));
    }
}