package io.intelehealth.client;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InvalidObjectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.intelehealth.client.objects.Obs;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.objects.WebResponse;

public class VisitSummaryActivity extends AppCompatActivity {

    String LOG_TAG = "Patient Summary";


    //Change when used with a different organization.
    //This is a demo server.

    private WebView mWebView;
    private LinearLayout mLayout;

    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2;
    String identifierNumber;

    boolean uploaded = false;
    boolean dataChanged = false;
    String failedMessage;

    Context context;

    String patientID;
    String visitID;
    String state;
    String patientName;
    String intentTag;
    String visitUUID;

    LocalRecordsDatabaseHelper mDbHelper;
    SQLiteDatabase db;

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

    TextView nameView;
    TextView idView;
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

    String medHistory;

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;

    Button uploadButton;
    Button downloadButton;


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
//            case R.id.summary_home:
//                endVisit();
//                return true;
            case R.id.summary_print:
                doWebViewPrint();
                return true;
            case R.id.summary_endVisit:
                endVisit();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientID = intent.getStringExtra("patientID");
            visitID = intent.getStringExtra("visitID");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            intentTag = intent.getStringExtra("tag");
            Log.v(LOG_TAG, "Patient ID: " + patientID);
            Log.v(LOG_TAG, "Visit ID: " + visitID);
            Log.v(LOG_TAG, "Patient Name: " + patientName);
            Log.v(LOG_TAG, "Intent Tag: " + intentTag);
        } else {
            patientID = "AND1";
            visitID = "6";
            state = "";
            patientName = "John Alam";
            intentTag = "";
        }




        String titleSequence = patientName + ": " + getTitle();
        setTitle(titleSequence);

        mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        identifierNumber = patientID;
