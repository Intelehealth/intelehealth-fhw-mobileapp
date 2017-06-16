
package io.intelehealth.client;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.intelehealth.client.db.LocalRecordsDatabaseHelper;
import io.intelehealth.client.objects.Patient;
import io.intelehealth.client.services.ClientService;

/**
 * This class displays all details about the patient.It also enables to print these details.
 * It creates a summary list of older visits of patient.
 */
public class PatientDetailActivity extends AppCompatActivity {

    LocalRecordsDatabaseHelper mDbHelper;
    private WebView mWebView;

    String LOG_TAG = "Patient Detail Activity";

    String patientID;
    String patientName;
    String visitID;
    String intentTag = "";
    Patient patient = new Patient();

    Button newVisit;
    LinearLayout previousVisitsList;



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
            intentTag = intent.getStringExtra("tag");
//            Log.v(TAG, "Patient ID: " + patientID);
//            Log.v(TAG, "Patient Name: " + patientName);
//            Log.v(TAG, "Intent Tag: " + intentTag);
        }

        patient.setId(patientID);
        setDisplay(String.valueOf(patientID));

        if (intentTag != null && intentTag.equals("new")) {
            Intent serviceIntent = new Intent(this, ClientService.class);
            serviceIntent.putExtra("serviceCall", "patient");
            serviceIntent.putExtra("patientID", patientID);
            serviceIntent.putExtra("name", patientName);
            startService(serviceIntent);
        }

        newVisit = (Button) findViewById(R.id.button_new_visit);
        newVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(PatientDetailActivity.this, ComplaintNodeActivity.class);
                String fullName = patient.getFirstName() + " " + patient.getLastName();
                intent2.putExtra("patientID", patientID);

                SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
                Date todayDate = new Date();
                String thisDate = currentDate.format(todayDate);

                ContentValues visitData = new ContentValues();
                visitData.put("patient_id", patient.getId());
                visitData.put("start_datetime", thisDate);
                visitData.put("visit_type_id", 0);
                visitData.put("visit_location_id", 0);
                visitData.put("visit_creator", 0);

                LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(PatientDetailActivity.this);
                SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
                Long visitLong = localdb.insert(
                        "visit",
                        null,
                        visitData
                );

                visitID = String.valueOf(visitLong);
                localdb.close();
                intent2.putExtra("visitID", visitID);
                intent2.putExtra("name", fullName);


                intent2.putExtra("tag", "new");
                startActivity(intent2);
            }
        });

        final CardView patientSummary = (CardView) findViewById(R.id.cardView_patDetail_summary);
        patientSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


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
            case R.id.detail_home:
                Intent intent = new Intent(PatientDetailActivity.this, HomeActivity.class);
                startActivity(intent);
                return true;
            case R.id.detail_print:
                doWebViewPrint();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method displays basic details about patient (eg: name, address).
     * @param dataString variable of type String
     * @return           void
     */
    public void setDisplay(String dataString) {
        LocalRecordsDatabaseHelper mDbHelper = new LocalRecordsDatabaseHelper(this.getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String patientSelection = "_id MATCH ?";
        String[] patientArgs = {dataString};
        String[] patientColumns = {"first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code","country","phone_number", "gender", "sdw", "occupation", "patient_photo"};
        final Cursor idCursor = db.query("patient", patientColumns, patientSelection, patientArgs, null, null, null);

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
                patient.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                patient.setPhoneNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                patient.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient.setPatientPhoto(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        ImageView photoView = (ImageView) findViewById(R.id.imageView_patient);

        TextView idView = (TextView) findViewById(R.id.textView_ID);
        TextView dobView = (TextView) findViewById(R.id.textView_DOB);
        TextView ageView = (TextView) findViewById(R.id.textView_age);
        TextView addr1View = (TextView) findViewById(R.id.textView_address_1);
        TableRow addr2Row = (TableRow) findViewById(R.id.tableRow_addr2);
        TextView addr2View = (TextView) findViewById(R.id.textView_address2);
        TextView addrFinalView = (TextView) findViewById(R.id.textView_address_final);
        TextView phoneView = (TextView) findViewById(R.id.textView_phone);
        TextView sdwView = (TextView) findViewById(R.id.textView_SDW);
        TableRow sdwRow = (TableRow) findViewById(R.id.tableRow_SDW);
        TextView occuView = (TextView) findViewById(R.id.textView_occupation);
        TableRow occuRow = (TableRow) findViewById(R.id.tableRow_Occupation);

        TextView medHistView = (TextView) findViewById(R.id.textView_patHist);
        TextView famHistView = (TextView) findViewById(R.id.textView_famHist);

        if (patient.getMiddleName() == null) {
            patientName = patient.getLastName() + ", " + patient.getFirstName();
        } else {
            patientName = patient.getLastName() + ", " + patient.getFirstName() + " " + patient.getMiddleName();
        }
        setTitle(patientName);

        if (patient.getPatientPhoto() != null || patient.getPatientPhoto() != "") {
            Bitmap imageBitmap = BitmapFactory.decodeFile(patient.getPatientPhoto());
            ImageView mImageView = (ImageView) findViewById(R.id.imageView_patient);
            mImageView.setImageBitmap(imageBitmap);
        }


        idView.setText(patient.getId());
        int age = HelperMethods.getAge(patient.getDateOfBirth());
        ageView.setText(String.valueOf(age));
        dobView.setText(patient.getDateOfBirth());
        if (patient.getAddress1() == null || patient.getAddress2().equals("")) {
            addr1View.setVisibility(View.GONE);
        } else {
            addr1View.setText(patient.getAddress1());
        }
        if (patient.getAddress2() == null || patient.getAddress2().equals("")) {
            addr2Row.setVisibility(View.GONE);
        } else {
            addr2View.setText(patient.getAddress2());
        }
        String city_village;
        if (patient.getCityVillage() != null) {
            city_village = patient.getCityVillage().trim();
        } else {
            city_village = "";
        }
        String postal_code;
        if (patient.getPostalCode() != null) {
            postal_code = patient.getPostalCode().trim()+",";
        } else {
            postal_code = "";
        }

        String addrFinalLine =
                String.format("%s, %s, %s %s",
                        city_village, patient.getStateProvince(),
                        postal_code, patient.getCountry());
        addrFinalView.setText(addrFinalLine);
        phoneView.setText(patient.getPhoneNumber());

        if (patient.getSdw() != null && !patient.getSdw().equals("")) {
            sdwView.setText(patient.getSdw());
        } else {
            sdwRow.setVisibility(View.GONE);
        }

        if (patient.getOccupation() != null && !patient.getOccupation().equals("")) {
            occuView.setText(patient.getOccupation());
        } else {
            occuRow.setVisibility(View.GONE);
        }

        if (visitID != null) {
            CardView histCardView = (CardView) findViewById(R.id.cardView_history);
            histCardView.setVisibility(View.GONE);
        } else {
            String medHistSelection = "patient_id = ? AND concept_id = ?";
            String[] medHistArgs = {dataString, "163187"};
            String[] medHistColumms = {"value", " concept_id"};
            Cursor medHistCursor = db.query("obs", medHistColumms, medHistSelection, medHistArgs, null, null, null);
            medHistCursor.moveToLast();

            String medHistValue;

            try {
                medHistValue = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            } catch (Exception e) {
                medHistValue = "";
            } finally {
                medHistCursor.close();
            }

            if (medHistValue != null && !medHistValue.equals("")) {
                medHistView.setText(medHistValue);
            } else {
                medHistView.setText(getString(R.string.string_no_hist));
            }


            String famHistSelection = "patient_id = ? AND concept_id = ?";
            String[] famHistArgs = {dataString, "163188"};
            String[] famHistColumns = {"value", " concept_id"};
            Cursor famHistCursor = db.query("obs", famHistColumns, famHistSelection, famHistArgs, null, null, null);
            famHistCursor.moveToLast();
            String famHistValue;

            try {
                famHistValue = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            } catch (Exception e) {
                famHistValue = "";
            } finally {
                famHistCursor.close();
            }

            if (famHistValue != null && !famHistValue.equals("")) {
                famHistView.setText(famHistValue);
            } else {
                famHistView.setText(getString(R.string.string_no_hist));
            }
        }

        String visitSelection = "patient_id = ?";
        String[] visitArgs = {dataString};
        String[] visitColumns = {"_id, start_datetime"};
        String visitOrderBy = "_id";
        Cursor visitCursor = db.query("visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);
        previousVisitsList = (LinearLayout) findViewById(R.id.linearLayout_previous_visits);

        if (visitCursor.getCount() < 1) {
            neverSeen();
        } else {

            if (visitCursor.moveToLast()) {
                do {
                    String date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("start_datetime"));
                    Integer visit_id = visitCursor.getInt(visitCursor.getColumnIndexOrThrow("_id"));
                    SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    try {
                        Date formatted = currentDate.parse(date);
                        String visitDate = currentDate.format(formatted);
                        createOldVisit(visitDate,visit_id);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } while (visitCursor.moveToPrevious());
            }
        }
        visitCursor.close();

    }


    /**
     * This method prints the basic details of patient.
     * It makes use of PRINT_SERVICE from PrintManager
     * @return void
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
//                Log.i("Patient WebView", "page finished loading " + url);
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        String patientDob = ((TextView) findViewById(R.id.textView_DOB)).getText().toString();
        String patientAge = ((TextView) findViewById(R.id.textView_age)).getText().toString();
        String patientAddr1 = ((TextView) findViewById(R.id.textView_address_1)).getText().toString();
        String patientAddr2 = ((TextView) findViewById(R.id.textView_address2)).getText().toString();
        String patientAddrFinal = ((TextView) findViewById(R.id.textView_address_final)).getText().toString();
        String patientPhone = ((TextView) findViewById(R.id.textView_phone)).getText().toString();
        String patientMedHist = ((TextView) findViewById(R.id.textView_patHist)).getText().toString();
        String patientFamHist = ((TextView) findViewById(R.id.textView_famHist)).getText().toString();


        // Generate an HTML document on the fly:
        String htmlDocument =
                String.format("<h1 id=\"Intelehealth-patient-detail\">Intelehealth Patient Detail</h1>\n" +
                                "<h1>%s</h1>\n" +
                                "<h2 id=\"basic-information\">Basic Information</h2>\n" +
                                "<p><b>Patient ID</b>: %s</p>\n" +
                                "<p><b>Date of Birth</b>: %s</p>\n" +
                                "<p><b>Age</b>: %s</p>\n" +
                                "<h2 id=\"address-and-contact\">Address and Contact</h2>\n" +
                                "<p>%s</p>\n" +
                                "<p>%s</p>\n" +
                                "<p>%s</p>\n" +
                                "<p>Phone Number: %s</p>\n" +
                                "<h2 id=\"recent-vists\">Recent Vists</h2>\n" +
                                "<h2 id=\"patient-history\">Patient History</h2>\n" +
                                "<p>%s</p>\n" +
                                "<h2 id=\"family-history\">Family History</h2>\n" +
                                "<p>%s</p>\n",
//                                "<h2 id=\"current-medications\">Current Medications</h2>",
                        patientName, patientID, patientDob, patientAge, patientAddr1,
                        patientAddr2, patientAddrFinal, patientPhone, patientMedHist, patientFamHist);
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    /**
     * This method creates a print job using PrintManager instance and PrintAdapter Instance
     * @param webView  object of type WebView.
     */
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

    /**
     * This class updates patient details and image.
     */
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

    /**
     * This method retrieves details about patient's old visits.
     * @param datetime variable of type String.
     * @param visit_id variable of type int.
     * @return void
     */
    private void createOldVisit(String datetime,int visit_id) {
       // final LayoutInflater inflater = PatientDetailActivity.this.getLayoutInflater();
      //  View convertView = inflater.inflate(R.layout.list_item_previous_visit, null);
      //  TextView textView = (TextView) convertView.findViewById(R.id.textView_visit_info);
        TextView textView = new TextView(this);
        String visitString = String.format("Seen on %s.", datetime);
        textView.setText(visitString);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llp.setMargins(25, 25,25, 25);
        textView.setLayoutParams(llp);
        textView.setTag(visit_id);
       /* textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        Toast.makeText(PatientDetailActivity.this,"Touch Down",Toast.LENGTH_SHORT).show();
                        v.getParent().getParent().getParent()
                                .requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        Toast.makeText(PatientDetailActivity.this,"Touch Up",Toast.LENGTH_SHORT).show();
                        v.getParent().getParent()
                                .requestDisallowInterceptTouchEvent(false);

                        break;
                }
                return true;
            }
        });*/
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PatientDetailActivity.this,"Click",Toast.LENGTH_SHORT).show();
                int position  = (Integer)v.getTag();
                Intent visitSummary = new Intent(PatientDetailActivity.this,VisitSummaryActivity.class);
                visitSummary.putExtra("visitID", String.valueOf(position));
                visitSummary.putExtra("patientID", patientID);
                visitSummary.putExtra("name", patientName);
                visitSummary.putExtra("tag", intentTag);
                visitSummary.putExtra("pastVisit", true);
                startActivity(visitSummary);
            }
        });
        previousVisitsList.addView(textView);
        //TODO: add on click listener to open the previous visit
    }

    /**
     * This method is called when patient has no prior visits.
     * @return void
     */
    private void neverSeen() {
        final LayoutInflater inflater = PatientDetailActivity.this.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.list_item_previous_visit, null);
        TextView textView = (TextView) convertView.findViewById(R.id.textView_visit_info);
        String visitString = "No prior visits.";
        textView.setText(visitString);
        previousVisitsList.addView(convertView);
    }

}

