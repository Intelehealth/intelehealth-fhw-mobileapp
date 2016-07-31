package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Obs;
import edu.jhu.bme.cbid.healthassistantsclient.objects.Patient;

public class VisitSummaryActivity extends AppCompatActivity {

    String LOG_TAG = "Patient Summary Activity";

    private WebView mWebView;
    private LinearLayout mLayout;

    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2;

    boolean uploaded = false;
    boolean dataChanged = false;

    Context context;

    Long patientID;
    Patient patient = new Patient();
    Obs complaint = new Obs();
    Obs famHistory = new Obs();
    Obs patHistory = new Obs();
    Obs physFindings = new Obs();
    Obs height = new Obs();
    Obs weight = new Obs();
    Obs pulse = new Obs();
    Obs bpSys = new Obs();
    Obs bpDias = new Obs();
    Obs temperature = new Obs();
    Obs spO2 = new Obs();

    String diagnosisReturned = "";
    String rxReturned = "";
    String testsReturned = "";
    String adviceReturned = "";
    String doctorName = "";
    String additionalReturned = "";

    ImageButton editVitals;
    ImageButton editComplaint;
    ImageButton editPhysical;
    ImageButton editFamHist;
    ImageButton editMedHist;

    TextView heightView;
    TextView weightView;
    TextView pulseView;
    TextView bpView;
    TextView tempView;
    TextView spO2View;
    TextView bmiView;
    TextView complaintView;
    TextView famHistView;
    TextView patHistView;
    TextView physFindingsView;

