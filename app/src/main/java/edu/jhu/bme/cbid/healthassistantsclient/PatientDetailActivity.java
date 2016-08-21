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
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Patient;

public class PatientDetailActivity extends AppCompatActivity {

    LocalRecordsDatabaseHelper mDbHelper;
    private WebView mWebView;

    String LOG_TAG = "Patient Detail Activity";

    String patientID = "1";
    String patientName;
    String patientStatus;
    String intentTag = "";
    Patient patient = new Patient();

    String mPatientName;
    String mPatientDob;
    String mAddress;
    String mCityState;
    String mPhone;
    String mSdw;
    String mOccupation;

    TextView dobAgeView;
    TextView occupation;
    TextView sDW;
    TextView addressLine1;
    TextView addressLine2;
    TextView phone;

    Button medHistButton;
    Button patHistButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getStringExtra("patientID");
            patientName = intent.getStringExtra("name");
            patientStatus = intent.getStringExtra("status");
            intentTag = intent.getStringExtra("tag");
            Log.v(LOG_TAG, "Patient ID: " + patientID);
            Log.v(LOG_TAG, "Patient Name: " + patientName);
            Log.v(LOG_TAG, "Status: " + patientStatus);
            Log.v(LOG_TAG, "Intent Tag: " + intentTag);
        }

        dobAgeView = (TextView) findViewById(R.id.textView_patient_info_age);
        occupation = (TextView) findViewById(R.id.textView_occup);
        sDW = (TextView) findViewById(R.id.textView_sdw);
        addressLine1 = (TextView) findViewById(R.id.textView_addr1);
        addressLine2 = (TextView) findViewById(R.id.textView_addr2);
        phone = (TextView) findViewById(R.id.textView_phone);

        queryDisplay(String.valueOf(patientID));
        setTitle(mPatientName);

        String tempString="Copyright";
        //TextView text=(TextView)findViewById(R.id.text);
        SpannableString spanString = new SpannableString(tempString);
        spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
        //text.setText(spanString);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Feature Coming Soon", Snackbar.LENGTH_LONG).show();
                }
            });
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.detail_print:
                doWebViewPrint();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void queryDisplay(String dataString) {
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
                patient.setFirstName(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                patient.setMiddleName(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                patient.setLastName(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                patient.setDateOfBirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                patient.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                patient.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                patient.setCityVillage(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                patient.setStateProvince(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                patient.setPostalCode(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                patient.setPhoneNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patient.setPatientIdentifier1(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_identifier1")));
                patient.setPatientIdentifier2(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_identifier2")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        selection = "patient_id = ?";

        String[] columns = {"value", " concept_id"};
        String orderBy = "concept_id";
        Cursor visitCursor = db.query("obs", columns, selection, args, null, null, orderBy);

        if (visitCursor.moveToFirst()) {
            do {
                int dbConceptID = visitCursor.getInt(visitCursor.getColumnIndexOrThrow("concept_id"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndexOrThrow("value"));
                //parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();

        if(patient.getMiddleName().equals("")){
            mPatientName = patient.getFirstName() + " " + patient.getLastName();
        } else {
            mPatientName = patient.getFirstName() + " " + patient.getMiddleName() + " " + patient.getLastName();
        }

        int age = HelperMethods.getAge(patient.getDateOfBirth());
        mPatientDob = "DOB: " + patient.getDateOfBirth() + " Age: " + String.valueOf(age);
        dobAgeView.setText(mPatientDob);

        mOccupation = patient.getPatientIdentifier1();
        occupation.setText(mOccupation);

        mSdw = patient.getPatientIdentifier2();
        sDW.setText(mSdw);

        mAddress = patient.getAddress1() + ", " + patient.getAddress2();
        addressLine1.setText(mAddress);

        mCityState = patient.getCityVillage() + ", " + patient.getStateProvince() + " " + patient.getPostalCode();
        addressLine2.setText(mCityState);

        mPhone = patient.getPhoneNumber();
        phone.setText(mPhone);

    }

    private void parseData(){

    }
    /*
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




                            String fName = searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name"));
                            String mName = searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name"));
                            char mInitial = '\0';
                            if (mName != null) mInitial = mName.charAt(0);
                            String lName = searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name"));
                            String dob = searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth"));
                            int age = HelperMethods.getAge(dob);

                            String addr1 = searchCursor.getString(searchCursor.getColumnIndexOrThrow("address1"));
                            String addr2 = searchCursor.getString(searchCursor.getColumnIndexOrThrow("address2"));
                            if (addr2 != null) addr1 = addr1 + " " + addr2;
                            String cityVillage = searchCursor.getString(searchCursor.getColumnIndexOrThrow("city_village"));
                            String stateProvince = searchCursor.getString(searchCursor.getColumnIndexOrThrow("state_province"));
                            String postal = searchCursor.getString(searchCursor.getColumnIndexOrThrow("postal_code"));
                            String phoneNumber = searchCursor.getString(searchCursor.getColumnIndexOrThrow("phone_number"));

                            String sdw = searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_identifier1"));
                            String occupation = searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_identifier2"));

                            String patientIdCol = searchCursor.getString(searchCursor.getColumnIndexOrThrow("_id"));

                            String photoLoc = searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_identifier3"));

        }
    }
    */

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

        // Generate an HTML document on the fly:
        String htmlDocument =
                String.format("<h1 id=\"Intelehealth-patient-detail\">Intelehealth Patient Detail</h1>\n" +
                "<h1>%s</h1>\n" +
                "<h2 id=\"basic-information\">Basic Information</h2>\n" +
                "<ul>\n" +
                "<li>%s</li>\n" +
                "<li>%s</li>\n" +
                "<li>%s</li>\n" +
                "</ul>\n" +
                "<h2 id=\"address-and-contact\">Address and Contact</h2>\n" +
                "<p>%s</p>\n" +
                "<p>%s</p>\n" +
                "<p>%s</p>\n" +
                "<h2 id=\"recent-vists\">Recent Vists</h2>\n" +
                "<h2 id=\"patient-history\">Patient History</h2>\n" +
                "<h2 id=\"family-history\">Family History</h2>\n" +
                "<h2 id=\"current-medications\">Current Medications</h2>",
                mPatientName, mPatientDob, mOccupation, mSdw, mAddress, mCityState, mPhone);
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
        String jobName = getString(R.string.app_name) + " Patient Detail";
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

    }

    protected class UpdatePatientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Snackbar.make(findViewById(R.id.clayout_detail), "Refreshing patient data", Snackbar.LENGTH_LONG).show();
        }

        @Override
        public Void doInBackground(String... params) {
            String patientId = params[0];
            queryExtraInfo(patientId);
            loadImage(patientId);

            return null;
        }

        public void queryExtraInfo(String id) {
            // TODO: Connect to OpenMRS via the Middleware API and get more info on the Patient
        }

        public void loadImage(String id) {
            // TODO: Update the image with the picture of the patient as stored on the filesystem
        }

    }

    public static void makeTextViewHyperlink(TextView tv) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(tv.getText());
        ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb, TextView.BufferType.SPANNABLE);
    }

}