//        identifierNumber = "AAA2";

        int checkedDigit = checkDigit(identifierNumber);

        identifierNumber = identifierNumber + "-" + String.valueOf(checkedDigit);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mLayout = (LinearLayout) findViewById(R.id.summary_layout);
        context = getApplicationContext();


        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        editVitals = (ImageButton) findViewById(R.id.imagebutton_edit_vitals);
        editComplaint = (ImageButton) findViewById(R.id.imagebutton_edit_complaint);
        editPhysical = (ImageButton) findViewById(R.id.imagebutton_edit_physexam);
        editFamHist = (ImageButton) findViewById(R.id.imagebutton_edit_famhist);
        editMedHist = (ImageButton) findViewById(R.id.imagebutton_edit_pathist);

        editComplaint.setVisibility(View.GONE);
        editVitals.setVisibility(View.GONE);
        editPhysical.setVisibility(View.GONE);
        editFamHist.setVisibility(View.GONE);
        editMedHist.setVisibility(View.GONE);

        uploadButton = (Button) findViewById(R.id.button_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Uploading to doctor.", Snackbar.LENGTH_LONG).show();
                sendPost(view);
            }
        });

        if(intentTag != null && intentTag.equals("prior")){
            uploadButton.setEnabled(false);
        }


        queryData(String.valueOf(patientID));
        nameView = (TextView) findViewById(R.id.textView_name_value);
        idView = (TextView) findViewById(R.id.textView_id_value);

        nameView.setText(patientName);
        idView.setText(patientID);

        heightView = (TextView) findViewById(R.id.textView_height_value);
        weightView = (TextView) findViewById(R.id.textView_weight_value);
        pulseView = (TextView) findViewById(R.id.textView_pulse_value);
        bpView = (TextView) findViewById(R.id.textView_bp_value);
        tempView = (TextView) findViewById(R.id.textView_temp_value);
        spO2View = (TextView) findViewById(R.id.textView_pulseox_value);
        bmiView = (TextView) findViewById(R.id.textView_bmi_value);
        complaintView = (TextView) findViewById(R.id.textView_content_complaint);
        famHistView = (TextView) findViewById(R.id.textView_content_famhist);
        patHistView = (TextView) findViewById(R.id.textView_content_pathist);
        physFindingsView = (TextView) findViewById(R.id.textView_content_physexam);

        heightView.setText(height.getValue());
        weightView.setText(weight.getValue());
        pulseView.setText(pulse.getValue());
        String bpText = bpSys.getValue() + "/" + bpDias.getValue();
        bpView.setText(bpText);

        Double mWeight = Double.parseDouble(weight.getValue());
        Double mHeight = Double.parseDouble(height.getValue());

        double numerator = mWeight * 10000;
        double denominator = (mHeight) * (mHeight);
        double bmi_value = numerator / denominator;
        mBMI = String.format(Locale.ENGLISH, "%.2f", bmi_value);


        bmiView.setText(mBMI);
        tempView.setText(temperature.getValue());
        spO2View.setText(spO2.getValue());
        complaintView.setText(complaint.getValue());
        famHistView.setText(famHistory.getValue());

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

    private void endVisit() {
        new EndVisit().execute();
    }

    public void queryData(String dataString) {

        String patientSelection = "_id MATCH ?";
        String[] patientArgs = {dataString};

        ArrayList<String> uploadedFields = new ArrayList<>();

        String table = "patient";
        String[] columnsToReturn = {"first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province", "country",
                "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo"};
        final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

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
                patient.setCountry(idCursor.getString(idCursor.getColumnIndex("country")));
                patient.setPostalCode(idCursor.getString(idCursor.getColumnIndex("postal_code")));
                patient.setPhoneNumber(idCursor.getString(idCursor.getColumnIndex("phone_number")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndex("gender")));
                patient.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient.setPatientPhoto(idCursor.getString(idCursor.getColumnIndex("patient_photo")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();


        String[] columns = {"value", " concept_id"};
        String orderBy = "visit_id";

        try {
            String famHistSelection = "patient_id = ? AND concept_id = ?";
            String[] famHistArgs = {dataString, "163188"};
            Cursor famHistCursor = db.query("obs", columns, famHistSelection, famHistArgs, null, null, orderBy);
            famHistCursor.moveToLast();
            String famHistText = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            famHistory.setValue(famHistText);
            famHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            famHistory.setValue(""); // if family history does not exist
        }

        try {
            String medHistSelection = "patient_id = ? AND concept_id = ?";
            String[] medHistArgs = {dataString, "163187"};
            Cursor medHistCursor = db.query("obs", columns, medHistSelection, medHistArgs, null, null, orderBy);
            medHistCursor.moveToLast();
            String medHistText = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            patHistory.setValue(medHistText);
            if (!medHistText.isEmpty()) {
                medHistory = patHistory.getValue();
                medHistory = medHistory.replace("\"", "");
                medHistory = medHistory.replace("\n", "");
                do {
                    medHistory = medHistory.replace("  ", "");
                } while (medHistory.contains("  "));
            }
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            patHistory.setValue(""); // if medical history does not exist
        }

        String visitSelection = "patient_id = ? AND visit_id = ?";
        String[] visitArgs = {dataString, visitID};
        Cursor visitCursor = db.query("obs", columns, visitSelection, visitArgs, null, null, orderBy);
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
        String mState = patient.getStateProvince();
        String mCountry = patient.getCountry();

        String mSdw = patient.getSdw();
        String mOccupation = patient.getOccupation();
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
                                "<p>%s</p>\n" +
                                "<h2 id=\"patient-information\">Patient Information</h2>\n" +
                                "<ul>\n" +
                                "<li>%s</li>\n" +
                                "<li>Son/Daughter/Wife of: %s</li>\n" +
                                "<li>Occupation: %s</li>\n" +
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
                                "<p>%s</p>\n" +
                                "<h2 id=\"complaint\">Diagnosis</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Prescription</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Tests To Be Performed</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">General Advices</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Doctor's Name</h2>" +
                                "<li>%s</li>\n" +
                                "<h2 id=\"complaint\">Additional Comments</h2>" +
                                "<li>%s</li>\n",
                        mPatientName, patientID, mDate, mPatientDob, mSdw, mOccupation, mAddress, mCityState, mPhone, mHeight, mWeight,
                        mBMI, mBP, mPulse, mTemp, mSPO2, mPatHist, mFamHist, mComplaint, mExam, diagnosisReturned, rxReturned, testsReturned, adviceReturned, doctorName, additionalReturned);
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

    private class PostClass extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = PostClass.class.getSimpleName();

        private final Context context;

        public PostClass(Context c) {

            this.context = c;
        }

        @Override
        protected void onPreExecute() {
            //remove the upload button
            //Add a progress bar to the top
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String openMRSUUID = null;
            String patientSelection = "_id MATCH ?";
            String[] patientArgs = {patientID};
            String[] patientColumns = {"openmrs_uuid"};
            final Cursor idCursor = db.query("patient", patientColumns, patientSelection, patientArgs, null, null, null);

            idCursor.moveToLast();
            openMRSUUID = idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_uuid"));
            idCursor.close();

            if (openMRSUUID == null) {
                String personString =
                        String.format("{\"gender\":\"%s\", " +
                                        "\"names\":[" +
                                        "{\"givenName\":\"%s\", " +
                                        "\"middleName\":\"%s\", " +
                                        "\"familyName\":\"%s\"}], " +
                                        "\"birthdate\":\"%s\", " +
                                        "\"attributes\":[" +
                                        "{\"attributeType\":\"14d4f066-15f5-102d-96e4-000c29c2a5d7\", " +
                                        "\"value\": \"%s\"}, " +
                                        "{\"attributeType\":\"8d87236c-c2cc-11de-8d13-0010c6dffd0f\", " +
                                        "\"value\": \"Barhra\"}], " + //TODO: Change this attribute to the name of the clinic as listed in OpenMRS
                                        "\"addresses\":[" +
                                        "{\"address1\":\"%s\", " +
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
                                patient.getPhoneNumber(),
                                patient.getAddress1(),
                                patient.getAddress2(),
                                patient.getCityVillage(),
                                patient.getStateProvince(),
                                patient.getCountry(),
                                patient.getPostalCode());

                Log.d(LOG_TAG, "Person String: " + personString);
                WebResponse responsePerson;
                responsePerson = HelperMethods.postCommand("person", personString, getApplicationContext());
                if (responsePerson != null && responsePerson.getResponseCode() != 201) {
                    failedMessage = "Person posting was unsuccessful";
//                    failedStep(failedMessage);
                    Log.d(LOG_TAG, "Person posting was unsuccessful");
                    return null;
                }

                assert responsePerson != null;

                String patientString =
                        String.format("{\"person\":\"%s\", " +
                                        "\"identifiers\":[{\"identifier\":\"%s\", " +
                                        "\"identifierType\":\"05a29f94-c0ed-11e2-94be-8c13b969e334\", " +
                                        "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\", " +
                                        "\"preferred\":true}]}",

                                responsePerson.getResponseString(), identifierNumber);

                Log.d(LOG_TAG, "Patient String: " + patientString);
                WebResponse responsePatient;
                responsePatient = HelperMethods.postCommand("patient", patientString, getApplicationContext());
                if (responsePatient != null && responsePatient.getResponseCode() != 201) {
                    failedMessage = "Patient posting was unsuccessful";
//                    failedStep(failedMessage);
                    Log.d(LOG_TAG, "Patient posting was unsuccessful");
                    return null;
                }

                assert responsePatient != null;
                ContentValues contentValuesOpenMRSID = new ContentValues();
                contentValuesOpenMRSID.put("openmrs_uuid", responsePatient.getResponseString());
                String selection = "_id = ?";
                String[] args = {patientID};

                db.update(
                        "patient",
                        contentValuesOpenMRSID,
                        selection,
                        args
                );

                openMRSUUID = responsePatient.getResponseString();
            }


            String table = "visit";
            String[] columnsToReturn = {"start_datetime"};
            String orderBy = "start_datetime";
            String visitSelection = "_id = ?";
            String[] visitArgs = {visitID};
            final Cursor visitCursor = db.query(table, columnsToReturn, visitSelection, visitArgs, null, null, orderBy);
            visitCursor.moveToLast();
            String startDateTime = visitCursor.getString(visitCursor.getColumnIndexOrThrow("start_datetime"));
            visitCursor.close();

            //TODO: Location UUID needs to be found before doing these
            String visitString =
                    String.format("{\"startDatetime\":\"%s\"," +
                                    "\"visitType\":\"Telemedicine\"," +
                                    "\"patient\":\"%s\"," +
                                    "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\"}",
                            startDateTime, openMRSUUID);
            Log.d(LOG_TAG, "Visit String: " + visitString);
            WebResponse responseVisit;
            responseVisit = HelperMethods.postCommand("visit", visitString, getApplicationContext());
            if (responseVisit != null && responseVisit.getResponseCode() != 201) {
                failedMessage = "Visit posting was unsuccessful";
//                failedStep(failedMessage);
                Log.d(LOG_TAG, "Visit posting was unsuccessful");
                return null;
            }

            assert responseVisit != null;

            visitUUID = responseVisit.getResponseString();
            ContentValues contentValuesVisit = new ContentValues();
            contentValuesVisit.put("openmrs_visit_uuid", visitUUID);
            String visitUpdateSelection = "start_datetime = ?";
            String[] visitUpdateArgs = {startDateTime};

            db.update(
                    "visit",
                    contentValuesVisit,
                    visitUpdateSelection,
                    visitUpdateArgs
            );

            Double fTemp = Double.parseDouble(temperature.getValue());
            Double cTemp = (fTemp - 32) * (5 / 9);
            String tempString = String.valueOf(cTemp);

            String vitalsString =
                    String.format("{" +
                            //"\"encounterDatetime\":\"%s\"," +
                                    "\"patient\":\"%s\"," +
                                    "\"encounterType\":\"VITALS\"," +
                                   // " \"visit\":\"%s\"," +
                                    "\"obs\":[" +
                                    "{\"concept\":\"5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\", \"value\":\"%s\"}," + //Weight
                                    "{\"concept\":\"5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}, " + //Height
                                    "{\"concept\":\"5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //Temperature
                                    "{\"concept\":\"5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //Pulse
                                    "{\"concept\":\"5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //BpSYS
                                    "{\"concept\":\"5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"value\":\"%s\"}," + //BpDias
                                    "{\"concept\":\"5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\", \"value\":\"%s\"}]," + //Sp02
                                    "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\"}",

//                            startDateTime, openMRSUUID, responseVisit.getResponseString(),
                            openMRSUUID,
                            weight.getValue(), height.getValue(), tempString,
                            pulse.getValue(), bpSys.getValue(),
                            bpDias.getValue(), spO2.getValue()
                    );
            Log.d(LOG_TAG, "Vitals Encounter String: " + vitalsString);
            WebResponse responseVitals;
            responseVitals = HelperMethods.postCommand("encounter", vitalsString, getApplicationContext());
            if (responseVitals != null && responseVitals.getResponseCode() != 201) {
                failedMessage = "Encounter posting was unsuccessful";
//                failedStep(failedMessage);
                Log.d(LOG_TAG, "Encounter posting was unsuccessful");
                return null;
            }

            assert responseVitals != null;

            if (patHistory.getValue().isEmpty() || patHistory.getValue().equals("")) {
                patHistory.setValue("None");
            }
            if (famHistory.getValue().isEmpty() || famHistory.getValue().equals("")) {
                famHistory.setValue("None");
            }

            String noteString =
                    String.format("{" +
                            //"\"encounterDatetime\":\"%s\"," +
                                    " \"patient\":\"%s\"," +
                                    "\"encounterType\":\"ADULTINITIAL\"," +
                                    //"\"visit\":\"%s\"," +
                                    "\"obs\":[" +
                                    //"{\"concept\":\"35c3afdd-bb96-4b61-afb9-22a5fc2d088e\", \"value\":\"%s\"}," + //son wife daughter
                                    //"{\"concept\":\"5fe2ef6f-bbf7-45df-a6ea-a284aee82ddc\",\"value\":\"%s\"}, " + //occupation
                                    "{\"concept\":\"62bff84b-795a-45ad-aae1-80e7f5163a82\",\"value\":\"%s\"}," + //medical history
                                    "{\"concept\":\"d63ae965-47fb-40e8-8f08-1f46a8a60b2b\",\"value\":\"%s\"}," + //family history
                                    "{\"concept\":\"3edb0e09-9135-481e-b8f0-07a26fa9a5ce\",\"value\":\"%s\"}," + //current complaint
                                    "{\"concept\":\"e1761e85-9b50-48ae-8c4d-e6b7eeeba084\",\"value\":\"%s\"}]," + //physical exam
                                    "\"location\":\"1eaa9a54-0fcb-4d5c-9ec7-501d2e5bcf2a\"}",

//                            startDateTime, openMRSUUID, responseVisit.getResponseString(),
                            openMRSUUID,
                            //patient.getSdw(), patient.getOccupation(),
                            //TODO: add logic to remove SDW and occupation when they are empty
                            patHistory.getValue(), famHistory.getValue(),
                            complaint.getValue(), physFindings.getValue()
                    );
            Log.d(LOG_TAG, "Notes Encounter String: " + noteString);
            WebResponse responseNotes;
            responseNotes = HelperMethods.postCommand("encounter", noteString, getApplicationContext());
            if (responseNotes != null && responseNotes.getResponseCode() != 201) {
                failedMessage = "Notes posting was unsuccessful";
//                failedStep(failedMessage);
                Log.d(LOG_TAG, "Notes Encounter posting was unsuccessful");
                return null;
            }

            uploaded = true;

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (uploaded) {
                Snackbar.make(uploadButton, "Upload success! Waiting for doctor.", Snackbar.LENGTH_LONG).show();
                mLayout.removeView(uploadButton);

                downloadButton = new Button(VisitSummaryActivity.this);
                downloadButton.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
                downloadButton.setText(R.string.visit_summary_button_download);

                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(view, "Downloading from doctor", Snackbar.LENGTH_LONG).show();
                        retrieveOpenMRS(view);
                    }
                });

                mLayout.addView(downloadButton, 0);
            } else {
                Snackbar.make(uploadButton, "Upload failed.", Snackbar.LENGTH_LONG).show();
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
            String queryString = "?q=" + identifierNumber;
            //Log.d(LOG_TAG, identifierNumber);
            WebResponse responseEncounter;
            responseEncounter = HelperMethods.getCommand("encounter", queryString, getApplicationContext());
            if (responseEncounter != null && responseEncounter.getResponseCode() != 200) {
//                failedStep("Encounter search was unsuccessful");
                //Log.d(LOG_TAG, "Encounter searching was unsuccessful");
                return null;
            }


            assert responseEncounter != null;
            JSONArray resultsArray = null;
            List<String> uriList = new ArrayList<>();
            try {
                JSONObject JSONResponse = new JSONObject(responseEncounter.getResponseString());
                resultsArray = JSONResponse.getJSONArray("results");


                SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                Date todayDate = new Date();
                String thisDate = currentDate.format(todayDate);

                Log.d(LOG_TAG, thisDate);

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
                obsResponse.add(i, HelperMethods.getCommand("encounter", uriList.get(i), getApplicationContext()));
                if (obsResponse.get(i) != null && obsResponse.get(i).getResponseCode() != 200) {
                    String errorMessage = "Obs get call number " + String.valueOf(i) + " of " + String.valueOf(uriList.size()) + " was unsuccessful";
//                    failedStep(errorMessage);
//                    Log.d(LOG_TAG, errorMessage);
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

                    String index = obsString.substring(0, obsString.indexOf(":"));
                    String indexText = obsString.substring(obsString.indexOf(":") + 1, obsString.length());

                    if (index.contains("TELEMEDICINE DIAGNOSIS")) {
                        if (!diagnosisReturned.contains(indexText) && !diagnosisReturned.isEmpty()) {
                            diagnosisReturned = diagnosisReturned + "\n" + indexText;
                        } else {
                            diagnosisReturned = indexText;
                        }
                    }

                    if (index.contains("JSV MEDICATIONS")) {
                        if (!rxReturned.contains(indexText) && !rxReturned.isEmpty()) {
                            rxReturned = rxReturned + "\n" + indexText;
                        } else {
                            rxReturned = indexText;
                        }

                    }

                    if (index.contains("MEDICAL ADVICE")) {
                        if (!adviceReturned.contains(indexText) && !adviceReturned.isEmpty()) {
                            adviceReturned = adviceReturned + "\n" + indexText;
                        } else {
                            adviceReturned = indexText;
                        }

                    }

                    if (index.contains("REQUESTED TESTS")) {
                        if (!testsReturned.contains(indexText) && !testsReturned.isEmpty()) {
                            testsReturned = testsReturned + "\n" + indexText;
                        } else {
                            testsReturned = indexText;
                        }

                    }

                    if (index.contains("Additional Comments")) {
                        if (!additionalReturned.contains(indexText) && !additionalReturned.isEmpty()) {
                            additionalReturned = additionalReturned + "\n" + indexText;
                        } else {
                            additionalReturned = indexText;
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
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (!doctorName.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_doctor_details), doctorName, 0);

            }
            if (!additionalReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_additional_comments), additionalReturned, 0);

            }

            if (!testsReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_tests_prescribed), testsReturned, 0);

            }

            if (!adviceReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_advice), adviceReturned, 0);

            }
            if (!rxReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_rx), rxReturned, 0);

            }
            if (!diagnosisReturned.isEmpty()) {
                createNewCardView(getString(R.string.visit_summary_diagnosis), diagnosisReturned, 0);

            }

            mLayout.removeView(downloadButton);

            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    private class EndVisit extends AsyncTask<String, Void, String> {


        WebResponse endResponse;

        @Override
        protected String doInBackground(String... params) {

            String urlModifier = "visit/" + visitUUID;

            SimpleDateFormat endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date rightNow = new Date();
            String endDateTime = endDate.format(rightNow);


            String endString =
                    String.format("{\"stopDatetime\":\"%s\"," +
                                    "\"visitType\":\"a86ac96e-2e07-47a7-8e72-8216a1a75bfd\"}",
                            endDateTime);

            Log.d("End String", endString);

            endResponse = HelperMethods.postCommand(urlModifier, endString, getApplicationContext());

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (endResponse != null && endResponse.getResponseCode() != 200) {
//                failedStep("Visit ending failed.");
            } else {
                Intent intent = new Intent(VisitSummaryActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        }
    }

    private void createNewCardView(String title, String content, int index) {
        final LayoutInflater inflater = VisitSummaryActivity.this.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.card_doctor_content, null);
        TextView titleView = (TextView) convertView.findViewById(R.id.textview_heading);
        TextView contentView = (TextView) convertView.findViewById(R.id.textview_content);
        titleView.setText(title);
        contentView.setText(content);
        mLayout.addView(convertView, index);
    }

    public int checkDigit(String idWithoutCheckDigit) {

        // allowable characters within identifier
        String validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVYWXZ_";

        // remove leading or trailing whitespace, convert to uppercase
        idWithoutCheckDigit = idWithoutCheckDigit.trim().toUpperCase();

        // this will be a running total
        int sum = 0;

        // loop through digits from right to left
        for (int i = 0; i < idWithoutCheckDigit.length(); i++) {


            //set ch to "current" character to be processed
            char ch = idWithoutCheckDigit
                    .charAt(idWithoutCheckDigit.length() - i - 1);


            // throw exception for invalid characters
            if (validChars.indexOf(ch) == -1)
                try {
                    throw new InvalidObjectException("\"" + ch + "\" is an invalid character");
                } catch (InvalidObjectException e) {
                    e.printStackTrace();
                }

            // our "digit" is calculated using ASCII value - 48
            int digit = (int) ch - 48;

            // weight will be the current digit's contribution to
            // the running total
            int weight;
            if (i % 2 == 0) {

                // for alternating digits starting with the rightmost, we
                // use our formula this is the same as multiplying x 2 and
                // adding digits together for values 0 to 9.  Using the
                // following formula allows us to gracefully calculate a
                // weight for non-numeric "digits" as well (from their
                // ASCII value - 48).

                weight = (2 * digit) - (int) (digit / 5) * 9;

            } else {

                // even-positioned digits just contribute their ascii
                // value minus 48
                weight = digit;

            }

            // keep a running total of weights
            sum += weight;

        }


        // avoid sum less than 10 (if characters below "0" allowed,
        // this could happen)
        sum = Math.abs(sum) + 10;

        // check digit is amount needed to reach next number
        // divisible by ten
        return (10 - (sum % 10)) % 10;

    }

    public void failedStep(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VisitSummaryActivity.this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private long insertDb(String value) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this);

        final int CREATOR_ID = 42;
        //TODO: Get the right creator_ID


        final int CONCEPT_ID = 163187; // RHK MEDICAL HISTORY BLURB
        //Eventually will be stored in a separate table

        ContentValues complaintEntries = new ContentValues();

        complaintEntries.put("patient_id", patientID);
        complaintEntries.put("visit_id", visitID);
        complaintEntries.put("value", value);
        complaintEntries.put("concept_id", CONCEPT_ID);
        complaintEntries.put("creator", CREATOR_ID);

        SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
        return localdb.insert("obs", null, complaintEntries);
    }

}