    FloatingActionButton fab;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_visit_summary, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        //do nothing
        //Use the buttons on the screen to navigate
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.summary_home:
                Intent intent = new Intent(VisitSummaryActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            case R.id.summary_print:
                doWebViewPrint();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        Bundle bundle = getIntent().getExtras();
//        patientID = bundle.getLong("patientID", 1);
//        Log.d(LOG_TAG, String.valueOf(patientID));

        //For Testing
        patientID = Long.valueOf("1");
        Calendar c = Calendar.getInstance();

//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X");
//        df.setTimeZone(TimeZone.getDefault());
//        String formattedDate = df.format(c.getTime());
//        Log.d(LOG_TAG, formattedDate);
//        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
//        Date todayDate = new Date();
//        String thisDate = currentDate.format(todayDate);
//        Log.d(LOG_TAG, thisDate);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mLayout = (LinearLayout) findViewById(R.id.summary_layout);
        context = getApplicationContext();

        editVitals = (ImageButton) findViewById(R.id.imagebutton_edit_vitals);
        editComplaint = (ImageButton) findViewById(R.id.imagebutton_edit_complaint);
        editPhysical = (ImageButton) findViewById(R.id.imagebutton_edit_physexam);
        editFamHist = (ImageButton) findViewById(R.id.imagebutton_edit_famhist);
        editMedHist = (ImageButton) findViewById(R.id.imagebutton_edit_pathist);

        editVitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        editComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        editPhysical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        editFamHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        editMedHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "Uploaded" + uploaded);
                if (uploaded) {
                    retrieveOpenMRS(view);
                } else if (!uploaded){
                    Snackbar.make(view, "Uploading to OpenMRS", Snackbar.LENGTH_LONG);
                    sendPost(view);
                }
            }
        });

        queryData(String.valueOf(patientID));

        heightView = (TextView) findViewById(R.id.textview_height_value);
        weightView = (TextView) findViewById(R.id.textview_weight_value);
        pulseView = (TextView) findViewById(R.id.textview_pulse_value);
        bpView = (TextView) findViewById(R.id.textview_bp_value);
        tempView = (TextView) findViewById(R.id.textview_temp_value);
        spO2View = (TextView) findViewById(R.id.textview_pulseox_value);
        bmiView = (TextView) findViewById(R.id.textview_bmi_value);
        complaintView = (TextView) findViewById(R.id.textview_content_complaint);
        famHistView = (TextView) findViewById(R.id.textview_content_famhist);
        patHistView = (TextView) findViewById(R.id.textview_content_pathist);
        physFindingsView = (TextView) findViewById(R.id.textview_content_physexam);

        heightView.setText(height.getValue());
        weightView.setText(weight.getValue());
        pulseView.setText(pulse.getValue());
        String bpText = bpSys.getValue() + "/" + bpDias.getValue();
        bpView.setText(bpText);

        Double mWeight = Double.parseDouble(weight.getValue());
        Double mHeight = Double.parseDouble(height.getValue());

        double numerator = mWeight;
        double denominator = (mHeight) * (mHeight);

        double bmi_value = numerator / denominator;

        mBMI = String.format(Locale.ENGLISH, "%,2f", bmi_value);
        bmiView.setText(mBMI);
        tempView.setText(temperature.getValue());
        spO2View.setText(spO2.getValue());
        complaintView.setText(complaint.getValue());
        famHistView.setText(famHistory.getValue());

        String medHistory = patHistory.getValue();
        medHistory = medHistory.replace("\"", "");
        medHistory = medHistory.replace("\n", "");
        do {
            medHistory = medHistory.replace("  ", "");
        } while (medHistory.contains("  "));
        patHistory.setValue(medHistory);
        patHistView.setText(patHistory.getValue());
        physFindingsView.setText(physFindings.getValue());
    }

    public void sendPost(View view) {
        new PostClass(this).execute();
    }

    public void retrieveOpenMRS(View view) {
        new RetrieveData(this).execute();
    }

    public void queryData(String dataString) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = "_id MATCH ?";
        String[] args = {dataString};

        ArrayList<String> uploadedFields = new ArrayList<>();

        String table = "patient";
        String[] columnsToReturn = {"first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "phone_number", "gender", "patient_identifier1", "patient_identifier2"};
        final Cursor idCursor = db.query(table, columnsToReturn, selection, args, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setFirstName(idCursor.getString(idCursor.getColumnIndex("first_name")));
                patient.setMiddleName(idCursor.getString(idCursor.getColumnIndex("middle_name")));
                patient.setLastName(idCursor.getString(idCursor.getColumnIndex("last_name")));
                patient.setDateOfBirth(idCursor.getString(idCursor.getColumnIndex("date_of_birth")));
                patient.setAddress1(idCursor.getString(idCursor.getColumnIndex("address1")));
                patient.setAddress2(idCursor.getString(idCursor.getColumnIndex("address2")));
                patient.setCityVillage(idCursor.getString(idCursor.getColumnIndex("city_village")));
                patient.setStateProvince(idCursor.getString(idCursor.getColumnIndex("state_province")));
                patient.setPostalCode(idCursor.getString(idCursor.getColumnIndex("postal_code")));
                patient.setPhoneNumber(idCursor.getString(idCursor.getColumnIndex("phone_number")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndex("gender")));
                patient.setPatientIdentifier1(idCursor.getString(idCursor.getColumnIndex("patient_identifier1")));
                patient.setPatientIdentifier2(idCursor.getString(idCursor.getColumnIndex("patient_identifier2")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        selection = "patient_id = ?";

        String[] columns = {"value", " concept_id"};
        String orderBy = "concept_id";
        Cursor visitCursor = db.query("obs", columns, selection, args, null, null, orderBy);

        if (visitCursor.moveToFirst()) {
            do {
                int dbConceptID = visitCursor.getInt(visitCursor.getColumnIndex("concept_id"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void parseData(int concept_id, String value) {
        switch (concept_id) {
            case 163187: //Medical History
                patHistory.setValue(value);
                break;
            case 163188: //Family History
                famHistory.setValue(value);
                break;
            case 163186: //Current Complaint
                complaint.setValue(value);
                break;
            case 163189: //Physical Examination
                physFindings.setValue(value);
                break;
            case 5090: //Height
                height.setValue(value);
                break;
            case 5089: //Weight
                weight.setValue(value);
                break;
            case 5087: //Pulse
                pulse.setValue(value);
                break;
            case 5085: //Systolic BP
                bpSys.setValue(value);
                break;
            case 5086: //Diastolic BP
                bpDias.setValue(value);
                break;
            case 163202: //Temperature
                temperature.setValue(value);
                break;
            case 5092: //SpO2
                spO2.setValue(value);
                break;
            default:
                break;

        }
    }

    private void doWebViewPrint() {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("Patient WebView", "page finished loading " + url);
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        String mPatientName = patient.getFirstName() + " " + patient.getMiddleName() + " " + patient.getLastName();
        String mPatientDob = patient.getDateOfBirth();
        String mAddress = patient.getAddress1() + "\n" + patient.getAddress2();
        String mCityState = patient.getCityVillage();
        String mPhone = patient.getPhoneNumber();
        String mSdw = patient.getPatientIdentifier1();
        String mOccupation = patient.getPatientIdentifier2();
        String mGender = patient.getGender();

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String mDate = df.format(c.getTime());

        String mPatHist = patHistory.getValue();
        String mFamHist = famHistory.getValue();
        mHeight = height.getValue();
        mWeight = weight.getValue();
        mBP = bpSys.getValue() + "/" + bpDias.getValue();
        mPulse = pulse.getValue();
        mTemp = temperature.getValue();
        mSPO2 = spO2.getValue();
        String mComplaint = complaint.getValue();
        String mExam = physFindings.getValue();


        // Generate an HTML document on the fly:
        String htmlDocument =
                String.format("<h1 id=\"intelecare-patient-detail\">Intelehealth Visit Summary</h1>\n" +
                                "<h1>%s</h1>\n" +
                                "<p>%s</p>\n" +
                                "<h2 id=\"patient-information\">Patient Information</h2>\n" +
                                "<ul>\n" +
                                "<li>%s</li>\n" +
                                "<li>%s</li>\n" +
                                "<li>%s</li>\n" +
                                "</ul>\n" +
                                "<h2 id=\"address-and-contact\">Address and Contact</h2>\n" +
                                "<p>%s</p>\n" +
                                "<p>%s</p>\n" +
                                "<p>%s</p>\n" +
                                "<h2 id=\"vitals\">Vitals</h2>\n" +
                                "<li>Height: %s</li>\n" +
                                "<li>Weight: %s</li>\n" +
                                "<li>BMI: %s</li>\n" +
                                "<li>Blood Pressure: %s</li>\n" +
                                "<li>Pulse: %s</li>\n" +
                                "<li>Temperature: %s</li>\n" +
                                "<li>SpO2: %s</li>\n" +
                                "<h2 id=\"patient-history\">Patient History</h2>\n" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"family-history\">Family History</h2>\n" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Complaint and Observations</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"examination\">On Examination</h2>" +
                                "<p>%s</p>\n",
                        mPatientName, mDate, mPatientDob, mOccupation, mSdw, mAddress, mCityState, mPhone, mHeight, mWeight,
                        mBMI, mBP, mPulse, mTemp, mSPO2, mPatHist, mFamHist, mComplaint, mExam);
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Visit Summary";
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

    }

    private class WebResponse {

        int responseCode = 1000;
        String responseString = "";

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public String getResponseString() {
            return responseString;
        }

        public void setResponseString(String responseString) {
            this.responseString = responseString;
        }
    }

    private class PostClass extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = PostClass.class.getSimpleName();

        private final Context context;

        public PostClass(Context c) {

            this.context = c;
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {

            String personString =
                    String.format("{\"gender\":\"%s\", " +
                                    "\"names\":[{\"givenName\":\"%s\", " +
                                    "\"middleName\":\"%s\", " +
                                    "\"familyName\":\"%s\"}], " +
                                    "\"birthdate\":\"%s\"," +
                                    "\"addresses\":[{\"address1\":\"%s\", " +
                                    "\"address2\":\"%s\"," +
                                    "\"cityVillage\":\"%s\"," +
                                    "\"stateProvince\":\"%s\"," +
                                    "\"country\":\"%s\"," +
                                    "\"postalCode\":\"%s\"}]}",
                            patient.getGender(),
                            patient.getFirstName(),
                            patient.getMiddleName(),
                            patient.getLastName(),
                            patient.getDateOfBirth(),
                            patient.getAddress1(),
                            patient.getAddress2(),
                            patient.getCityVillage(),
                            patient.getStateProvince(),
                            patient.getCountry(),
                            patient.getPostalCode());

            Log.d(LOG_TAG, "Person String: " + personString);
            WebResponse responsePerson;
            responsePerson = postCommand("person", personString);
            if (responsePerson != null && responsePerson.getResponseCode() != 201) {
                Log.d(LOG_TAG, "Person posting was unsuccessful");
                return null;
            }

            assert responsePerson != null;
            String identifierNumber = 20000 + String.valueOf(patientID);
//            Testing Purposes
//            String identifierNumber = "20004";
            String patientString =
                    String.format("{\"person\":\"%s\", " +
                                    "\"identifiers\":[{\"identifier\":\"%s\", " +
                                    "\"identifierType\":\"05a29f94-c0ed-11e2-94be-8c13b969e334\", " +
                                    "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\", " +
                                    "\"preferred\":true}]}",
                            responsePerson.getResponseString(), identifierNumber);

            Log.d(LOG_TAG, "Patient String: " + patientString);
            WebResponse responsePatient;
            responsePatient = postCommand("patient", patientString);
            if (responsePatient != null && responsePatient.getResponseCode() != 201) {
                Log.d(LOG_TAG, "Patient posting was unsuccessful");
                return null;
            }


            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date todayDate = new Date();
            String thisDate = currentDate.format(todayDate);

            //TODO: Location UUID needs to be found before doing these
            assert responsePatient != null;
            String visitString =
                    String.format("{\"startDatetime\":\"%s\"," +
                                    "\"visitType\":\"Telemedicine\"," +
                                    "\"patient\":\"%s\"," +
                                    "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\"}",
                            thisDate, responsePatient.getResponseString());
            Log.d(LOG_TAG, "Visit String: " + visitString);
            WebResponse responseVisit;
            responseVisit = postCommand("visit", visitString);
            if (responseVisit != null && responseVisit.getResponseCode() != 201) {
                Log.d(LOG_TAG, "Visit posting was unsuccessful");
                return null;
            }

            assert responseVisit != null;
            String vitalsString =
                    String.format("{\"encounterDatetime\":\"%s\"," +
                                    " \"patient\":\"%s\"," +
                                    "\"encounterType\":\"VITALS\"," +
                                    " \"visit\":\"%s\"," +
                                    "\"obs\":[" +
                                    "{\"concept\":\"5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\", \"value\":\"%s\"}," + //Weight
                                    "{\"concept\":\"5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}, " + //Height
                                    "{\"concept\":\"5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //Temperature
                                    "{\"concept\":\"5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //Pulse
                                    "{\"concept\":\"5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //BpSYS
                                    "{\"concept\":\"5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //BpDias
                                    "{\"concept\":\"5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\", \"value\":\"%s\"}]," + //Sp02
                                    "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\"}",

                            thisDate, responsePatient.getResponseString(), responseVisit.getResponseString(),
                            weight.getValue(), height.getValue(), temperature.getValue(),
                            pulse.getValue(), bpSys.getValue(),
                            bpDias.getValue(), spO2.getValue()
                    );
            Log.d(LOG_TAG, "Vitals Encounter String: " + vitalsString);
            WebResponse responseVitals;
            responseVitals = postCommand("encounter", vitalsString);
            if (responseVitals != null && responseVitals.getResponseCode() != 201) {
                Log.d(LOG_TAG, "Encounter posting was unsuccessful");
                return null;
            }

            assert responseVitals != null;
            String noteString =
                    String.format("{\"encounterDatetime\":\"%s\"," +
                                    " \"patient\":\"%s\"," +
                                    "\"encounterType\":\"ADULTINITIAL\"," +
                                    "\"visit\":\"%s\"," +
                                    "\"obs\":[" +
                                    "{\"concept\":\"35c3afdd-bb96-4b61-afb9-22a5fc2d088e\", \"value\":\"%s\"}," + //son wife daughter
                                    "{\"concept\":\"5fe2ef6f-bbf7-45df-a6ea-a284aee82ddc\",\"value\":\"%s\"}, " + //occupation
                                    "{\"concept\":\"62bff84b-795a-45ad-aae1-80e7f5163a82\",\"value\":\"%s\"}," + //medical history
                                    "{\"concept\":\"d63ae965-47fb-40e8-8f08-1f46a8a60b2b\",\"value\":\"%s\"}," + //family history
                                    "{\"concept\":\"3edb0e09-9135-481e-b8f0-07a26fa9a5ce\",\"value\":\"%s\"}," + //current complaint
                                    "{\"concept\":\"e1761e85-9b50-48ae-8c4d-e6b7eeeba084\",\"value\":\"%s\"}]," + //physical exam
                                    "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\"}",

                            thisDate, responsePatient.getResponseString(), responseVisit.getResponseString(),
                            patient.getPatientIdentifier1(), patient.getPatientIdentifier2(),
                            patHistory.getValue(), famHistory.getValue(),
                            complaint.getValue(), physFindings.getValue()
                    );
            Log.d(LOG_TAG, "Notes Encounter String: " + noteString);
            WebResponse responseNotes;
            responseNotes = postCommand("encounter", noteString);
            if (responseNotes != null && responseNotes.getResponseCode() != 201) {
                Log.d(LOG_TAG, "Notes Encounter posting was unsuccessful");
                return null;
            }

            uploaded = true;

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (uploaded){
                fab.setImageResource(R.drawable.ic_file_download_white_48px);
            } else {
                Snackbar.make(fab, "Upload failed.", Snackbar.LENGTH_LONG);
            }
            super.onPostExecute(s);
        }
    }

    private class RetrieveData extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = RetrieveData.class.getSimpleName();

        private final Context context;

        public RetrieveData(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(String... params) {

            String identifierNumber = 20000 + String.valueOf(patientID);

            //For Testing
//            String identifierNumber = "20009";
            String queryString = "?q=" + identifierNumber;
            Log.d(LOG_TAG, identifierNumber);
            WebResponse responseEncounter;
            responseEncounter = getCommand("encounter", queryString);
            if (responseEncounter != null && responseEncounter.getResponseCode() != 200) {
                Log.d(LOG_TAG, "Encounter searching was unsuccessful");
                return null;
            }


            assert responseEncounter != null;
            JSONArray resultsArray = null;
            List<String> uriList = new ArrayList<>();
            try {
                JSONObject JSONResponse = new JSONObject(responseEncounter.getResponseString());
                resultsArray = JSONResponse.getJSONArray("results");


                SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
                Date todayDate = new Date();
                String thisDate = currentDate.format(todayDate);


                String searchString = "Visit Note " + thisDate;

                if (resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject checking = resultsArray.getJSONObject(i);
                        if (checking.getString("display").equals(searchString)) {
                            uriList.add("/" + checking.getString("uuid"));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            List<WebResponse> obsResponse = new ArrayList<>();
            for (int i = 0; i < uriList.size(); i++) {
                obsResponse.add(i, getCommand("encounter", uriList.get(i)));
                if (obsResponse.get(i) != null && obsResponse.get(i).getResponseCode() != 200) {
                    Log.d(LOG_TAG, "Obs get call number " + String.valueOf(i) + " of " + String.valueOf(uriList.size()) + " was unsuccessful");
                    return null;
                }
            }


            JSONObject responseObj;
            JSONArray obsArray;
            JSONArray providersArray;

            for (int i = 0; i < obsResponse.size(); i++) {
                //Log.d(LOG_TAG, obsResponse.get(i).toString());
                //Log.d(LOG_TAG, obsResponse.get(i).getResponseString());

                try {
                    responseObj = new JSONObject(obsResponse.get(i).getResponseString());
                    obsArray = responseObj.getJSONArray("obs");
                    providersArray = responseObj.getJSONArray("encounterProviders");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }

                //Log.d(LOG_TAG, obsArray.toString());
                for (int k = 0; k < obsArray.length(); k++) {
                    String obsString = "";
                    //Log.d(LOG_TAG, obsString);
                    try {
                        JSONObject obsObj = obsArray.getJSONObject(k);
                        obsString = obsObj.getString("display");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }

                    String[] obsSplit = obsString.split(":");
                    //Log.d(LOG_TAG, obsString);

                    String obsLocation = obsSplit[0];
                    obsString = obsSplit[1];

                    if (obsLocation.contains("Visit Diagnoses")) {
                        if (!diagnosisReturned.contains(obsString) && !diagnosisReturned.isEmpty()) {
                            diagnosisReturned = diagnosisReturned + "\n" + obsString;
                        } else {
                            diagnosisReturned = obsString;
                        }
                    }

                    if (obsLocation.contains("PRESCRIPTION")) {
                        if (!rxReturned.contains(obsString) && !rxReturned.isEmpty()) {
                            rxReturned = rxReturned + "\n" + obsString;
                        } else {
                            rxReturned = obsString;
                        }

                    }

                    if (obsLocation.contains("MEDICAL ADVICE")) {
                        if (!adviceReturned.contains(obsString) && !adviceReturned.isEmpty()) {
                            adviceReturned = adviceReturned + "\n" + obsString;
                        } else {
                            adviceReturned = obsString;
                        }

                    }

                    if (obsLocation.contains("REQUESTED TESTS")) {
                        if (!testsReturned.contains(obsString) && !testsReturned.isEmpty()) {
                            testsReturned = testsReturned + "\n" + obsString;
                        } else {
                            testsReturned = obsString;
                        }

                    }

                    if (obsLocation.contains("Additional Comments")) {
                        if (!additionalReturned.contains(obsString) && !additionalReturned.isEmpty()) {
                            additionalReturned = additionalReturned + "\n" + obsString;
                        } else {
                            additionalReturned = obsString;
                        }

                    }

                }

                for (int j = 0; j < providersArray.length(); j++) {
                    String providerName;

                    try {
                        JSONObject providerObj = providersArray.getJSONObject(j);
                        providerName = providerObj.getString("display");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }

                    String[] providerSplit = providerName.split(":");
                    providerName = providerSplit[0];
                    if (!doctorName.contains(providerName) && !doctorName.isEmpty()) {
                        doctorName = doctorName + "\n" + providerName;
                    } else {
                        doctorName = providerName;
                    }

                }
                Log.d(LOG_TAG, diagnosisReturned);
                Log.d(LOG_TAG, rxReturned);
                Log.d(LOG_TAG, adviceReturned);
                Log.d(LOG_TAG, testsReturned);
                Log.d(LOG_TAG, additionalReturned);
                Log.d(LOG_TAG, doctorName);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(!diagnosisReturned.isEmpty()){
                createNewCardView(getString(R.string.card_diagnosis), diagnosisReturned, 0);
                createNewCardView(getString(R.string.card_rx), rxReturned, 1);
                createNewCardView(getString(R.string.card_advice), adviceReturned, 2);
                createNewCardView(getString(R.string.card_tests_prescribed), testsReturned, 3);
                createNewCardView(getString(R.string.card_additional_comments), additionalReturned, 4);
                createNewCardView(getString(R.string.card_doctor_details), doctorName, 5);
                Log.d(LOG_TAG, "Retrieval successful");
            }
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    private WebResponse getCommand(String urlModifier, String dataString) {
        BufferedReader reader;
        String JSONString;

        WebResponse webResponse = new WebResponse();

        try {
            Log.d(LOG_TAG, "Try Catch Entered");

            final String USERNAME = "Admin";
            final String PASSWORD = "CBIDtiger123";
            String urlString =
                    String.format("http://openmrs.amal.io:8080/openmrs/ws/rest/v1/%s%s", urlModifier, dataString);

            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", "Basic " + encoded);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
            connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

            int responseCode = connection.getResponseCode();
            webResponse.setResponseCode(responseCode);

            Log.d(LOG_TAG, "GET URL: " + url);
            Log.d(LOG_TAG, "Response Code from Server: " + String.valueOf(responseCode));

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Do Nothing.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            JSONString = buffer.toString();

            Log.d(LOG_TAG, "JSON Response: " + JSONString);
            webResponse.setResponseString(JSONString);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return webResponse;
    }

    private WebResponse postCommand(String urlModifier, String dataString) {
        BufferedReader reader;
        String JSONString;

        WebResponse webResponse = new WebResponse();

        try {

            final String USERNAME = "Admin";
            final String PASSWORD = "CBIDtiger123";
            String urlString =
                    String.format("http://openmrs.amal.io:8080/openmrs/ws/rest/v1/%s", urlModifier);

            URL url = new URL(urlString);

            byte[] outputInBytes = dataString.getBytes("UTF-8");


            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String encoded = Base64.encodeToString((USERNAME + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", "Basic " + encoded);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
            connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.write(outputInBytes);
            dStream.flush();
            dStream.close();
            int responseCode = connection.getResponseCode();
            webResponse.setResponseCode(responseCode);


            Log.d(LOG_TAG, "POST URL: " + url);
            Log.d(LOG_TAG, "Response Code from Server: " + String.valueOf(responseCode));

            // Read the input stream into a String
            InputStream inputStream = connection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            JSONString = buffer.toString();

            Log.d(LOG_TAG, "JSON Response: " + JSONString);

            try {
                JSONObject JSONResponse = new JSONObject(JSONString);
                webResponse.setResponseString(JSONResponse.getString("uuid"));

            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return webResponse;
    }

    private void createNewCardView(String title, String content, int index) {
        final LayoutInflater inflater = VisitSummaryActivity.this.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.card_simple_content, null);
        TextView titleView = (TextView) convertView.findViewById(R.id.textview_heading);
        TextView contentView = (TextView) convertView.findViewById(R.id.textview_content);
        titleView.setText(title);
        contentView.setText(content);
        mLayout.addView(convertView, index);
    }
}